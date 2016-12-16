
public class TestCases {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FEN f = new FEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1  Pe2-e4");
		System.out.println(f.moveNum);
		System.out.println(Utils.NumberPattern.matcher("192").matches());
		System.out.println(Utils.NumberPattern.matcher("192a").matches());

	}

}
