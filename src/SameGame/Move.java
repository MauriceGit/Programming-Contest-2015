package SameGame;

import java.util.LinkedList;

public class Move {
	
	private int score = 0;
	private LinkedList<Tuple> move = null;
	
	public Move(int score, LinkedList<Tuple> move) {
		this.score = score;
		this.move = move;
	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	public void appendTuple(Tuple tuple, int tmpScore) {
		move.addLast(tuple);
		score += tmpScore;
	}
	
	public void resetScore() {
		score = 0;
	}
	
	public Move cloneMove() {
		LinkedList<Tuple> newList = new LinkedList<Tuple>();
		//newList.add(new Tuple(-1, -1));
		return new Move(score, (LinkedList<Tuple>) move.clone());
	}

	@Override
	public String toString() {
		
		if (move.size() == 0) {
			return "[]";
		}
		
		String res = "";
		Tuple last = move.getLast();
		
		for (Tuple tuple : move) {
			res += tuple;
			if (tuple != last)
				res += ", ";			
		}
		
		return "[" + res + "]";
	}
	
	
}
