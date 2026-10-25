package ch.bbw.m450.tictactoe.e2e;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.players.PerfectPlayer;

/**
 * End-to-end tests: the whole game is started via {@code main()} like a real user would do,
 * the keyboard input and the console output are replaced by Pioneer ({@code @StdIo}).
 * Runs in its own Gradle task: {@code ./gradlew e2eTest}. In the game the human is X, the
 * greedy computer player is O.
 */
@Tag("e2e")
// protection against denial of service: a test that hangs (e.g. endless loop) is stopped after
// 5 seconds. It needs its own thread, otherwise JUnit could not interrupt an endless loop.
@Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
class GameEndToEndTest implements WithAssertions {

	private static final String[] NO_ARGS = {};

	private static final String X_WINS = "...and the winner is: CROSS\n";

	// E2E-01: X plays the middle row 3-4-5, O (greedy) only gets 0 and 1
	@Test
	@StdIo({ "3", "4", "5" })
	void humanWins(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(out.capturedString()).endsWith(X_WINS);
	}

	// E2E-02: X plays 8, 7, 3, O (greedy) fills 0, 1, 2 and wins with the top row
	@Test
	@StdIo({ "8", "7", "3" })
	void computerWins(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(out.capturedString()).endsWith("...and the winner is: CIRCLE\n");
	}

	// E2E-03: all nine fields are full and nobody has three in a line
	@Test
	@StdIo({ "1", "3", "4", "6", "8" })
	void draw(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(out.capturedString()).endsWith("it's a draw!\n");
	}

	// E2E-04: invalid input is asked again, the game continues normally
	@Test
	@StdIo({ "abc", "3", "", "4", "-1", "99999999999999999999", "5" })
	void invalidInputIsAskedAgain(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(invalidInputMessages(out)).hasSize(4);
		assertThat(out.capturedString()).endsWith(X_WINS);
	}

	// E2E-05: a field that is already taken (by O or by X itself) is asked again
	@Test
	@StdIo({ "3", "0", "3", "4", "5" })
	void takenFieldIsAskedAgain(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(invalidInputMessages(out)).hasSize(2);
		assertThat(out.capturedString()).endsWith(X_WINS);
	}

	// E2E-06: malicious input (terminal control characters, shell command, format string)
	// is rejected and not printed back to the terminal
	@Test
	@StdIo({ "\u001b[2J4", "4; rm -rf /", "%s%n", "3", "4", "5" })
	void maliciousInputIsRejectedAndNotPrinted(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(out.capturedString())
				.doesNotContain("\u001b[2J", "rm -rf", "%s")
				.endsWith(X_WINS);
	}

	// E2E-07: the input ends in the middle of the game (e.g. Ctrl+D): the game must stop with a
	// message, not wait forever for the next line and not crash with a stack trace
	@Test
	@StdIo({ "abc" })
	void endOfInputStopsTheGame(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(out.capturedString()).endsWith("game aborted: no more input\n");
	}

	// E2E-09: endless invalid input (e.g. "yes abc | java -jar ...") must not keep the game busy
	// forever: after 5 invalid inputs for the same move the game stops
	@Test
	@StdIo({ "a", "b", "c", "d", "e", "f", "g" })
	void endlessInvalidInputStopsTheGame(StdOut out) {
		TicTacToeMain.main(NO_ARGS);

		assertThat(invalidInputMessages(out)).hasSize(5);
		assertThat(out.capturedString()).endsWith("game aborted: too many invalid inputs\n");
	}

	// E2E-08: the perfect player calculates all possible games (minimax), that must not block
	// the game: a complete game of two perfect players has to be finished within one second
	@Test
	@StdIo
	@Timeout(value = 1, threadMode = ThreadMode.SEPARATE_THREAD)
	void twoPerfectPlayersFinishWithinOneSecond(StdOut out) {
		assertThat(TicTacToeMain.play(new PerfectPlayer(), new PerfectPlayer())).isNull();
		assertThat(out.capturedString()).endsWith("it's a draw!\n");
	}

	private static List<String> invalidInputMessages(StdOut out) {
		return Arrays.stream(out.capturedLines()).filter(line -> line.startsWith("invalid input")).toList();
	}
}
