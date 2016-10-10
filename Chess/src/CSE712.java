import java.io.*;
import java.util.*;
import java.util.Map.Entry;
public class CSE712 {
	static List<Game> games = new ArrayList<Game>();
	static BufferedWriter bw;
	static int[] benfordArray = new int[10];
	static int[] zipfLawStats = new int[50];
	static TreeMap<Integer,int[]> valueMap = new TreeMap<Integer,int[]>();
	static TreeMap<Integer,int[]> lowRateValueMap = new TreeMap<Integer,int[]>();
	static TreeMap<Integer,int[]> highRateValueMap = new TreeMap<Integer,int[]>();	
	static TreeMap<Integer,double[]> pieceMoveMap = new TreeMap<Integer,double[]>();
	static TreeMap<Integer,ArrayList<Integer>> movesByRating = new TreeMap<Integer,ArrayList<Integer>>();
	static String[] pieces = new String[]{"Bishop","King","Knight","Pawn","Queen","Rook"};
	public static void PrintHelp()
	{
		System.out.println("\n##########################################\n");
		System.out.println("----Usage----");
		System.out.println("java -jar CSE712.jar [INPUT(FILE | DIRECTORY)] [OUTFILE]");
		System.out.println("\n----Extra options----");
		System.out.println("--ff [FILE EXTENSION]");
		System.out.println("use this option to filter input files by extension Eg : --ff .aif");
		
		System.out.println("\n--rf [REGEX]");
		System.out.println("use this option to filter input files by regex(JAVA)");
		
		System.out.println("\nEg: --rf (?=.*2100)(?=.*aif) to filter all files contaning 2100 and aif strings");
		System.out.println("\n##########################################\n");
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0)
		{
			PrintHelp();
			return;
		}
		for(int i = 0; i < args.length ; i++)
		{
			if(args[i].equals("--help"))
			{
				PrintHelp();
				return;
			}
		}		
		
		if(args.length < 2)
		{
			System.out.println("Insufficient number of arguments, min required 2 but found "+args.length);
			return;
		}
		FileFilter filter = null;
		for(int i = 0; i < args.length ; i++)
		{
			if(args[i].equals("--rf"))
			{
				if(i+1 < args.length)
				{
					filter = new MyFilter("",args[i+1]);
				}
			}
			else if(args[i].equals("--ff"))
			{
				if(i+1 < args.length)
				{
					filter = new MyFilter(args[i+1],"");
				}				
			}
		}
		
		System.out.println("Handling file "+args[0]);
		File input = new File(args[0]);
		
		if(!input.exists())
		{
			return;
		}
		File[] fileArray;
		if(input.isDirectory())
		{
			System.out.println(input.getAbsolutePath());
			if(filter != null)
			{
				fileArray = input.listFiles(filter);
			}
			else
			{
				fileArray = input.listFiles();
			}
			
		}
		else
		{
			fileArray = new File[]{input};
		}
		
		System.out.println("Reading games..."+fileArray.length);
		FileOutputStream out = new FileOutputStream(args[1]);
		bw = new BufferedWriter(new OutputStreamWriter(out));
	
