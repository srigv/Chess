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
	

	public void AddToResultMap(String GameId,String result)
	{
		gameResultMap.put(GameId,result);
	}
	
	public void UpdateTurnCount(int turnNum)
	{
		if(turnWiseCount.containsKey(turnNum))
		{
			turnWiseCount.put(turnNum, turnWiseCount.get(turnNum)+1);
		}
		else
		{
			turnWiseCount.put(turnNum, 1);
		}
	}
	
	public void UpdateFENProp(int count,String GameId,String Result)
	{
		this.count += count;
		this.gameResultMap.put(GameId, Result);
	}
}
