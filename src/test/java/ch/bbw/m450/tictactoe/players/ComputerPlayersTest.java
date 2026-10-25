package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.Boards.toBoard;
import static org.junit.jupiter.api.Named.named;

import java.util.stream.Stream;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import ch.bbw.m450.tictactoe.TicTacToePlayer;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * Tests that are the same for every computer player, plus the moves of the greedy player.
 */
class ComputerPlayersTest implements WithAssertions {

	@ParameterizedTest(name = "{0}")
	@MethodSource("computerPlayers")
	void throwsOnAFullBoard(TicTacToePlayer player) {
		var fullBoard = toBoard("XXX XXX XXX");

		assertThatThrownBy(() -> player.play(fullBoard, Stone.CIRCLE))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("cannot play at all");
	}

	// the greedy player always takes the top-most-left free field
	@ParameterizedTest(name = "\"{0}\" -> {1}")
	@CsvSource({
			"... ... ..., 0",
			"XO. ... ..., 2",
			"XOX OXO XO., 8" })
	void greedyPlayerTakesTheFirstFreeField(String pattern, int expectedMove) {
		assertThat(new GreedyPlayer().play(toBoard(pattern), Stone.CROSS)).isEqualTo(expectedMove);
	}

	private static Stream<Named<TicTacToePlayer>> computerPlayers() {
		return Stream.of(
				named("GreedyPlayer", new GreedyPlayer()),
				named("PerfectPlayer", new PerfectPlayer()));
	}
}
