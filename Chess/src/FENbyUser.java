import java.util.*;
public class FENbyUser {
	String name;
	TreeMap<Date,String> fens = new TreeMap<Date,String>();
	
	public FENbyUser(String name)
	{
		this.name = name;
	}
	
	public void Addfen(Date date,String fen)
	{
		this.fens.put(date, fen);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.name+"\n");
		for(Map.Entry<Date, String> pair : fens.entrySet())
		{
			sb.append(pair.getKey()+" "+pair.getValue()+"\n");
		}
		
		return sb.toString();
	}
}
