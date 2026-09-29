package ch.bbw.m450.tictactoe;

import static ch.bbw.m450.tictactoe.Boards.toBoard;

import java.util.stream.Stream;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

	@Test
	@StdIo
	void given_twoPerfectPlayers_when_aGameIsPlayed_then_itIsADraw(StdOut out) {
		var winner = TicTacToeMain.play(new PerfectPlayer(), new PerfectPlayer());

		assertThat(winner).isNull();
		assertThat(out.capturedLines()).contains("it's a draw!");
	}

	@ParameterizedTest(name = "O plays to {0}")
	@ValueSource(ints = { -1, 9, 0 }) // outside the board, outside the board, already taken by X
	@StdIo
	void given_aPlayerWithAnInvalidMove_when_aGameIsPlayed_then_throwsIllegalStateException(int invalidMove,
			StdOut out) {
		TicTacToePlayer cheatingPlayer = (board, color) -> invalidMove;

		assertThatThrownBy(() -> TicTacToeMain.play(xPlayer, cheatingPlayer))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play to position " + invalidMove);
		assertThat(out.capturedString()).contains("X"); // the board is printed before the error
	}

	@Test
	void given_aBoard_when_toStringIsCalled_then_showsTheStonesAndTheFreeFieldNumbers() {
		var board = toBoard("X.. .O. ...");

		var text = TicTacToeMain.toString(board);

		// remove the color codes (like \033[1m) so only the visible text is left
		var visibleText = text.replaceAll("\033\\[[0-9;]*m", "");
		assertThat(visibleText).isEqualTo("""
				X  1  2 \s
				3  O  5 \s
				6  7  8 \s
				""");
	}

	@Test
	void given_aWrongBoard_when_theHelperIsUsed_then_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> toBoard("XX")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> toBoard("XXX ... ..A")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void given_aStone_when_opponentIsCalled_then_returnsTheOtherStone() {
		assertThat(Stone.CROSS.opponent()).isEqualTo(Stone.CIRCLE);
		assertThat(Stone.CIRCLE.opponent()).isEqualTo(Stone.CROSS);
	}

	@Test
	@StdIo({ "3", "4", "5" }) // the human (X) plays the middle row, the greedy player only gets 0 and 1
	void given_humanInput_when_mainIsStarted_then_theHumanWins(StdOut out) {
		TicTacToeMain.main(new String[0]);

		assertThat(out.capturedString()).contains("...and the winner is: CROSS");
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
}
