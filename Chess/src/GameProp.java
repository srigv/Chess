
public class GameProp {
	public String TournmentName = "";
	public String TournmentDate = "";
	public String BlackPlayer = "";
	public String WhitePlayer = "";
	public int BlackELO;
	public int WhiteELO;
	public int id;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer("{\"id\" : "+this.id);
		if(this.TournmentName.length() > 0)
		{
			sb.append(",\n\"TournmentName\" : \""+this.TournmentName+"\"");
		}
		
		if(this.TournmentDate.length() > 0)
		{
			sb.append(",\n\"TournmentDate\" : \""+this.TournmentDate+"\"");
		}
		
		if(this.BlackELO > 0)
		{
			sb.append(",\n\"BlackELO\" : "+this.BlackELO);
		}
		
		if(this.BlackPlayer.length() > 0)
		{
			sb.append(",\n\"BlackPlayer\" : \""+this.BlackPlayer+"\"");
		}
		
		if(this.WhiteELO > 0)
		{
			sb.append(",\n\"WhiteELO\" : "+this.WhiteELO);
		}
		
		if(this.WhitePlayer.length() > 0)
		{
			sb.append(",\n\"WhitePlayer\" : \""+this.WhitePlayer+"\"");
		}
		
		sb.append("}");
		return sb.toString();
	}
}
