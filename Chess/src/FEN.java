import java.util.*;

public class FEN {
	String FenComplete;
	String JustFen;
	int count;
	int moveNum = 0;
	String MovePlayed = "NA";
	HashMap<String,String> GameResultMap = new HashMap<String,String>();
	HashMap<Integer,Integer> TurnWiseCount = new HashMap<Integer,Integer>();
	boolean isWhiteMove = true;
	
	public FEN(String str)
	{
		this.FenComplete = str;
		String[] s = Utils.FenDivided(FenComplete);
		if(s.length == 4 && s[0] != null && s[1] != null && s[2] != null && s[3] != null)
		{
			IsValidFen = true;
			JustFen = s[0].trim();
			if(Utils.NumberPattern.matcher(s[2].trim()).matches())
			{
				moveNum = Integer.parseInt(s[2].trim());
			}
			
			String[] parts = s[0].split(" ");
			if(parts[1].trim().equals("b"))
			{
				isWhiteMove = false;
			}
			
			MovePlayed = s[3].trim();
		}
	}
	
	public FEN(String str, int count)
	{
		this.JustFen = str;
		this.count = count;
	}
	
	Boolean IsValidFen = false;
	
	public Boolean isWhiteMove()
	{
		return isWhiteMove;
	}
	
	public int MoveNum()
	{
		return moveNum;
	}
	
	public Boolean isValidFen()
	{
		return ((IsValidFen && moveNum > 8) || moveNum == 0) && JustFen != null && JustFen.length() > 0;
	}
	
	public Boolean isOpeningMove()
	{
		return moveNum <= 8 && moveNum > 0;
	}
	
	public String justFen()
	{
		return JustFen;
	}
	
	@Override
	public int hashCode()
	{
		if(IsValidFen)
		{
			if(!Utils.symbolTable.containsKey(JustFen))
			{
				Utils.symbolTable.put(JustFen, (new String(JustFen)).hashCode());
			}
			return Utils.symbolTable.get(JustFen);
		}
		
		return (new String(FenComplete)).hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		FEN two = (FEN)obj;
		return two.JustFen.equals(this.JustFen);
	}

}
