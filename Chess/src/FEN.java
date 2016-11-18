
public class FEN {
	String FenComplete;
	String JustFen;
	int count;
	int moveNum = 0;
	
	public FEN(String str)
	{
		this.FenComplete = str;
		String[] s = Utils.FenDivided(FenComplete);
		if(s.length == 4 && s[0] != null && s[1] != null && s[2] != null && s[3] != null)
		{
			IsValidFen = true;
			JustFen = s[0]+" w "+s[2];
			moveNum = Integer.parseInt(s[3]);
		}
	}
	
	public FEN(String str, int count)
	{
		this.JustFen = str;
		this.count = count;
	}
	
	Boolean IsValidFen = false;
	
	
	public int MoveNum()
	{
		return moveNum;
	}
	
	public Boolean isValidFen()
	{
		return IsValidFen && moveNum > 8;
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
