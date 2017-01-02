/**
 * 
 */
import java.util.*;
/**
 * @author Srinath
 *
 */
public class FENProp {
	int count = 0;
	HashMap<String,String> gameResultMap = new HashMap<String,String>();
	HashMap<Integer,Integer> turnWiseCount = new HashMap<Integer,Integer>();
	HashSet<String> prevFen = new HashSet<String>();
	String id = "";
	
	public FENProp()
	{
		//this.id = Utils.SHA1(fen);
	}

	public synchronized void AddToResultMap(String GameId,String result)
	{
		gameResultMap.put(GameId,result);
	}
	
	public synchronized void UpdateTurnCount(int turnNum,int count)
	{
		if(turnWiseCount.containsKey(turnNum))
		{
			turnWiseCount.put(turnNum, turnWiseCount.get(turnNum)+count);
		}
		else
		{
			turnWiseCount.put(turnNum, count);
		}
	}
	
	public synchronized void UpdateFENProp(int count,String GameId,String Result)
	{
		this.count += count;
		this.gameResultMap.put(GameId, Result.length() > 0 ? Result : "NA");
	}
	
	public synchronized void AddPrevFen(String fen)
	{
		prevFen.add(fen);
	}
}
