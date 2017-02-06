import java.util.Arrays;

public class TestCases {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FEN f = new FEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1  Pe2-e4");
		
		System.out.println(f.JustFen);
		System.out.println(f.moveNum);
		System.out.println(f.MovePlayed);
		
		f = new FEN("r2q1rk1/1p1b1pbp/3p2p1/p1nPp2n/4P3/P1N1BP2/1PB1N1PP/R2Q1RK1 w - - 1 14  Ra1-b1");
		
		System.out.println(f.JustFen);
		System.out.println(f.moveNum);
		System.out.println(f.MovePlayed);
		
		GameProp prop = new GameProp();
		prop.BlackELO = 1000;
		prop.BlackPlayer = "test1";
		prop.id = 100;
		prop.TournmentDate = "2011-98-89";
		prop.TournmentName = "test";
		prop.WhiteELO = 1890;
		prop.WhitePlayer = "test2";
		
		System.out.println(Utils.GetSolrDateString(("{2201,2121;A40;tourn (),2011.09.25}").substring(1, ("{2201,2121;A40;tourn (),2011.09.25}").length()-1)));
		System.out.println(Arrays.toString(Utils.GetEloRatingsFromString("2201,2121;A40;tourn (),2011.09.25")));
	}

}
