package ch.bbw.m450.tictactoe.players;

import java.util.Random;

import org.assertj.core.api.WithAssertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * Additional testing framework: jqwik (property-based testing). Instead of fixed examples,
 * jqwik generates many random inputs and checks that a rule (property) is always true.
 */
class PerfectPlayerProperties implements WithAssertions {

	/**
	 * Rule: the perfect player never loses, no matter which fields the opponent chooses.
	 * The opponent plays randomly: jqwik generates the random numbers.
	 */
	@Property(tries = 50)
	boolean thePerfectPlayerNeverLoses(@ForAll @IntRange(min = 0, max = 1000) int seed) {
		var random = new Random(seed);
		TicTacToePlayer randomPlayer = (board, color) -> {
			int move;
			do {
				move = random.nextInt(TicTacToeMain.BOARD_SIZE);
			} while (board[move] != null);
			return move;
		};

		var winner = TicTacToeMain.play(randomPlayer, new PerfectPlayer());

		return winner != Stone.CROSS; // the random player (X) must never win
	}

	/**
	 * Rule: the perfect player always chooses a free field on the board.
	 */
	@Property(tries = 50)
	void thePerfectPlayerAlwaysPlaysAFreeField(@ForAll @IntRange(min = 0, max = 8) int takenField) {
		var board = new Stone[TicTacToeMain.BOARD_SIZE];
		board[takenField] = Stone.CROSS;

		var move = new PerfectPlayer().play(board, Stone.CIRCLE);

		assertThat(board[move]).isNull();
	}
}
