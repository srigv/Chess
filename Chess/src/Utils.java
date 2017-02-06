import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Utils {
	public static HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();
	public static Pattern NumberPattern = Pattern.compile("^[0-9]+$");
	private static AtomicInteger FileCount = new AtomicInteger();
	private static Calendar c = Calendar.getInstance();
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
	
	public static int[] GetEloRatingsFromString(String str)
	{
		int[] arr = new int[2];
		String[] parts = str.split(";");
		if(parts.length == 3)
		{
			String[] ratings = parts[0].split(",");
			if(ratings.length == 2)
			{
				if(ratings[0].matches("-?\\d+(\\.\\d+)?"))
				{
					try{
						arr[0] = Integer.parseInt(ratings[0].trim());
					}
					catch(Exception e)
					{
						//do nothing for now
					}
				}
				
				if(ratings[1].matches("-?\\d+(\\.\\d+)?"))
				{
					try{
						arr[1] = Integer.parseInt(ratings[1].trim());
					}
					catch(Exception e)
					{
						//do nothing for now
					}
				}
			}
		}
		
		return arr;		
	}
	
	public static String GetSolrDateString(String str)
	{
		String[] parts = str.split(";");
		if(parts.length == 3)
		{
			String[] extras = parts[2].split(",");
			if(extras.length == 2)
			{
				String[] dateParts = extras[1].trim().split("\\.");
				if(dateParts.length == 3)
				{
					int year = 1000;
					int month = 1;
					int day = 1;
					
					if(dateParts[0].matches("-?\\d+(\\.\\d+)?"))
					{
						year = Integer.parseInt(dateParts[0]);
					}
					
					if(dateParts[1].matches("-?\\d+(\\.\\d+)?"))
					{
						month = Integer.parseInt(dateParts[1]);
					}
					
					if(dateParts[2].matches("-?\\d+(\\.\\d+)?"))
					{
						day = Integer.parseInt(dateParts[2]);
					}
					
					c.set(year, month-1, day);
					
					
					SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					try {
			            return out.format(c.getTime());
			        } catch (Exception ignore) { }
				}
			}						
		}
		
	    return "1000-01-01T17:33:18Z";
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
			//first part FEN
			//second part move count since pawn
			//third part move turn
			//fourth the move played in TAN notation
			String[] fenParts = str.split(" ");
			if(fenParts.length > 2 && fenParts[2] != null && fenParts[2].equals("-"))
			{
				arr[0] = fenParts[0]+" "+fenParts[1];
				if(fenParts.length >= 5)
				{
					arr[1] = fenParts[4].trim();
				}
				
				if(fenParts.length >= 6)
				{
					arr[2] = fenParts[5].trim();
				}
				
				if(fenParts.length >= 8)
				{
					arr[3] = fenParts[7].trim();
				}
			}
			else if(fenParts.length > 2 && fenParts[2] != null && !fenParts[2].equals("-"))
			{
				arr[0] = fenParts[0]+" "+fenParts[1] +" "+fenParts[2];
				if(fenParts.length >= 5)
				{
					arr[1] = fenParts[4].trim();
				}
				
				if(fenParts.length >= 6)
				{
					arr[2] = fenParts[5].trim();
				}
				
				if(fenParts.length >= 8)
				{
					arr[3] = fenParts[7].trim();
				}
			}
			
//			if(str.contains("- -"))
//			{
//				String[] parts = str.split("- -");
//				if(parts.length == 2)
//				{
//					String[] first = parts[0].trim().split(" ");
//					if(first.length >= 2)
//					{
//						arr[0] = first[0].trim();
//						arr[1] = first[1].trim();
//						arr[2] = "";
//					}
//					
//					
//					String[] second = parts[1].trim().split(" ");
//					if(second.length == 2)
//					{
//						arr[3] = second[1].trim();
//					}					
//				}
//			}
//			else if(str.contains("-"))
//			{
//				String[] parts = new String[2];
//				
//				if(parts.length == 2)
//				{
//					String[] first = parts[0].trim().split(" ");
//					if(first.length == 3)
//					{
//						arr[0] = first[0].trim();
//						arr[1] = first[1].trim();
//						arr[2] = first[2].trim();
//					}
//					
//					
//					String[] second = parts[1].trim().split(" ");
//					if(second.length == 2)
//					{
//						arr[3] = second[1].trim();
//					}					
//				}
//			}
		}
		catch(Exception e)
		{
			//TODO handle the issue
		}
		
		
		return arr;
	}
	
	public static void IncrementDoneFileCount()
	{
		FileCount.addAndGet(1);
	}
	
	public static int GetDoneFileCount()
	{
		return FileCount.get();
	}
	
	public static void ResetFileCount()
	{
		FileCount.set(0);
	}
	
	public static String GetQuotedValue(String inp)
	{
		if(inp.contains("\""))
		{
			if(inp.indexOf('"', inp.indexOf('"')+1) > -1)
			{
				return inp.substring(inp.indexOf('"')+1,inp.indexOf('"', inp.indexOf('"')+1));
			}			
		}
		
		return "";
	}
	
	public static void PrintMemory()
	{
		System.gc();
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();
		System.out.println("##### Heap utilization statistics [MB] #####");
		
		//Print used memory
		System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

		//Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);
		
		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		
		
	}
	
	private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 
 
    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
    MessageDigest md;
    md = MessageDigest.getInstance("SHA-1");
    byte[] sha1hash = new byte[40];
    md.update(text.getBytes("iso-8859-1"), 0, text.length());
    sha1hash = md.digest();
    return convertToHex(sha1hash);
    }

}



