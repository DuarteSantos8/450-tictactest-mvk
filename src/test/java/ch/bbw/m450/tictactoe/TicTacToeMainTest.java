package ch.bbw.m450.tictactoe;

import static ch.bbw.m450.tictactoe.TicTacToeMain.isWin;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

/**
 * Test-suite for the tic-tac-toe engine. Uses AssertJ via the {@link WithAssertions}
 * entry-point so the IDE offers {@code assertThat(...)} completions out of the box.
 */
class TicTacToeMainTest implements WithAssertions {

	@Test
	void isWinningDiagonalForX() {
		assertThat(isWin(toBoard("XOO OX. XOX"), Stone.CROSS)).isTrue();
	}

	@Test
	void isWinningTopRowForO() {
		assertThat(isWin(toBoard("OOO XX. .X."), Stone.CIRCLE)).isTrue();
	}

	@Test
	void emptyBoardIsNoWin() {
		assertThat(isWin(toBoard("... ... ..."), Stone.CROSS)).isFalse();
	}

	@Test
	void aRowOnlyWinsForItsOwnColor() {
		assertThat(isWin(toBoard("OOO XX. .X."), Stone.CROSS)).isFalse();
	}

	/**
	 * The same as {@link #isWinningDiagonalForX()}, written out in the given-when-then style.
	 * Readable, but the one-liner above usually says the same thing with less ceremony.
	 */
	@Test
	void givenADiagonal_whenCheckingX_thenItWins() {
		// given
		var boardWithDiagonal = toBoard("XOO OX. XOX");
		// when
		var winning = isWin(boardWithDiagonal, Stone.CROSS);
		// then
		assertThat(winning).isTrue();
	}

	@Test
	void twoGreedyPlayersLetTheStartingPlayerWin() {
		// both always fill the top-most-left free field, which lets X complete the 0-4-8 diagonal
		assertThat(TicTacToeMain.play(new GreedyPlayer(), new GreedyPlayer())).isEqualTo(Stone.CROSS);
	}

	@Test
	void theSamePlayerInstanceTwiceIsRejected() {
		var greedy = new GreedyPlayer();
		assertThatThrownBy(() -> TicTacToeMain.play(greedy, greedy))
				.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test-only helper turning a compact pattern like {@code "XOO OX. XOX"} into a board.
	 * {@code X} = cross, {@code O} = circle, {@code .} = empty; spaces separate rows and are ignored.
	 * Lives in the test-scope because it is only useful for writing readable tests.
	 */
	static Stone[] toBoard(String pattern) {
		var fields = pattern.replace(" ", "");
		if (fields.length() != TicTacToeMain.BOARD_SIZE) {
			throw new IllegalArgumentException("a board needs exactly 9 fields, but got: " + fields);
		}
		var board = new Stone[TicTacToeMain.BOARD_SIZE];
		for (var i = 0; i < board.length; i++) {
			board[i] = switch (fields.charAt(i)) {
				case 'X' -> Stone.CROSS;
				case 'O' -> Stone.CIRCLE;
				case '.' -> null;
				default -> throw new IllegalArgumentException("unexpected field: " + fields.charAt(i));
			};
		}
		return board;
	}
}
