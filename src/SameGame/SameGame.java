package SameGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;

import SameGame.MonteCarlo.Move_Type;

public class SameGame {
	
	static String readFile(String path) throws IOException 	  
	{
		return new String(Files.readAllBytes(Paths.get(path)), Charset.defaultCharset()).replaceAll("\n", "");
	}
	
	private static void readInputBoard(Board board, boolean fromFile, String filename) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String message = "";
			if (!fromFile) {
				message = reader.readLine().replaceAll(" ", "");
			} else {
				message = readFile(filename).replaceAll(" ", "");;
			}
			
			if (message == null) {
				return;
			}
			
			board.parseInputBoard(message);
			
			
		} catch (IOException e) {
		}
		board.calcInitColorCount();
	}
	
	public static void main(String[] args) {		
		
		Board board = new Board();
		Moves moves = new Moves();
		MonteCarlo monteCarlo = new MonteCarlo(moves);
		String files[] = {"challenge1", "challenge2", "one_green", "one_red", "toothrot", "guybrush", "hermann", "hut", "earth"}; 

		Move movesSoFar = new Move(0, new LinkedList<Tuple>());
		
		readInputBoard(board, false, files[2]);
		//board.createRandomBoard(255, 255, 100);
		long before = System.currentTimeMillis();
		
		while (true) {
			monteCarlo.mcts(board, 0, movesSoFar, true, before, false, Move_Type.BEST);
		}
	}
	
}
