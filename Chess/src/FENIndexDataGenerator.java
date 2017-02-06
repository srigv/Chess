import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FENIndexDataGenerator implements Runnable {

	private File fileName;  
	private ConcurrentHashMap<String, Boolean> fenMap;
	private ConcurrentHashMap<String, Integer> gameMap;
	private ConcurrentHashMap<String, FENProp> fenPropMap;
	private int dumpType = 1;
    public FENIndexDataGenerator(File fileName,ConcurrentHashMap<String, Boolean> fenMap,ConcurrentHashMap<String, Integer> gameMap,ConcurrentHashMap<String, FENProp> fenPropMap,int dumpType){  
        this.fileName = fileName;  
        this.fenMap = fenMap;
        this.gameMap = gameMap;
        this.fenPropMap = fenPropMap;
        this.dumpType = dumpType;
    }  
     public void run() {  
    	 try
			{
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				System.out.println("Started processing file "+fileName.getName());
				
				
				HashMap<GamePropEum,String> props = new HashMap<GamePropEum,String>();
				Boolean isValidDate = false;
				String line = "";
				String prev = "";
				while((line = br.readLine()) != null)
				{						
					if(line.startsWith("[GameID"))
					{
						isValidDate = true; //TODO Some of the dates are missing, need to fix the issue
						String str = Utils.GetQuotedValue(line);
						props = Utils.GetGameProps(str);
						prev = "";
					}
					else if(isValidDate && !line.startsWith("{"))
					{
						FEN fen = new FEN(line);
						int moveType = fen.isWhiteMove ? 1 : 2;
						if(moveType == dumpType && fen.isValidFen() && fenMap.containsKey(fen.JustFen))
						{
							try
							{
								fenPropMap.get(fen.JustFen).UpdateTurnCount(fen.moveNum,1);
								fenPropMap.get(fen.JustFen).UpdateFENProp(1, gameMap.get(props.get(GamePropEum.GAME_ID))+"", fen.MovePlayed);
								//fenPropMap.put(fen.JustFen, fProp) ;
								if(prev.length() > 0)
								{
									fenPropMap.get(fen.JustFen).AddPrevFen(prev);
								}								
							}
							catch(Exception e)
							{
								System.out.println(e.getMessage());
							}
						}
						
						if(fen.isValidFen())
						{
							prev = fen.JustFen;
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
