package ch.bbw.m450.tictactoe;

import static ch.bbw.m450.tictactoe.Boards.toBoard;
import static org.junit.jupiter.api.Named.named;

import java.util.stream.Stream;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import ch.bbw.m450.tictactoe.players.PerfectPlayer;

/**
 * Test-suite for the tic-tac-toe engine. Uses AssertJ via the {@link WithAssertions}
 * entry-point so the IDE offers {@code assertThat(...)} completions out of the box.
 */
class TicTacToeMainTest implements WithAssertions {

	// board fixtures, one per winning line: X = cross, O = circle, . = empty field,
	// the spaces only separate the three rows
	private static final String TOP_ROW_X_WINS = "XXX ... ...";
	private static final String MID_ROW_O_WINS = "... OOO ...";
	private static final String BOTTOM_ROW_X_WINS = "... ... XXX";
	private static final String LEFT_COL_O_WINS = "O.. O.. O..";
	private static final String MID_COL_X_WINS = ".X. .X. .X.";
	private static final String RIGHT_COL_O_WINS = "..O ..O ..O";
	private static final String DIAGONAL_X_WINS = "XOO OX. XOX";
	private static final String ANTI_DIAGONAL_O_WINS = "..O .O. O..";

	// board fixtures without a win for the color that gets checked
	private static final String EMPTY_BOARD = "... ... ...";
	private static final String DRAW_BOARD = "XOX XXO OXO";
	private static final String TOP_ROW_O_WINS = "OOO XX. .X.";

	private GreedyPlayer xPlayer;

	private GreedyPlayer oPlayer;

	/**
	 * Fixture: every test starts with two fresh players, so no test can be influenced by a
	 * game another test has played before.
	 */
	@BeforeEach
	void setUp() {
		xPlayer = new GreedyPlayer();
		oPlayer = new GreedyPlayer();
	}

	@ParameterizedTest(name = "{1} on \"{0}\" -> {2}")
	@MethodSource("boardConstellations")
	void isWinDetectsALineOnlyForItsOwnColor(String pattern, Stone color, boolean expectedToWin) {
		assertThat(TicTacToeMain.isWin(toBoard(pattern), color)).isEqualTo(expectedToWin);
	}

	@ParameterizedTest(name = "{0} vs {1} -> {2}")
	@MethodSource("games")
	@StdIo
	void playReturnsTheWinnerAndPrintsTheResult(TicTacToePlayer x, TicTacToePlayer o, Stone expectedWinner,
			String expectedOutput, StdOut out) {
		assertThat(TicTacToeMain.play(x, o)).isEqualTo(expectedWinner);
		assertThat(out.capturedString()).contains(expectedOutput);
	}

	@Test
	void playRejectsTheSamePlayerTwice() {
		assertThatThrownBy(() -> TicTacToeMain.play(xPlayer, xPlayer))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("players must differ");
	}

	@ParameterizedTest(name = "O plays to {0}")
	@ValueSource(ints = { -1, 9, 0 }) // outside the board, outside the board, already taken by X
	@StdIo
	void playRejectsAnInvalidMove(int invalidMove, StdOut out) {
		TicTacToePlayer cheatingPlayer = (board, color) -> invalidMove;

		assertThatThrownBy(() -> TicTacToeMain.play(xPlayer, cheatingPlayer))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play to position " + invalidMove);
		// before the error the board is printed: X (greedy) has already played to field 0
		assertThat(withoutColors(out.capturedString())).startsWith("X  1  2");
	}

	@Test
	void toStringShowsTheStonesAndTheFreeFieldNumbers() {
		var text = TicTacToeMain.toString(toBoard("X.. .O. ..."));

		assertThat(withoutColors(text)).isEqualTo("""
				X  1  2 \s
				3  O  5 \s
				6  7  8 \s
				""");
	}

	@ParameterizedTest
	@ValueSource(strings = { "XX", "XXX ... ..A" }) // too short, unknown character
	void toBoardRejectsAnInvalidPattern(String pattern) {
		assertThatThrownBy(() -> toBoard(pattern)).isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({ "CROSS, CIRCLE", "CIRCLE, CROSS" })
	void opponentReturnsTheOtherStone(Stone stone, Stone expectedOpponent) {
		assertThat(stone.opponent()).isEqualTo(expectedOpponent);
	}

	@Test
	@StdIo({ "3", "4", "5" }) // the human (X) plays the middle row, the greedy player only gets 0 and 1
	void mainLetsTheHumanWinWithTheMiddleRow(StdOut out) {
		TicTacToeMain.main(new String[0]);

		assertThat(out.capturedString()).contains("...and the winner is: CROSS");
	}

	@Test
	// only an empty line, then the input ends. A plain @StdIo without values would not replace
	// System.in at all, the test would then wait for the real keyboard (e.g. in PITest)
	@StdIo("")
	void mainStopsWithAMessageInsteadOfAStackTrace(StdOut out) {
		TicTacToeMain.main(new String[0]);

		assertThat(out.capturedString()).endsWith("game aborted: no more input\n");
	}

	/**
	 * Removes the color codes (like \033[1m) so only the visible text is left.
	 */
	private static String withoutColors(String text) {
		return text.replaceAll("\033\\[[\\d;]*m", "");
	}

	/**
	 * The board constellations for the isWin test, each one as pattern / color to check /
	 * expected result.
	 */
	private static Stream<Arguments> boardConstellations() {
		return Stream.of(
				// all eight winning lines, checked for the color that completes them
				Arguments.of(TOP_ROW_X_WINS, Stone.CROSS, true),
				Arguments.of(MID_ROW_O_WINS, Stone.CIRCLE, true),
				Arguments.of(BOTTOM_ROW_X_WINS, Stone.CROSS, true),
				Arguments.of(LEFT_COL_O_WINS, Stone.CIRCLE, true),
				Arguments.of(MID_COL_X_WINS, Stone.CROSS, true),
				Arguments.of(RIGHT_COL_O_WINS, Stone.CIRCLE, true),
				Arguments.of(DIAGONAL_X_WINS, Stone.CROSS, true),
				Arguments.of(ANTI_DIAGONAL_O_WINS, Stone.CIRCLE, true),
				// nobody has three in a line
				Arguments.of(EMPTY_BOARD, Stone.CROSS, false),
				Arguments.of(DRAW_BOARD, Stone.CROSS, false),
				Arguments.of(DRAW_BOARD, Stone.CIRCLE, false),
				// a line only wins for its own color
				Arguments.of(TOP_ROW_O_WINS, Stone.CROSS, false));
	}

	/**
	 * Complete games as X player / O player / expected winner (null = draw) / expected output.
	 */
	private static Stream<Arguments> games() {
		return Stream.of(
				// both greedy players fill the top-most-left free field, X completes the 0-4-8 diagonal
				Arguments.of(named("Greedy", new GreedyPlayer()), named("Greedy", new GreedyPlayer()),
						Stone.CROSS, "...and the winner is: CROSS"),
				// the perfect player wins against the greedy player, no matter who starts
				Arguments.of(named("Perfect", new PerfectPlayer()), named("Greedy", new GreedyPlayer()),
						Stone.CROSS, "...and the winner is: CROSS"),
				Arguments.of(named("Greedy", new GreedyPlayer()), named("Perfect", new PerfectPlayer()),
						Stone.CIRCLE, "...and the winner is: CIRCLE"),
				// two perfect players always play a draw
				Arguments.of(named("Perfect", new PerfectPlayer()), named("Perfect", new PerfectPlayer()),
						null, "it's a draw!"));
	}
}
