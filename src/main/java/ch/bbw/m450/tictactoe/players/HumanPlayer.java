package ch.bbw.m450.tictactoe.players;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Simple human-player taking input from stdin.
 */
public class HumanPlayer implements TicTacToePlayer {

	// at most this many inputs per move, if none is valid the player gives up (no endless loop with endless input)
	private static final int MAX_INPUTS = 5;

	// a valid input is exactly one digit 0-8
	private static final Pattern FIELD_NUMBER = Pattern.compile("[0-8]");

	// one shared scanner: creating a new one per turn would drop input already buffered by the previous one
	private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		System.out.println(TicTacToeMain.toString(board) + "where to put the next " + colorToPlay + "? (0-8): ");
		for (var attempt = 1; attempt <= MAX_INPUTS; attempt++) {
			if (!scanner.hasNextLine()) {
				throw new IllegalStateException("no more input");
			}
			var input = scanner.nextLine().trim();
			if (FIELD_NUMBER.matcher(input).matches() && board[Integer.parseInt(input)] == null) {
				return Integer.parseInt(input);
			}
			// the input is not printed again, so no strange characters end up in the terminal
			System.out.println("invalid input, please enter the number of a free field (0-8): ");
		}
		throw new IllegalStateException("too many invalid inputs");
	}
}
