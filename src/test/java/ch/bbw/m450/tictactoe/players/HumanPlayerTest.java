package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.Boards.emptyBoard;
import static ch.bbw.m450.tictactoe.Boards.toBoard;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * StdIn/StdOut tests with Pioneer: {@code @StdIo} replaces the keyboard input and catches the
 * console output.
 */
class HumanPlayerTest implements WithAssertions {

	@Test
	@StdIo(" 4 ") // spaces around the number are ignored
	void readsTheNumberAndIgnoresSpaces(StdOut out) {
		var player = new HumanPlayer();

		var move = player.play(emptyBoard(), Stone.CROSS);

		assertThat(move).isEqualTo(4);
		assertThat(out.capturedString()).contains("where to put the next CROSS? (0-8): ");
	}

	@Test
	@StdIo({ "2", "7" })
	void readsOneLinePerMove() {
		var player = new HumanPlayer();
		var board = emptyBoard();

		var firstMove = player.play(board, Stone.CIRCLE);
		var secondMove = player.play(board, Stone.CIRCLE);

		assertThat(firstMove).isEqualTo(2);
		assertThat(secondMove).isEqualTo(7);
	}

	@Test
	@StdIo({ "abc", "", "9", "-1", "4" }) // no number, empty, outside the board, outside the board, valid
	void asksAgainUntilTheInputIsAFieldOnTheBoard(StdOut out) {
		var player = new HumanPlayer();

		assertThat(player.play(emptyBoard(), Stone.CROSS)).isEqualTo(4);
		assertThat(out.capturedLines()).filteredOn(line -> line.startsWith("invalid input")).hasSize(4);
	}

	@Test
	@StdIo({ "0", "1" })
	void asksAgainWhenTheFieldIsAlreadyTaken() {
		var player = new HumanPlayer();

		assertThat(player.play(toBoard("X.. ... ..."), Stone.CIRCLE)).isEqualTo(1);
	}
}
