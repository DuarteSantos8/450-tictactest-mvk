package ch.bbw.m450.tictactoe.players;

import java.util.Arrays;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

class GreedyPlayerTest implements WithAssertions {

	@Test
	void given_aFullBoard_when_theGreedyPlayerPlays_then_throwsIllegalStateException() {
		var board = new Stone[TicTacToeMain.BOARD_SIZE];
		Arrays.fill(board, Stone.CROSS);
		var player = new GreedyPlayer();

		assertThatThrownBy(() -> player.play(board, Stone.CIRCLE))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play at all");
	}
}
