import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FENCountCheck implements Runnable {
	private File fileName;  
	private ConcurrentHashMap<String, Integer> fenCountMap;
	
	public FENCountCheck(File fileName,ConcurrentHashMap<String, Integer> fenMap)
	{
		this.fileName = fileName;
		this.fenCountMap = fenMap;
	}
	
	public void run() {  
   	 try
			{
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				System.out.println("Started processing file "+fileName.getName());
				
				
				HashMap<GamePropEum,String> props = new HashMap<GamePropEum,String>();
				Boolean isValidDate = false;
				String line = "";
				while((line = br.readLine()) != null)
				{						
					if(line.startsWith("[GameID"))
					{
						isValidDate = true; //TODO Some of the dates are missing, need to fix the issue
						String str = Utils.GetQuotedValue(line);
						props = Utils.GetGameProps(str);
					}
					else if(isValidDate && !line.startsWith("{"))
					{
						FEN fen = new FEN(line);
						if(fen.isValidFen() && fenCountMap.containsKey(fen.JustFen))
						{
							try
							{
								fenCountMap.put(fen.JustFen, fenCountMap.get(fen.JustFen)+1);
								//fenPropMap.put(fen.JustFen, fProp) ;
							}
							catch(Exception e)
							{
								System.out.println(e.getMessage());
							}
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
