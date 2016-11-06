
public class FEN {
	String FenComplete;
	String JustFen;
	int count;
	
	public FEN(String str)
	{
		this.FenComplete = str;
		String[] s = Utils.FenDivided(FenComplete);
		if(s.length == 3)
		{
			IsValidFen = true;
			JustFen = s[0]+" w "+s[2];			
		}
	}
	Boolean IsValidFen = false;
	
	public Boolean isValidFen()
	{
		return IsValidFen;
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
