package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.Boards.emptyBoard;
import static ch.bbw.m450.tictactoe.Boards.toBoard;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * Single moves of the perfect player. Complete games are in {@code TicTacToeMainTest} and
 * {@link PerfectPlayerPropertiesTest}.
 */
class PerfectPlayerTest implements WithAssertions {

	private final PerfectPlayer perfectPlayer = new PerfectPlayer();

	// the opponent (X) has two in a line, the perfect player (O) must block the third field
	@ParameterizedTest(name = "\"{0}\" -> O blocks {1}")
	@CsvSource({
			"XX. .O. ..., 2",
			"X.. .O. X.., 3",
			"..X .O. ..X, 5" })
	void blocksTheOpponent(String pattern, int expectedMove) {
		assertThat(perfectPlayer.play(toBoard(pattern), Stone.CIRCLE)).isEqualTo(expectedMove);
	}

	// the perfect player (O) can win right now: winning is better than blocking X
	@ParameterizedTest(name = "\"{0}\" -> O wins with {1}")
	@CsvSource({
			"OO. XX. X.., 2",
			"X.. OO. XX., 5" })
	void winsWhenItCan(String pattern, int expectedMove) {
		assertThat(perfectPlayer.play(toBoard(pattern), Stone.CIRCLE)).isEqualTo(expectedMove);
	}

	@Test
	void takesTheFirstFieldWhenAllMovesAreEquallyGood() {
		// on an empty board every move leads to a draw, so the first free field is chosen
		assertThat(perfectPlayer.play(emptyBoard(), Stone.CROSS)).isZero();
	}
}
