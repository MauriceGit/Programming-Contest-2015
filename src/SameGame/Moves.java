package SameGame;

import java.util.LinkedList;

public class Moves {
	
	private LinkedList<Move> moves = new LinkedList<Move>();
	
	public int getBestScore() {
		return moves.getFirst().getScore();
	}
	
	public synchronized boolean submitAndPrintMove(Move move, int penalty, double startTime) {		
		if (moves.size() == 0 || (move.getScore() + penalty) > moves.getFirst().getScore()) {
			double time = ((int)(System.currentTimeMillis()-startTime)/100)/10;
			//System.out.print(move.getScore() + penalty + " - " + time + " - ");
			//System.out.print(moves.size() == 0 ? 0 : getBestScore());
			System.out.println(move);
			System.out.flush();
			Move clone = move.cloneMove();
			clone.setScore(move.getScore()+penalty);
			moves.add(0, clone);
			return true;
		}
		return false;
	}
	
}