		for(File f : fileArray)
		{
			System.out.println("Reading games from "+f.getName());
			BufferedReader br = null;
			try
			{
				 br = new BufferedReader(new FileReader(f));
			}
			catch(Exception exp)
			{
				System.out.println("Issue with "+f.getName()+" : "+exp.getMessage());
			}
			
			try
			{
				String line;
				Game currentGame = null;
				Move currentMove = null;
				while((line = br.readLine()) != null)
				{
					if(line.startsWith("[Game"))
					{
						if(currentGame != null)
						{
							TestBenfordLaw(currentGame);
							TestZipfsLawForMoves(currentGame);
							TestPieceMovement(currentGame);
							SaveMoves(currentGame);
							
							//valueMap.put(currentGame.GetBEloRange(), currentGame.GetBEvaluation());
							//valueMap.put(currentGame.GetWEloRange(), currentGame.GetWEvaluation());
							//games.add(currentGame);
						}
						currentGame = new Game();
					}
					else if(line.startsWith("[WhiteElo"))
					{
						currentGame.wElo = Integer.parseInt(GetValue(line));
					}				
					else if(line.startsWith("[BlackElo"))
					{
						currentGame.bElo = Integer.parseInt(GetValue(line));
					}
					else if(line.startsWith("[GID"))
					{
						if(currentMove != null && Math.abs(currentMove.Eval) <= 300)
						{
							if(currentMove.Turn.contains("-w"))
							{
								if(!currentMove.IsCaptureMove())
								{
									currentGame.wMoves.add(currentMove);
								}								
							}
							else
							{
								if(!currentMove.IsCaptureMove())
								{
									currentGame.bMoves.add(currentMove);
								}								
							}
						}						
						currentMove = new Move();					
					}				
					else if(line.startsWith("[MovePlayed"))
					{
						String current = line.split(" ")[1];
						currentMove.MovePlayed = current.substring(1, current.lastIndexOf("\""));
					}				
					else if(line.startsWith("[EngineMove"))
					{
						currentMove.EngineMove = GetValue(line);
					}				
					else if(line.startsWith("[Eval"))
					{
						currentMove.Eval = Integer.parseInt(GetValue(line));
					}				
					else if(line.startsWith("[Depth "))
					{
						currentMove.Depth = GetValue(line);
					}
					else if(line.startsWith("[NumLegalMoves"))
					{
						currentMove.NumLegalMoves = Integer.parseInt(GetValue(line));
					}				
					else if(line.startsWith("[Turn"))
					{
						currentMove.Turn = GetValue(line);
					}
					else if(line.startsWith("[LegalMoves "))
					{
						String err = null;
						try
						{
							if(currentMove.NumLegalMoves > 0)
							{
								//skip 4 lines and start reading evaluation
								int count = 4;
								while(count > 0)
								{
									line = br.readLine();
									count--;
								}
								
								while(count < currentMove.NumLegalMoves && (line = br.readLine()) != null)
								{
									MoveEvaluation eval = new MoveEvaluation();
									String[] moveArray = line.split("\\s+");
									eval.Move = moveArray[0];
									for(int i = 1; i < moveArray.length;i++)
									{
										try
										{
											err = FixEvaluation(moveArray[i]);
											eval.Evaluation.add(Integer.parseInt(FixEvaluation(moveArray[i])));
										}
										catch(Exception exp)
										{
											//some issue with evaluation value
											eval.Evaluation.add(0);
										}
										
									}
									currentMove.LegalMoves.add(eval);	
									count++;
								}
							}
						}
						catch(Exception exp)
						{
							System.out.println(err);
							System.out.println(Arrays.toString(exp.getStackTrace()));
							//inner exception, continue with other moves
						}
						
					}
				}
				
				if(currentMove != null && Math.abs(currentMove.Eval) <= 300)
				{
					if(currentMove.Turn.contentEquals("-w"))
					{
						if(!currentMove.IsCaptureMove()){
							currentGame.wMoves.add(currentMove);
						}						
					}
					else
					{
						if(!currentMove.IsCaptureMove()){
							currentGame.bMoves.add(currentMove);
						}						
					}
				}
				
				if(currentGame != null)
				{
					TestBenfordLaw(currentGame);
					TestZipfsLawForMoves(currentGame);
					TestPieceMovement(currentGame);
					SaveMoves(currentGame);
					
					//valueMap.put(currentGame.GetBEloRange(), currentGame.GetBEvaluation());
					//valueMap.put(currentGame.GetWEloRange(), currentGame.GetWEvaluation());
					//games.add(currentGame);
					//no need to add games to memory, this saves memory
				}
				
				System.out.println("..............");
				
				System.out.println("Done reading games...");
				
				
			}
			catch(Exception e)
			{
				System.out.println(Arrays.toString(e.getStackTrace()));
			}
			finally
			{
				if(br != null)
				{
					br.close();
				}								
			}
		}
		
		bw.write("#### Benford's law test ###\n");
		
		System.out.println("Testing Benford's law....");
		PrintTestResults(benfordArray);
		System.out.println("Done, writing results to "+args[1]);
		
		bw.write("#### Zipf's law test ###\n");
		
		System.out.println("Testing Zip's law....");
		
		System.out.println("Testing Zipf's law....");
		PrintTestResults(valueMap);
		System.out.println("Done, writing results to "+args[1]);
		
		System.out.println("Testing Piece frequency ....");
		PrintTestResults1(pieceMoveMap);
		System.out.println("Done, writing results to "+args[1]);
		
		System.out.println("Printing moves on to file");
		SaveMoveInfo();
		
//		bw.write("#### Zipf's law test - when played Against Higher rated ###\n");
//		System.out.println("Testing Zipf's law....when played Against Higher rated");
//		PrintTestResults(lowRateValueMap);
//		System.out.println("Done, writing results to "+args[1]);
//		
//		bw.write("#### Zipf's law test - when played Against Lower rated ###\n");
//		System.out.println("Testing Zipf's law.... when played Against Lower rated");
//		PrintTestResults(highRateValueMap);
//		System.out.println("Done, writing results to "+args[1]);
		
