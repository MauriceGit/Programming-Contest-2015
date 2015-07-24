package SameGame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Board {
	
	private int sizeX = 0;
	private int sizeY = 0;
	private int board[][] = null;
	private int initColorCount;
	
	public Board(int initColorCount){
		this.initColorCount = initColorCount;
	}
	
	public Board(){
		
	}
	
	public void initBoard(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.board = new int[sizeX][sizeY];
	}
	
	public void setBoard(int newBoard[][]) {
		this.board = newBoard;
	}
	
	public void setSize(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	public Tuple getSize() {
		return new Tuple(sizeX, sizeY);
	}
	
	public int getColorAt(int x, int y) {
		return board[x][y];
	}
	
	public int getColorAt(Tuple t) {
		return getColorAt(t.x, t.y);
	}
	
	public void createRandomBoard(int sizeX, int sizeY, int colorCount) {
		Random rand = new Random();
		
		initBoard(sizeX, sizeY);
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				board[x][y] = rand.nextInt(colorCount);
			}			
		}
		correctBoard();
	}
	
	public void parseInputBoard(String message) {
		
		int sizeX = message.split("\\[")[2].replaceAll("\\]", "").split(",").length;
		int sizeY = message.replaceAll("[^\\[]", "").length()-1;
		
		//System.out.println(message.split("\\[")[2].replaceAll("\\]", ""));
		
		initBoard(sizeX, sizeY);
		
		//System.out.println(sizeX + ", " + sizeY);
		
		String moves = message.replaceAll(" ",  "")
							.replaceAll("\\[", "")
							.replaceAll("\\]", "");
		
		String[] array = moves.split(",");
		
		
		for (int i = 0; i < array.length; i++) {
			this.board[i % sizeX][i / sizeX] = Integer.parseInt(array[i]);
		}
	}
	
	public void calcInitColorCount() {
		this.initColorCount = getColors().size();
	}
	
	public int getInitColorCount() {
		return initColorCount;
	}
	
	public Board cloneBoard() {
		Board newBoard = new Board(initColorCount);
		
		int tmpArray[][] = new int[sizeX][sizeY];
		
		for (int i = 0; i < this.board.length; i++) {
		    System.arraycopy(this.board[i], 0, tmpArray[i], 0, this.board[0].length);
		}
		
		newBoard.setSize(sizeX, sizeY);
		newBoard.setBoard(tmpArray);
		
		return newBoard;
	}
	
	private void eraseColorAt(int color, int x, int y) {
		if (board[x][y] == color) {
			board[x][y] = 0;
			if (x > 0) {
				eraseColorAt(color, x-1, y);
			}
			if (x < sizeX-1) {
				eraseColorAt(color, x+1, y);
			}
			if (y > 0) {
				eraseColorAt(color, x, y-1);
			}
			if (y < sizeY-1) {
				eraseColorAt(color, x, y+1);
			}
		}
	}
	
	private int markColorGroupAt(int color, boolean testField[][], int x, int y) {
		int res = 0;
		if (board[x][y] == color && !testField[x][y]) {
			testField[x][y] = true;
			res++;
			if (x > 0) {
				res += markColorGroupAt(color,testField, x-1, y);
			}
			if (x < sizeX-1) {
				res += markColorGroupAt(color, testField, x+1, y);
			}
			if (y > 0) {
				res += markColorGroupAt(color, testField, x, y-1);
			}
			if (y < sizeY-1) {
				res +=markColorGroupAt(color, testField, x, y+1);
			}
		}
		return res;
	}
	
	public int calcColorConnections() {
		int connections = 0;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				int bonus = 0;
				if (x < sizeX-1 && board[x][y] == board[x+1][y]) {
					connections++;
					bonus++;
				}
				if (y < sizeY-1 && board[x][y] == board[x][y+1]) {
					connections++;
					bonus++;
				}
				if (x > 0 && board[x][y] == board[x-1][y]) {
					connections++;
					bonus++;
				}
				if (y > 0 && board[x][y] == board[x][y-1]) {
					connections++;
					bonus++;
				}
				//connections += bonus*bonus;
				//connections += bonus;
				// Sehr hohe Punkte mit <= 3 und += bonus ... komisch...!
				if (bonus >= 3) {
					connections += bonus;
				}
			}			
		}
		return connections;
	}
	
	private boolean isGroupAt (int x, int y) {
		return  board[x][y] != 0 && (
				x > 0 && board[x][y] == board[x-1][y] ||
				x < sizeX-1 && board[x][y] == board[x+1][y] ||
				y > 0 && board[x][y] == board[x][y-1] ||
				y < sizeY-1 && board[x][y] == board[x][y+1]);
	}
	
	/**
	 * Shifted eine Reihe von oben nach unten und füllt Leere Felder auf (runterfallen lassen).
	 * Gibt zurück, ob mindestens 1 Stein geshifted wurde.
	 * @return Ob mindestens 1 Stein bewegt wurde.
	 */
	private boolean shiftColsDownAt(int col, int at) {
		int moved = 0;
		int lastI = at;
		for (int i = at; i < sizeY-1; i++) {
			moved += board[col][i+1];
			board[col][i] = board[col][i+1];
			lastI = i+1;
		}
		
		board[col][lastI] = 0;
		
		return moved > 0;
	}
	
	/**
	 * Shifted eine Spalte von rechts nach links.
	 * Gibt zurück, ob mindestens eine Spalte geshifted wurde.
	 * @return ob mindestens eine Spalte geshifted wurde.
	 */
	private boolean shiftColsLeftAt(int col) {
		int moved = 0;
		for (int newCol = col; newCol < sizeX-1; newCol++) {
			for (int row = 0; row < sizeY; row++) {
				moved += board[newCol+1][row];
				board[newCol][row] = board[newCol+1][row];
			}
		}
		// Letzte Reihe auf 0 setzen.
		for (int row = 0; row < sizeY; row++) {
			board[sizeX-1][row] = 0;
		}
		
		return moved > 0;
	}
	
	private boolean colIsEmpty(int col) {
		for (int i = 0; i < sizeY; i++) {
			if (board[col][i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	private void correctBoard() {
		// Über Spalten
		for (int i = 0; i < sizeX; i++) {
			// Über Zeilen
			for (int j = 0; j < sizeY; j++) {
				while (board[i][j] == 0) {
					// Abbruch, alle Felder über dem aktuellen sind leer, ein
					// weiteres herunterfallen lassen daher obsolet.
					if (!shiftColsDownAt(i, j)) {
						break;
					}
				}
			}
		}
		
		for (int i = 0; i < sizeX-1; i++) {
			if (colIsEmpty(i)) {
				if (shiftColsLeftAt(i)) {
					i--;
				}				
			}
		}		
	}
	
	/**
	 * Gib eine Liste mit klickbaren Koordinaten zurück, die valide Züge sind.
	 */
	public LinkedList<Tuple> getAllPossibleMoves() {
		
		boolean used[][] = new boolean[sizeX][sizeY];
		LinkedList<Tuple> res = new LinkedList<Tuple>();
		
		// Schleife über das gesamte Feld:
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!used[x][y] && isGroupAt(x, y)) {
					res.add(0, new Tuple(x, y));
					markColorGroupAt(board[x][y], used, x, y);
				}
			}			
		}
		
		return res;		
	}
	
	public void applyMoveAt(int x, int y) {
		if (isGroupAt(x, y)) {
			eraseColorAt(board[x][y], x, y);
			correctBoard();
		}
	}
	
	public void applyMoveAt(Tuple t) {
		applyMoveAt(t.x, t.y);
	}
	
	public int getMoveScore(Tuple move) {
		
		boolean tmp[][] = new boolean[sizeX][sizeY];
		
		int count = markColorGroupAt(board[move.x][move.y], tmp, move.x, move.y);
		
		return (count-1)*(count-1);		
	}
	
	public boolean isEmpty() {
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (board[x][y] != 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int calculatePenalty() {
		int penalty = 0;
		HashMap<Integer, Integer> addedColours = new HashMap<Integer, Integer>();
		
		if (isEmpty()) {
			return 0;
		}
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (board[x][y] != 0) {
					int value = 1;
					int key = board[x][y];
					if (addedColours.containsKey(key)) {
						value += addedColours.get(key);
					}					
					addedColours.put(key, value);
				}
			}
		}
		
		for (int value : addedColours.values()) {
			penalty -= (value-1)*(value-1);
		}
		
		return penalty;
	}
	
	public boolean singleColorStonesDetected() {
		
		boolean testField[][] = new boolean[sizeX][sizeY];
		
		LinkedList<Integer> colorOccurrence = new LinkedList<Integer>();
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!testField[x][y]) {
					colorOccurrence.add(markColorGroupAt(board[x][y], testField, x, y));
				}
			}
		}
		
		for (int occurrence : colorOccurrence) {
			if (occurrence == 1) {
				return true;
			}
		}
		return false;	
	}
	
	public int getColorCount(int color) {
		int colorCount = 0;
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (board[x][y] == color) {
					colorCount++;
				}
			}
		}
		
		return colorCount;
	}


	public int getBiggestGroupSize() {
		
		boolean testField[][] = new boolean[sizeX][sizeY];
		
		int largestGroupCount = 0;
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!testField[x][y] && isGroupAt(x, y)) {
					int value = markColorGroupAt(board[x][y], testField, x, y);
					if (value > largestGroupCount) {
						largestGroupCount = value;
					}
				}
			}
		}
		return largestGroupCount;
	}
	
	// Einzelne Steine gelten als einzelne Gruppe. Dadurch werden einzelne Steine zu
	// Gruppen migriert.
	public int getGroupCount() {
		int groupCount = 0;
		
		boolean testField[][] = new boolean[sizeX][sizeY];
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!testField[x][y] && board[x][y] != 0) {
					markColorGroupAt(board[x][y], testField, x, y);
					groupCount++;
				}
			}
		}
		
		return groupCount;
	}
	
	/**
	 * Gibt eine Hashmap zurück mit: (Farbe -> Anzahl)
	 */
	public HashMap<Integer, Integer> getColors() {
		//LinkedList<Tuple> colorList = new LinkedList<Tuple>();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!map.containsKey(board[x][y])) {
					map.put(board[x][y], 1);
				} else {
					int count = map.get(board[x][y]);
					map.put(board[x][y], count+1);
				}
			}
		}
		
		return map;
	}
	
	public int getMostFrequentColor() {
		int color = 0;
		int frequency = 0;
		HashMap<Integer, Integer> map = getColors();
		
		for (int c : map.keySet()) {
			if (map.get(c) > frequency) {
				color = c;
				frequency = map.get(c);
			}
		}
		
		return color;
	}
	
	public String toString() {
		String output = "";

		for (int y = sizeY - 1; y >= 0; y--) {
			for (int x = 0; x < sizeX; x++) {
				output = output + this.board[x][y] + "\t";
			}
			output += "\n";
		}

		return output;
	}
	
}
