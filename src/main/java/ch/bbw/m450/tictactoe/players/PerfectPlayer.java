package ch.bbw.m450.tictactoe.players;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Perfect player using the minimax algorithm: it tries every possible move until the end of the
 * game and picks the best one. It never loses, against a perfect opponent it's always a draw.
 */
public class PerfectPlayer implements TicTacToePlayer {

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		var bestMove = -1;
		var bestScore = Integer.MIN_VALUE;
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			if (board[i] == null) {
				board[i] = colorToPlay; // try the move
				var score = -minimax(board, colorToPlay.opponent());
				board[i] = null; // undo the move
				if (score > bestScore) {
					bestScore = score;
					bestMove = i;
				}
			}
		}
		if (bestMove == -1) {
			throw new IllegalStateException("cannot play at all");
		}
		return bestMove;
	}

	/**
	 * @return the score of the board from the view of colorToPlay: 1 = win, 0 = draw, -1 = loss
	 */
	private int minimax(Stone[] board, Stone colorToPlay) {
		if (TicTacToeMain.isWin(board, colorToPlay.opponent())) {
			return -1; // the opponent has just won with the last move
		}
		var bestScore = Integer.MIN_VALUE;
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			if (board[i] == null) {
				board[i] = colorToPlay;
				bestScore = Math.max(bestScore, -minimax(board, colorToPlay.opponent()));
				board[i] = null;
			}
		}
		// no free field left and nobody won -> draw
		return bestScore == Integer.MIN_VALUE ? 0 : bestScore;
	}
}
