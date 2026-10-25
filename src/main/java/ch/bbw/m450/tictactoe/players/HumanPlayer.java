package ch.bbw.m450.tictactoe.players;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Simple human-player taking input from stdin.
 */
public class HumanPlayer implements TicTacToePlayer {

	// one shared scanner: creating a new one per turn would drop input already buffered by the previous one
	private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		System.out.println(TicTacToeMain.toString(board) + "where to put the next " + colorToPlay + "? (0-8): ");
		while (true) {
			var input = scanner.nextLine().trim();
			// only a single digit 0-8 on a free field is a valid move, everything else is asked again
			if (input.matches("[0-8]") && board[Integer.parseInt(input)] == null) {
				return Integer.parseInt(input);
			}
			// the input is not printed again, so no strange characters end up in the terminal
			System.out.println("invalid input, please enter the number of a free field (0-8): ");
		}
	}
}
