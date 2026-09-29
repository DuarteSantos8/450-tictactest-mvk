package ch.bbw.m450.tictactoe.players;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * StdIn/StdOut tests with Pioneer: {@code @StdIo} replaces the keyboard input and catches the
 * console output.
 */
class HumanPlayerTest implements WithAssertions {

	@Test
	@StdIo(" 4 ") // spaces around the number are ignored
	void given_theInput4_when_theHumanPlays_then_returns4(StdOut out) {
		var player = new HumanPlayer();

		var move = player.play(new Stone[TicTacToeMain.BOARD_SIZE], Stone.CROSS);

		assertThat(move).isEqualTo(4);
		assertThat(out.capturedString()).contains("where to to put the next CROSS? (0-8): ");
	}

	@Test
	@StdIo({ "2", "7" })
	void given_twoInputs_when_theHumanPlaysTwice_then_bothInputsAreRead() {
		var player = new HumanPlayer();
		var board = new Stone[TicTacToeMain.BOARD_SIZE];

		var firstMove = player.play(board, Stone.CIRCLE);
		var secondMove = player.play(board, Stone.CIRCLE);

		assertThat(firstMove).isEqualTo(2);
		assertThat(secondMove).isEqualTo(7);
	}

	@Test
	@StdIo("abc")
	void given_noNumber_when_theHumanPlays_then_throwsNumberFormatException() {
		var player = new HumanPlayer();

		assertThatThrownBy(() -> player.play(new Stone[TicTacToeMain.BOARD_SIZE], Stone.CROSS))
				.isInstanceOf(NumberFormatException.class);
	}
}
