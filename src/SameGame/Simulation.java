package SameGame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Simulation implements Runnable{

	public enum Strategy {
	    TABU_COLOR_RANDOM, 
	    TABU_RANDOM,
	    CONNECTION_MAXIMIZATION,
	    RANDOM
	}
	
	private Thread ownThread;
	private int result = 0;
	private static Random rand = new Random();
	private Strategy strategy;
	
	//
	// TABU_COLOR_RANDOM, TABU_RANDOM:
	//
	private int dominatingColor = 0;
	
	
	private Board board;
	private int maxTests;
	private int moveScore;
	private Tuple move;
	private boolean sort;
	
	// Konstruktor für eine parallele Simulation!
	public Simulation(Board board, int maxTests, int moveScore, Tuple move, Strategy strategy, boolean sort) {
		this.board = board;
		this.maxTests = maxTests;
		this.moveScore = moveScore;
		this.move = move;
		this.strategy = strategy;
		this.sort = sort;
	}
	
	// Konstruktor, wenn die Simulation nicht parallel ausgeführt wird.
	public Simulation(Strategy strategy) {
		this.strategy = strategy;
		//initializeStrategy();
	}
	
	public void init(Board board) {
		this.board = board;
		initializeStrategy();
	}
	
	private void initializeStrategy () {
		switch(strategy) {
		case TABU_COLOR_RANDOM:
			dominatingColor = board.getMostFrequentColor();
			break;
		case TABU_RANDOM:
			HashMap<Integer, Integer> tmp = board.getColors();
			int color = (Integer)tmp.keySet().toArray()[rand.nextInt(tmp.size())];
			dominatingColor = color;
			break;
		}
	}
	
	private int evaluateBoard(Board board) {
		
		int res = 0;
		
		int groupSize = board.getBiggestGroupSize()-1;
		
		return groupSize*groupSize;
	}
	
	private int evaluateColorCount(Board board) {
		int diff = board.getInitColorCount() - board.getColors().size();
		return (diff+1)*(diff+1);
	}
	
	private int minimizeGroupCountEval(Board board) {
		int groupCount = board.getGroupCount();
		
		return board.getSize().x*board.getSize().y - groupCount;
		
	}
	
	private int connectionMaximizationEvaluation(Board board) {
		
		int res = 0;
		
		if ((board.getSize().x + board.getSize().y) > 50) {
			// Großes Feld
			res += 1.0 * evaluateBoard(board);
			res += 1.5 * board.calcColorConnections();
			res += 160 * minimizeGroupCountEval(board);
		} else {
			// Kleines Feld
			res += 1.0 * evaluateBoard(board);
			res += 0.5 * board.calcColorConnections();
			res += 220 * minimizeGroupCountEval(board);
		}
		
		return res;		
	}
	
	public int simulation(Board board, int maxTests, boolean sort) {
		
		if (board.isEmpty()) {
			return 0;
		}
		
		if (maxTests == 0) {
			return connectionMaximizationEvaluation(board);
		}
		
		LinkedList<Tuple> allMoves = board.getAllPossibleMoves();
		
		if (allMoves.size() == 0) {
			//return board.calculatePenalty();
			return 0;
		}
		
		int moveCount = allMoves.size();
		int avgScore = 0;
		int bestScore = 0;
		Tuple fieldSize = board.getSize();
		
		//
		// 1-Ply-Search, wieviele Gruppenbildungen entstanden sind. Je größer, desto besser.
		//
		if (sort) {
			HashMap<Integer, Tuple> movePoints = new HashMap<Integer, Tuple>();
			
			for (Tuple move : allMoves) {
				Board tmpBoard = board.cloneBoard();
				int before;
				//= tmpBoard.calcColorConnections();
				before = connectionMaximizationEvaluation(tmpBoard);
				tmpBoard.applyMoveAt(move);
				int after;
				//= tmpBoard.calcColorConnections();
				after = connectionMaximizationEvaluation(tmpBoard);
				//System.out.println(after-before);
				int value = after-before;
				
				while (movePoints.containsKey(value)) {
					value++;
				}
				movePoints.put(value, move);
			}
			
			SortedSet<Integer> sortedKeys = new TreeSet<Integer>(movePoints.keySet());
			allMoves.clear();
			for (int key : sortedKeys) {
				allMoves.addFirst(movePoints.get(key));
			}
		}
		
		int testMovesCount = 0;
		for (Tuple move : allMoves) {
			
			if (testMovesCount >= 2*allMoves.size()/3) {
				continue;
			}
			
			int testCount = maxTests/moveCount;
			int tmpScore = board.getMoveScore(move);
			
			//
			// Die dominierende Farbe wird generell in 60% der Fälle nicht gespielt!
			//
			if (true) {
				int percent = 99;
				if (board.getColorAt(move) == dominatingColor && rand.nextInt(100) >= 100-percent) {
					continue;
				}
			}
			
			Board tmpBoard = board.cloneBoard();
			tmpBoard.applyMoveAt(move);
			
			int bonus = simulation(tmpBoard, testCount, sort);

			avgScore += (tmpScore + bonus) / moveCount;
			
			if (bonus > bestScore) {
				bestScore = bonus; 
			}
			testMovesCount++;
		}
		//System.out.println(score);
		return bestScore;
		
	}
	
	@Override
	public void run() {
		initializeStrategy();
		result = simulation(board, maxTests, sort);
	}
	
	public void newThread() {
		this.ownThread = new Thread(this);
	}

	public Thread getThread() {
		return this.ownThread;
	}
	
	public int getResult() {
		return result;
	}
	
	public Tuple getMove() {
		return move;
	}
	
	public int getMoveScore() {
		return moveScore;
	}
	
}