		bw.close();
	}
	
	public static String GetValue(String inp)
	{
		String current = inp.split("\"")[1];
		return current;
	}
	
	public static void SaveMoves(Game g)
	{
		if(!movesByRating.containsKey(g.GetWEloRange()+1))
		{
			movesByRating.put(g.GetWEloRange()+1,g.GetWMoves());
		}
		else
		{
			ArrayList<Integer> list = movesByRating.get(g.GetWEloRange()+1);
			list.addAll(g.GetWMoves());
			movesByRating.put(g.GetWEloRange()+1, list);
		}
		
		if(!movesByRating.containsKey(g.GetBEloRange()+2))
		{
			movesByRating.put(g.GetBEloRange()+2,g.GetBMoves());
		}
		else
		{
			ArrayList<Integer> list = movesByRating.get(g.GetBEloRange()+2);
			list.addAll(g.GetBMoves());
			movesByRating.put(g.GetBEloRange()+2, list);
		}
	}
	
	public static void TestPieceMovement(Game g)
	{
		//send false,true to get only white moves
		TreeMap<String,Double> moves = g.GetMovePreference(false, true);
		int rating = g.GetWEloRange();
		double[] arr = null;
		if(pieceMoveMap.containsKey(rating))
		{
			arr = pieceMoveMap.get(rating);
		}
		else
		{
			arr = new double[6];
		}
		int i = 0;
		for(Entry<String,Double> entry : moves.entrySet())
		{
			arr[i] = arr[i] + entry.getValue();
			i++;
		}
		i = 0;
		
		//remove check cases
		arr[1] = arr[1] - g.checksForLastAccesedMoves;
		
		pieceMoveMap.put(rating, arr);
		
		//both false gets only black moves
		moves = g.GetMovePreference(false, false);
		rating = g.GetBEloRange();
		
		if(pieceMoveMap.containsKey(rating))
		{
			arr = pieceMoveMap.get(rating);
		}
		else
		{
			arr = new double[6];
		}
		
		for(Entry<String,Double> entry : moves.entrySet())
		{
			
			arr[i] = arr[i] + entry.getValue();
			i++;
		}
		
		//remove check cases
		arr[1] = arr[1] - g.checksForLastAccesedMoves;
		pieceMoveMap.put(rating, arr);
	}
	
	public static void TestZipfsLawForMoves(Game g)
	{
		int[] bArray = null;
		int[] wArray = null;
		if(valueMap.containsKey(g.GetBEloRange()))
		{
			bArray = valueMap.get(g.GetBEloRange());
		}
		else
		{
			bArray = new int[50];
		}
		
		if(valueMap.containsKey(g.GetWEloRange()))
		{
			wArray = valueMap.get(g.GetWEloRange());
		}
		else
		{
			wArray = new int[50];
		}
		int[] arr = g.GetWEvaluation();
		for(int i = 0; i < arr.length;i++)
		{
			wArray[i] = wArray[i]+arr[i];
		}
		
		arr = g.GetWEvaluation();
		for(int i = 0; i < arr.length;i++)
		{
			bArray[i] = bArray[i]+arr[i];
		}
		
		
		valueMap.put(g.GetBEloRange(), bArray);
		valueMap.put(g.GetWEloRange(), wArray);
		
		if(g.bElo > g.wElo)
		{
			//black Elo is greater than white Elo
			if(lowRateValueMap.containsKey(g.GetWEloRange()))
			{
				wArray = lowRateValueMap.get(g.GetWEloRange());
			}
			else
			{
				wArray = new int[50];
			}
			arr = g.GetWEvaluation();
			for(int i = 0; i < arr.length;i++)
			{
				wArray[i] = wArray[i]+arr[i];
			}
			lowRateValueMap.put(g.GetWEloRange(), wArray);
			
			if(highRateValueMap.containsKey(g.GetBEloRange()))
			{
				bArray = highRateValueMap.get(g.GetBEloRange());
			}
			else
			{
				bArray = new int[50];
			}
			arr = g.GetBEvaluation();
			for(int i = 0; i < arr.length;i++)
			{
				bArray[i] = bArray[i]+arr[i];
			}
			highRateValueMap.put(g.GetBEloRange(), bArray);
			
		}
		else
		{
			if(highRateValueMap.containsKey(g.GetWEloRange()))
			{
				wArray = highRateValueMap.get(g.GetWEloRange());
			}
			else
			{
				wArray = new int[50];
			}
			arr = g.GetWEvaluation();
			for(int i = 0; i < arr.length;i++)
			{
				wArray[i] = wArray[i]+arr[i];
			}
			highRateValueMap.put(g.GetWEloRange(), wArray);
			
			if(lowRateValueMap.containsKey(g.GetBEloRange()))
			{
				bArray = lowRateValueMap.get(g.GetBEloRange());
			}
			else
			{
				bArray = new int[50];
			}
			arr = g.GetBEvaluation();
			for(int i = 0; i < arr.length;i++)
			{
				bArray[i] = bArray[i]+arr[i];
			}
			lowRateValueMap.put(g.GetBEloRange(), bArray);
		}		
	}
	
	public static void TestBenfordLaw(Game g) throws IOException
	{	
		for(Move m : g.wMoves)
		{
			for(MoveEvaluation eval : m.LegalMoves)
			{
				for(int val : eval.Evaluation)
				{
					benfordArray[Move.BenfordValue(val)] = benfordArray[Move.BenfordValue(val)]+1; 
				}
			}
		}			
		for(Move m : g.bMoves)
		{
			for(MoveEvaluation eval : m.LegalMoves)
			{
				for(int val : eval.Evaluation)
				{
					benfordArray[Move.BenfordValue(val)] = benfordArray[Move.BenfordValue(val)]+1; 
				}
			}
		}
	}
	
	public static void PrintTestResults1(TreeMap<Integer,double[]> val) throws IOException
	{
		Iterator itr = val.keySet().iterator();
		while(itr.hasNext())
		{
			int rating = (int) itr.next();
			double[] arr = pieceMoveMap.get(rating);
			Double total = 0.0;
			for(int i = 0; i < arr.length;i++)
			{
				total = total + arr[i];			
			}
			bw.write("##### rating : "+rating+" #####\n");
			for(int i = 0; i < arr.length;i++)
			{
				bw.write(pieces[i]+" count : "+arr[i]+"  percentage : "+(((arr[i]*1.0/total)*100))+"% \n");
			}
			
		}
		bw.flush();
	}
	
	public static void PrintTestResults(TreeMap<Integer,int[]> val) throws IOException
	{
		Iterator itr = val.keySet().iterator();
		while(itr.hasNext())
		{
			int rating = (int) itr.next();
			int[] arr = valueMap.get(rating);
			int total = 0;
			for(int i = 1; i < arr.length;i++)
			{
				total = total + arr[i];			
			}
			bw.write("##### rating : "+rating+" #####\n");
			for(int i = 1; i < arr.length;i++)
			{
				bw.write("Move position : "+i+" count : "+arr[i]+"  percentage : "+(((arr[i]*1.0/total)*100))+"% \n");
			}
			
		}
		bw.flush();
	}
	
	public static void PrintTestResults(int[] arr) throws IOException
	{
		int total = 0;
		for(int i = 1; i < arr.length;i++)
		{
			total = total + arr[i];			
		}
		
		for(int i = 1; i < arr.length;i++)
		{
			bw.write("Digit : "+i+" count : "+arr[i]+"  percentage : "+(((arr[i]*1.0/total)*100))+"% \n");			
		}
		
		bw.flush();
	}
	
	public static void SaveMoveInfo()
	{
		Set<Integer> keys = movesByRating.keySet();
		for(Integer i : keys)
		{
			try {
				bw.write(i + " "+ Arrays.toString(movesByRating.get(i).toArray()));
				bw.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String FixEvaluation(String str)
	{
		//case where evaluation goes beyond 1000
		if(str.endsWith("x") || str.endsWith("X"))
		{
			return str.substring(0, str.length()-1);
		}
		
		//Not sure where this case occurs
		if(str.startsWith("+M") || str.startsWith("-M"))
		{
			return str.charAt(0)+""+str.substring(2, str.length());
		}
		
		//Not sure where this case occurs
		if(str.endsWith("C") || str.endsWith("c"))
		{
			return str.substring(0, str.length()-1);
		}
		
		//Not sure where this case occurs
		if(str.equals("PRUN"))
		{
			return "0";
		}
		
		//Eval not available
		if(str.contains("n.a"))
		{
			return "0";
		}
		
		try
		{
			Integer.parseInt(str);
		}
		catch(NumberFormatException ex)
		{
			return "0";
		}
		catch(Exception e)
		{
			return "0";
		}
		
		return str;
	}
}
