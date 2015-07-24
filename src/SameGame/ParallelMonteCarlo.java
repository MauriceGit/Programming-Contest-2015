package SameGame;

import SameGame.MonteCarlo.Move_Type;

public class ParallelMonteCarlo implements Runnable{
	
	private Board board;
	private Tuple move;
	private int   simulationCount;
	private Move  movesSoFar;
	private Moves moves;
	private Thread ownThread;
	private MonteCarlo mc;
	private int depth;
	private double startTime;
	private boolean contributer;
	private Move_Type moveType;
	
	public ParallelMonteCarlo(MonteCarlo mc, Board board, Tuple move, Move movesSoFar, Moves moves, int simulationCount, int depth, double startTime, boolean contributer, Move_Type moveType) {
		this.mc = mc;
		this.board = board;
		this.move = move;
		this.movesSoFar = movesSoFar;
		this.moves = moves;
		this.simulationCount = simulationCount;
		this.depth = depth;
		this.startTime = startTime;
		this.contributer = contributer;
		this.moveType = moveType;
	}
	
	public void startThread() {
		ownThread = new Thread(this);
		ownThread.start();
	}
	
	public Thread getThread() {
		return ownThread;
	}
	
	@Override
	public void run() {
		
			int goodScore = board.getMoveScore(move);
			board.applyMoveAt(move);
			Move goodMoveSoFar = movesSoFar.cloneMove();
			goodMoveSoFar.appendTuple(move, goodScore);
			int penalty = board.calculatePenalty();
			boolean contribute = moves.submitAndPrintMove(goodMoveSoFar, penalty, startTime);
			
			double factor = 0.99;
			double factorizedPenalty = -penalty * factor;
			
			// Wenn garnichts besseres mehr erreicht werden kann, abbrechen!
			if (moves.getBestScore() >= goodMoveSoFar.getScore() + factorizedPenalty ) {
				return;
			}
			
			mc.mcts(board, depth+1, goodMoveSoFar, true, startTime, contributer || contribute, moveType);
		
	}

}
