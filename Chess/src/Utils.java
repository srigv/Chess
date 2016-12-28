import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Utils {
	public static HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();
	public static Pattern NumberPattern = Pattern.compile("^[0-9]+$");
	private static AtomicInteger FileCount = new AtomicInteger();
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



