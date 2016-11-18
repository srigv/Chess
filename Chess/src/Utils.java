import java.util.*;

public class Utils {
	public static HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();
	public static HashMap<GamePropEum,String> GetGameProps(String str)
	{
		HashMap<GamePropEum,String> map = new HashMap<GamePropEum,String>();
		String[] arr = str.split(";");
		if(arr.length == 7)
		{
			for(int i = 0; i < arr.length ; i++)
			{
				if(i == 0)
				{
					map.put(GamePropEum.TOURNMENT_NAME, arr[i]);
				}
				else if(i == 1)
				{
					map.put(GamePropEum.TOURNMENT_LOCATION, arr[i]);
				}
				else if(i == 2)
				{
					map.put(GamePropEum.TOURNMENT_DATE, arr[i]);
				}
				else if(i == 3)
				{
					map.put(GamePropEum.UNKNOWN, arr[i]);
				}
				else if(i == 4)
				{
					map.put(GamePropEum.GAME_WHITE_PLAYER, arr[i]);
				}
				else if(i == 5)
				{
					map.put(GamePropEum.GAME_BLACK_PLAYER, arr[i]);
				}
				else if(i == 6)
				{
					map.put(GamePropEum.GAME_RESULT, arr[i]);
				}
				
			}
		}		
		
		return map;
	}
	
	public static String[] FenDivided(String str)
	{
		String[] arr = new String[4];
		try
		{
			String[] temp = str.substring(0, str.indexOf('-')).trim().split(" ");
			String num = "";
			if(str.indexOf("-", str.indexOf('-')+1) > -1)
			{
				num = str.substring(str.indexOf('-')+1).trim().split(" ")[1].trim();
			}
			else
			{
				num = str.substring(str.indexOf('-')).trim().split(" ")[1].trim();
			}
			
			if(temp.length >= 3)
			{
				for(int i = 0 ; i < 3 ; i++)
				{
					arr[i] = temp[i].trim();
				}
			}
			
			arr[3] = num;
		}
		catch(Exception e)
		{
			
			String[] temp = str.split(" ");
			if(temp.length >= 3)
			{
				for(int i = 0 ; i < 3 ; i++)
				{
					arr[i] = temp[i];
				}
			}
		}
		
		
		return arr;
	}

}



