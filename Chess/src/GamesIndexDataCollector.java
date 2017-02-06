import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
public class GamesIndexDataCollector implements Runnable {

	private File fileName;
	//private ConcurrentHashMap<String, Integer> gameMap;
	private ConcurrentHashMap<String, GameProp> gamePropMap;
    public GamesIndexDataCollector(File fileName,ConcurrentHashMap<String, GameProp> gamePropMap){  
        this.fileName = fileName; 
        //this.gameMap = gameMap;
        this.gamePropMap = gamePropMap;
    }  
     public void run() {  
    	 try
			{
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				System.out.println("Started processing file "+fileName.getName());				
				
				HashMap<GamePropEum,String> props = new HashMap<GamePropEum,String>();
				String line = "";
				String str = "";
				GameProp gp = null;
				while((line = br.readLine()) != null)
				{						
					if(line.startsWith("[GameID"))
					{
						str = Utils.GetQuotedValue(line);
						props = Utils.GetGameProps(str);
						if(gamePropMap.containsKey(str.trim()))
						{
							gp = gamePropMap.get(str.trim());
							gp.BlackPlayer = props.get(GamePropEum.GAME_BLACK_PLAYER);
							gp.WhitePlayer = props.get(GamePropEum.GAME_WHITE_PLAYER);
							gp.TournmentName = props.get(GamePropEum.TOURNMENT_NAME);
							
							gamePropMap.put(str.trim(), gp);
						}
						
					}
					else if(line.startsWith("{"))
					{
						if(gamePropMap.containsKey(str.trim()))
						{
							gp = gamePropMap.get(str.trim());
							int[] ratings = Utils.GetEloRatingsFromString(line.substring(1, line.length()-1));
							
							String matchDate = Utils.GetSolrDateString(line.substring(1, line.length()-1));
							
							gp.BlackELO = ratings[1];
							gp.WhiteELO = ratings[0];
							gp.TournmentDate = matchDate;
							gamePropMap.put(str.trim(), gp);
						}
					}
				}
				
				System.out.println("Done processing file "+fileName.getName());
				
				Utils.IncrementDoneFileCount();
				System.out.println("Done with "+Utils.GetDoneFileCount()+" files");
				Utils.PrintMemory();
				br.close();
			}
			catch(Exception exp)
			{
				System.out.println("Issue with "+fileName.getName()+" : "+exp.getMessage());
			}
    }
}
