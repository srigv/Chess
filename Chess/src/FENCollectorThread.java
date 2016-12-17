import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FENCollectorThread implements Runnable {
	private File fileName;  
	private ConcurrentHashMap<String, Boolean> fenMap;
	private ConcurrentHashMap<String, Boolean> gameMap;
	private int dumpType = 0;
    public FENCollectorThread(File fileName,ConcurrentHashMap<String, Boolean> fenMap,ConcurrentHashMap<String, Boolean> gameMap,int dumpType){  
        this.fileName = fileName;  
        this.fenMap = fenMap;
        this.gameMap = gameMap;
        this.dumpType = dumpType;
    }  
     public void run() {  
    	 try
			{
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				
				HashMap<GamePropEum,String> props = new HashMap<GamePropEum,String>();
				Boolean isValidDate = false;
				String line = "";
				while((line = br.readLine()) != null)
				{						
					if(line.startsWith("[GameID") && dumpType == 3)
					{
						isValidDate = true; //TODO Some of the dates are missing, need to fix the issue
						String str = Utils.GetQuotedValue(line);
						props = Utils.GetGameProps(str);
						if(props.get(GamePropEum.GAME_ID).length() > 0)
						{
							if(!gameMap.containsKey(props.get(GamePropEum.GAME_ID)))
							{
								gameMap.put(props.get(GamePropEum.GAME_ID), true);
							}
						}
					}
					else if(!line.startsWith("{") && !line.startsWith("[GameID"))
					{
						FEN fen = new FEN(line);
						if(fen.isValidFen())
						{
							try
							{
								if((dumpType == 2 && !fen.isWhiteMove) || (dumpType == 1 && fen.isWhiteMove))
								{
									if(!fenMap.containsKey(fen.JustFen))
									{
										fenMap.put(fen.JustFen, true);
									}
								}
							}
							catch(Exception e)
							{
								System.out.println(e.getMessage());
							}
						}
					}
				}
				System.out.println("Done processing file "+fileName.getName());
				System.out.println("Games colected "+gameMap.size());
				System.out.println("FENs colected "+fenMap.size());
				
				Utils.PrintMemory();
				Utils.IncrementDoneFileCount();
				
				System.out.println("Done with "+Utils.GetDoneFileCount()+" files");
				br.close();
			}
			catch(Exception exp)
			{
				System.out.println("Issue with "+fileName.getName()+" : "+exp.getMessage());
			}
    }
}
