package ch.bbw.m450.tictactoe;

import java.util.stream.Stream;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;

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
	void given_aBoard_when_isWinIsChecked_then_returnsWhetherThatColorHasALine(String pattern, Stone color,
			boolean expectedToWin) {
		var board = toBoard(pattern);

		var winning = TicTacToeMain.isWin(board, color);

		assertThat(winning).isEqualTo(expectedToWin);
	}

	@Test
	void given_twoGreedyPlayers_when_aGameIsPlayed_then_theStartingPlayerWins() {
		// both always fill the top-most-left free field, which lets X complete the 0-4-8 diagonal
		assertThat(TicTacToeMain.play(xPlayer, oPlayer)).isEqualTo(Stone.CROSS);
	}

	@Test
	void given_theSamePlayerTwice_when_aGameIsStarted_then_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> TicTacToeMain.play(xPlayer, xPlayer))
				.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * The board constellations for the parameterized test above, each one as
	 * pattern / color to check / expected result.
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
	 * Helper turning a compact pattern like {@code "XOO OX. XOX"} into the board that
	 * {@link TicTacToeMain#isWin} expects. Lives in the test-scope because it is only useful
	 * for writing readable tests.
	 */
	private static Stone[] toBoard(String pattern) {
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
