import java.util.*;
public class Move {
	public String Turn;
	public String MovePlayed;
	public String EngineMove;
	public int Eval;
	public String Gid = "";
	public String Depth;
	public int NumLegalMoves;
	public String FEN = "";
	public List<MoveEvaluation> LegalMoves = new ArrayList<MoveEvaluation>();
	
	public int BenfordValue()
	{
		int val = Math.abs(this.Eval);		
		return ((val % 100)/10);
	}
	
	public static int BenfordValue(int inp)
	{
		int val = Math.abs(inp);		
		return ((val % 100)/10);
	}
	
	public boolean IsCaptureMove()
	{
		return this.MovePlayed.contains("x");
	}
}
