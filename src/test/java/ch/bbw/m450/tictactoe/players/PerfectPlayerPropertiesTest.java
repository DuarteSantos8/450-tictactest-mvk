package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.Boards.emptyBoard;

import java.util.List;

import org.assertj.core.api.WithAssertions;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * Additional testing framework: jqwik (property-based testing). Instead of fixed examples,
 * jqwik generates many inputs and checks that a rule (property) is always true.
 */
class PerfectPlayerPropertiesTest implements WithAssertions {

	/**
	 * Rule: the perfect player never loses, no matter if it starts or not and which fields the
	 * opponent chooses. jqwik generates the order in which the opponent tries the fields.
	 */
	@Property(tries = 100)
	void thePerfectPlayerNeverLoses(@ForAll("fieldOrders") List<Integer> opponentFieldOrder,
			@ForAll boolean perfectPlayerStarts) {
		TicTacToePlayer opponent = (board, color) -> firstFreeField(board, opponentFieldOrder);
		var perfectPlayer = new PerfectPlayer();

		var winner = perfectPlayerStarts
				? TicTacToeMain.play(perfectPlayer, opponent)
				: TicTacToeMain.play(opponent, perfectPlayer);

		var opponentColor = perfectPlayerStarts ? Stone.CIRCLE : Stone.CROSS;
		assertThat(winner).isNotEqualTo(opponentColor);
	}

	/**
	 * Rule: the perfect player always chooses a free field on the board.
	 */
	@Property(tries = 50)
	void thePerfectPlayerAlwaysPlaysAFreeField(@ForAll @IntRange(min = 0, max = 8) int takenField) {
		var board = emptyBoard();
		board[takenField] = Stone.CROSS;

		var move = new PerfectPlayer().play(board, Stone.CIRCLE);

		assertThat(board[move]).isNull();
	}

	/**
	 * All nine fields in a random order, e.g. [4, 0, 8, 2, ...].
	 */
	@Provide
	Arbitrary<List<Integer>> fieldOrders() {
		return Arbitraries.shuffle(0, 1, 2, 3, 4, 5, 6, 7, 8);
	}

	private static int firstFreeField(Stone[] board, List<Integer> fieldOrder) {
		return fieldOrder.stream()
				.filter(field -> board[field] == null)
				.findFirst()
				.orElseThrow();
	}
}
