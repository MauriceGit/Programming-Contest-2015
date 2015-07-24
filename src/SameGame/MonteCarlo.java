package SameGame;

import java.util.LinkedList;
import java.util.Random;

import SameGame.Simulation.Strategy;

public class MonteCarlo {
	
	public enum Move_Type {
	    BEST, 
	    RANDOM,
	    SOMETHING_ELSE
	}
	
	private Moves moves;
	private static Random rand = new Random();
	
	public MonteCarlo (Moves moves) {
		this.moves = moves;
	}
	
	private LinkedList<Tuple> selectBestMove(Board board, int maxTests, boolean parallel, Move_Type moveType) {
		
		LinkedList<Tuple> allMoves = board.getAllPossibleMoves();
		int moveCount = allMoves.size();
		//Strategy strategy = rand.nextInt(2) == 0 ? Strategy.TABU_RANDOM : Strategy.TABU_COLOR_RANDOM;
		Strategy strategy = Strategy.TABU_COLOR_RANDOM;
		//Strategy strategy = Strategy.TABU_RANDOM;
		
		LinkedList<Tuple> bestMoves = new LinkedList<Tuple>();
		LinkedList<Integer> bestScores  = new LinkedList<Integer>();
		
		bestScores.add(-10000000);
		bestScores.add(-10000000);
		
		if (parallel) {
			LinkedList<Simulation> simulations = new LinkedList<Simulation>();
			
			for (Tuple move : allMoves) {
				Board tmpBoard = board.cloneBoard();
				int moveScore = board.getMoveScore(move);
				tmpBoard.applyMoveAt(move);
				
				Simulation s = new Simulation(tmpBoard, maxTests/moveCount, moveScore, move, strategy, moveType != Move_Type.RANDOM);
				s.newThread();
				s.getThread().start();
				
				simulations.add(s);
			}
			
			for (Simulation s : simulations) {
				try {
					s.getThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int tmpScore = s.getMoveScore();
				int score = s.getResult() + tmpScore;
				
				int goodIndex = 0; 
				for (int goodScore : bestScores) {
					if (score > goodScore) {
						bestScores.add(goodIndex, score);
						bestMoves.add(goodIndex, s.getMove());
						break;
					}
					goodIndex++;
				}
			}
		} else {
			
			Simulation simulation = new Simulation(strategy);
		
			for (Tuple move : allMoves) {
				
				Board tmpBoard = board.cloneBoard();
				tmpBoard.applyMoveAt(move);
				int tmpScore = board.getMoveScore(move);
				
				simulation.init(tmpBoard);
				int count = moveType == Move_Type.RANDOM ? 0 : maxTests/moveCount;
				int score = simulation.simulation(tmpBoard, maxTests/moveCount, moveType != Move_Type.RANDOM) + tmpScore;
				
				int goodIndex = 0; 
				for (int goodScore : bestScores) {
					if (score > goodScore) {
						bestScores.add(goodIndex, score);
						bestMoves.add(goodIndex, move);
						break;
					}
					goodIndex++;
				}
	
			}
		}
		
		return (bestMoves.size() == 0 ? null : bestMoves);		
	}
	

	/**
	 * Beste Kombination für 0: simulationCount = 200; randomMoveCount = 8;  moveCount = 6; normalMoveCount = 4; ~2-7s    == 1071 (1-3. Platz)
	 * Beste Kombination für 1: simulationCount = 200; randomMoveCount = 8;  moveCount = 6; normalMoveCount = 4; ~17s     == 1177 (2. Platz)
	 * Beste Kombination für 2: simulationCount = 200; randomMoveCount = 8;  moveCount = 6; normalMoveCount = 4; ~30s     == 22028 (1. Platz)
	 * Beste Kombination für 3: simulationCount = 200; randomMoveCount = 8;  moveCount = 6; normalMoveCount = 4; ~3s      == 11379 (2. Platz)
	 * Beste Kombination für 4: simulationCount = 200; randomMoveCount = 8;  moveCount = 6; normalMoveCount = 4; ~1-2s    == 89 (1-6. Platz)
	 * Beste Kombination für 5: simulationCount = 0;   randomMoveCount = 8;  moveCount = 2; normalMoveCount = 2; ~20s     == 315000 (5. Platz) geht so...
	 * Beste Kombination für 6: simulationCount = 0;   randomMoveCount = 8;  moveCount = 2; normalMoveCount = 2; ~23s     == 132000 (9. Platz) nicht gut...
	 * Beste Kombination für 7: simulationCount = 0;   randomMoveCount = 8;  moveCount = 2; normalMoveCount = 2; ~190s    == 506000 (5. Platz)
	 * Beste Kombination für 8: simulationCount = 0;   randomMoveCount = 8;  moveCount = 2; normalMoveCount = 2; ~80s     == 4530000 (3. Platz)
	 */
	public void mcts(Board board, int depth, Move movesSoFar, boolean ascension, double startTime, boolean contributer, Move_Type moveType) {
		
		int simulationCount;
		// Nur für Depth == 0!!!
		int startMoveCount;
		// Nur für Depth == 0!!!
		int normalMoveCount;
		// Für alle Züge und Rekursionstiefen!
		int moveCount;
		
		if ((board.getSize().x + board.getSize().y) < 50) {
			simulationCount = 200;
			startMoveCount = 8;
			normalMoveCount = 6;
			moveCount = 4;
		} else {
			simulationCount = 0;
			startMoveCount = 8;
			normalMoveCount = 2;
			moveCount = 2;
		}
		
		if (moveType == Move_Type.RANDOM) {
			simulationCount = 0;
		}
		
		LinkedList<Tuple> bestMoves = selectBestMove(board, simulationCount, false, moveType);
		LinkedList<ParallelMonteCarlo> threads = new LinkedList<ParallelMonteCarlo>();
		
		LinkedList<Integer> usedRandomNumbers = new LinkedList<Integer>();
		
		moveCount = depth < 5 ? 6 : depth < 100 ? 2 : 1;
		
		if (bestMoves == null) {
			return;
		}
		
		if (bestMoves.size() < startMoveCount) {
			startMoveCount = bestMoves.size();
		}
		if (bestMoves.size() < normalMoveCount) {
			normalMoveCount = bestMoves.size()/2;
		}
		
		if (bestMoves.size() < moveCount) {
			moveCount = bestMoves.size();
		}
		
		// Nächster Versuch: Für den Root-Knoten auch parallel 4 schlechte Moves auswerten. Eventuell kommt im Endeffekt ja was bei rum...
		if (depth == 0) {
			//System.out.println(bestMoves.size());
			for (int i = 0; i < startMoveCount; i++) {
				
				Move_Type firstMoveType = null;
				int index = 0;
				if (i < normalMoveCount) {
					index = i;
					firstMoveType = Move_Type.BEST;
				} else {
					index = rand.nextInt(bestMoves.size());
					/*while (usedRandomNumbers.contains(index)) {
						index = rand.nextInt(bestMoves.size());
						//firstMoveType = Move_Type.SOMETHING_ELSE;
						firstMoveType = Move_Type.RANDOM;
					}*/
					firstMoveType = Move_Type.RANDOM;
				}
				
				ParallelMonteCarlo pmc = new ParallelMonteCarlo(this, 
						board.cloneBoard(), 
						bestMoves.get(index), 
						movesSoFar.cloneMove(), 
						moves,
						simulationCount, 
						depth,
						startTime,
						contributer,
						firstMoveType);
				threads.add(pmc);
				pmc.startThread();
				
			}
			
		} else {
			
			for (int i = 0; i < bestMoves.size() && i < moveCount; i++) {
				int index = -1;
				/*while (index == -1) {
					
					switch (moveType) {
					case RANDOM:
						index = rand.nextInt(bestMoves.size());
						while (usedRandomNumbers.contains(index)) {
							index = rand.nextInt(bestMoves.size());
						}
						break;
					case BEST:
						index = i;
						break;
					case SOMETHING_ELSE:
						int random = rand.nextInt(3);
						moveType = random == 0 ? Move_Type.BEST : random == 1 ? Move_Type.RANDOM : Move_Type.SOMETHING_ELSE;
						break;
					}
				}*/
				int r = rand.nextInt(50);
				if (moveType != Move_Type.BEST){// && rand.nextInt(2) == 0) {
					index = rand.nextInt(bestMoves.size());
					//moveType = r == 0 ? Move_Type.RANDOM : moveType;
					moveType = depth > 100 && r < 5 ? Move_Type.BEST: moveType;
				} else {
					for (int j = 0; j <= i; j++) {
						if (!usedRandomNumbers.contains(j)) {
							index = i;
						}
					}
					moveType = depth > 100 && r < 5 ? Move_Type.RANDOM: moveType;
				}
				
				/*if (i > moveCount/2 || moveType != Move_Type.RANDOM) {
					index = rand.nextInt(bestMoves.size());
					while (usedRandomNumbers.contains(index)) {
						index = rand.nextInt(bestMoves.size());
					}
				} else {
					index = i;
				}*/
				usedRandomNumbers.add(index);
				
				
				
				ParallelMonteCarlo pmc = new ParallelMonteCarlo(this, 
						board.cloneBoard(), 
						bestMoves.get(index), 
						movesSoFar.cloneMove(), 
						moves, 
						simulationCount, 
						depth,
						startTime,
						contributer,
						moveType);
				
				pmc.startThread();
				try {
					pmc.getThread().join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).getThread().join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}



