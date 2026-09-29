package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.Boards.toBoard;

import java.util.Arrays;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

class PerfectPlayerTest implements WithAssertions {

	private final PerfectPlayer perfectPlayer = new PerfectPlayer();

	@Test
	void given_theGreedyPlayerStarts_when_aGameIsPlayed_then_thePerfectPlayerWins() {
		var winner = TicTacToeMain.play(new GreedyPlayer(), perfectPlayer);

		assertThat(winner).isEqualTo(Stone.CIRCLE);
	}

	@Test
	void given_thePerfectPlayerStarts_when_aGameIsPlayed_then_thePerfectPlayerWins() {
		var winner = TicTacToeMain.play(perfectPlayer, new GreedyPlayer());

		assertThat(winner).isEqualTo(Stone.CROSS);
	}

	// the opponent (X) has two in a line, the perfect player (O) must block the third field
	@ParameterizedTest(name = "\"{0}\" -> O blocks {1}")
	@CsvSource({
			"XX. .O. ..., 2",
			"X.. .O. X.., 3",
			"..X .O. ..X, 5" })
	void given_theOpponentCanWin_when_thePerfectPlayerPlays_then_itBlocks(String pattern, int expectedMove) {
		var move = perfectPlayer.play(toBoard(pattern), Stone.CIRCLE);

		assertThat(move).isEqualTo(expectedMove);
	}

	@Test
	void given_aFullBoard_when_thePerfectPlayerPlays_then_throwsIllegalStateException() {
		var board = new Stone[TicTacToeMain.BOARD_SIZE];
		Arrays.fill(board, Stone.CROSS);

		assertThatThrownBy(() -> perfectPlayer.play(board, Stone.CIRCLE))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play at all");
	}
}
