package ch.bbw.m450.tictactoe;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;

/**
 * Test helper used by all test classes. Turns a compact pattern like {@code "XOO OX. XOX"} into
 * the board that the game expects: X = cross, O = circle, . = empty field, the spaces only
 * separate the three rows.
 */
public final class Boards {

	private Boards() {
		// only static helper methods
	}

	public static Stone[] toBoard(String pattern) {
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
