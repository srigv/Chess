import java.util.*;
import java.util.regex.Pattern;

public class Utils {
	public static HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();
	public static Pattern NumberPattern = Pattern.compile("^[0-9]+$");
	public static HashMap<GamePropEum,String> GetGameProps(String str)
	{
		HashMap<GamePropEum,String> map = new HashMap<GamePropEum,String>();
		String[] arr = str.split(";");
		
		map.put(GamePropEum.GAME_ID, str);
		
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
		for(int i = 0; i < 4; i++)
		{
			arr[i] = "";
		}
		try
		{
			if(str.contains("- -"))
			{
				String[] parts = str.split("- -");
				if(parts.length == 2)
				{
					String[] first = parts[0].trim().split(" ");
					if(first.length >= 2)
					{
						arr[0] = first[0].trim();
						arr[1] = first[1].trim();
						arr[2] = "";
					}
					
					
					String[] second = parts[1].trim().split(" ");
					if(second.length == 2)
					{
						arr[3] = second[1].trim();
					}					
				}
			}
			else if(str.contains("-"))
			{
				String[] parts = str.split("-");
				if(parts.length == 2)
				{
					String[] first = parts[0].trim().split(" ");
					if(first.length == 3)
					{
						arr[0] = first[0].trim();
						arr[1] = first[1].trim();
						arr[2] = first[2].trim();
					}
					
					
					String[] second = parts[1].trim().split(" ");
					if(second.length == 2)
					{
						arr[3] = second[1].trim();
					}					
				}
			}
		}
		catch(Exception e)
		{
			//TODO handle the issue
		}
		
		
		return arr;
	}

}



