package analyse;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

import java.nio.file.Paths;

public class SgfParser {
	private static String sgfFolder = "C:\\Users\\tszsu\\Documents\\Lizzie.0.5.Windows.x64.GPU\\lizzie\\";

	public static void main(String[] args) {
		int totalTimeMs = 0;
		System.out.println(totalTimeMs);

		Game game = Sgf.createFromPath(Paths.get(sgfFolder + "lss_hd1.sgf"));
		System.out.println(game.getNoMoves());
		System.out.println(game.getProperties());
		GameNode node = game.getRootNode();
		do {
			System.out.println(node.getProperties());
		} while ((node = node.getNextNode()) != null);
	}

	public static Game parseGame(String sgfPath) {
		return Sgf.createFromPath(Paths.get(sgfPath));
	}
}
