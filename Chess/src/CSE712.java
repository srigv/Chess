import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.nio.file.Paths;

public class CSE712 {
	static List<Game> games = new ArrayList<Game>();
	static BufferedWriter bw;
	static BufferedWriter bw_fen;
	static int[] benfordArray = new int[10];
	static int[] zipfLawStats = new int[50];
	static TreeMap<Integer, int[]> valueMap = new TreeMap<Integer, int[]>();
	static TreeMap<Integer, int[]> lowRateValueMap = new TreeMap<Integer, int[]>();
	static TreeMap<Integer, int[]> highRateValueMap = new TreeMap<Integer, int[]>();
	static TreeMap<Integer, double[]> pieceMoveMap = new TreeMap<Integer, double[]>();
	static HashMap<String, ArrayList<MoveByFEN>> FENMap = new HashMap<String, ArrayList<MoveByFEN>>();
	static TreeMap<Integer, ArrayList<Integer>> movesByRating = new TreeMap<Integer, ArrayList<Integer>>();
	static String[] pieces = new String[] { "Bishop", "King", "Knight", "Pawn", "Queen", "Rook" };
	static File fenOutDir = null;
	static File fenWriteDir = null;

	static File FenDumpDir = null;
	static File GameDumpDir = null;
	static int DumpType = 0;

	
	static int fenStart = 0;
	static int totalFenCount = 0;
	static int discardedFenCount = 0;
	static int discardedGameCount = 0;
	static int first8MoveCount = 0;
	static int zeroMoveNumCount = 0;

	static HashMap<FEN, FENProp> fenCountMap = new HashMap<FEN, FENProp>();

	public static void PrintHelp() {
		System.out.println("\n##########################################\n");
		System.out.println("----Usage----");
		System.out.println("java -jar CSE712.jar [INPUT(FILE | DIRECTORY)] [OUTFILE]");
		System.out.println("\n----Extra options----");
		System.out.println("--ff [FILE EXTENSION]");
		System.out.println("use this option to filter input files by extension Eg : --ff .aif");

		System.out.println("\n--rf [REGEX]");
		System.out.println("use this option to filter input files by regex(JAVA)");

		System.out.println("\nEg: --rf (?=.*2100)(?=.*aif) to filter all files contaning 2100 and aif strings");

		System.out.println("--fen");
		System.out.println("use this option to use fen segregation piece Eg : --fen");

		System.out.println("--solr");
		System.out.println("use this option to use solr data gen from fen part files Eg : --solr");
		System.out.println("\n##########################################\n");

		System.out.println("--buffer");
		System.out.println("use this option to use fen part index files Eg : --buffer 2");
		System.out.println("\n##########################################\n");
	}

	public static void main(String[] args) throws IOException {

		int buffer = 0;
		if (args.length == 0) {
			PrintHelp();
			return;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--help")) {
				PrintHelp();
				return;
			}
		}

		if (args.length < 2) {
			System.out.println("Insufficient number of arguments, min required 2 but found " + args.length);
			return;
		}

		Boolean fenFiles = false;
		FileFilter filter = null;
		int fileCount = 0;
		int doneFileCount = 0;

		File fenTempDir = null;
		File uniqFenDir = null;

		boolean isSolrDataGen = false;
		boolean isFenTest = false;
		boolean isSanityTest = false;

		
		int fenBatchSize = 2500000;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--buffer")) {
				if (i + 1 < args.length) {
					try {
						buffer = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--buffer isn't specified or has a wrong value " + e.getMessage());
						return;
					}
				}
			} else if (args[i].equals("--dumpType")) {
				if (i + 1 < args.length) {
					try {
						DumpType = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--buffer isn't specified or has a wrong value " + e.getMessage());
						return;
					}
				}
			} else if (args[i].equals("--fenDump")) {
				if (i + 1 < args.length) {
					try {
						FenDumpDir = new File(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--FenDump isn't specified or has a wrong value " + e.getMessage());
						return;
					}
				}
			} else if (args[i].equals("--gameDump")) {
				if (i + 1 < args.length) {
					try {
						GameDumpDir = new File(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--gameDump isn't specified or has a wrong value " + e.getMessage());
						return;
					}
				}
			} else if (args[i].equals("--rf")) {
				if (i + 1 < args.length) {
					filter = new MyFilter("", args[i + 1]);
				}
			} else if (args[i].equals("--ff")) {
				if (i + 1 < args.length) {
					filter = new MyFilter(args[i + 1], "");
				}
			} else if (args[i].equals("--fen")) {
				fenFiles = true;
				fenOutDir = new File((new File(args[1]).getParent()) == null
						? Paths.get(".").toAbsolutePath().normalize().toString() : (new File(args[1]).getParent()));
				String today = Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE);
				fenWriteDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE));
				fenTempDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE) + File.separator + "Temp");

				if (!fenWriteDir.exists()) {
					fenWriteDir.mkdirs();
				}

				if (!fenTempDir.exists()) {
					fenTempDir.mkdirs();
				}
			} else if (args[i].equals("--solr")) {
				fenOutDir = new File((new File(args[1]).getParent()) == null
						? Paths.get(".").toAbsolutePath().normalize().toString() : (new File(args[1]).getParent()));
				String today = Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE);
				fenWriteDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE));
				fenTempDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE) + File.separator + "Temp");

				if (!fenWriteDir.exists()) {
					fenWriteDir.mkdirs();
				}

				if (!fenTempDir.exists()) {
					fenTempDir.mkdirs();
				}
				isSolrDataGen = true;
			} else if (args[i].equals("--justfen")) {
				fenOutDir = new File((new File(args[1]).getParent()) == null
						? Paths.get(".").toAbsolutePath().normalize().toString() : (new File(args[1]).getParent()));
				String today = Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE);
				uniqFenDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE) + File.separator + "UNIQ");

				if (!uniqFenDir.exists()) {
					uniqFenDir.mkdirs();
				}
				System.out.println("--justfen option found");
				System.out.println("Going to test FEN memory footprint");
				isFenTest = true;
			} else if (args[i].equals("--fenBatchStart")) {
				if (i + 1 < args.length) {
					try {
						fenStart = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--fenBatchStart isn't specified or has a bad value " + e.getMessage());
						return;
					}
				}
			} else if (args[i].equals("--fenBatchSize")) {
				if (i + 1 < args.length) {
					try {
						fenBatchSize = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.out.println("--fenBatchSize isn't specified or has a bad value " + e.getMessage());
						return;
					}
				}
			}
			else if (args[i].equals("--sanity")) {
				isSanityTest = true;
				
				fenFiles = true;
				fenOutDir = new File((new File(args[1]).getParent()) == null
						? Paths.get(".").toAbsolutePath().normalize().toString() : (new File(args[1]).getParent()));
				String today = Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE);
				fenWriteDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE));
				fenTempDir = new File(fenOutDir.getAbsolutePath() + File.separator + today + File.separator
						+ Calendar.getInstance().get(Calendar.YEAR) + "_"
						+ (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_"
						+ Calendar.getInstance().get(Calendar.DATE) + "_" + Calendar.getInstance().get(Calendar.HOUR)
						+ "_" + Calendar.getInstance().get(Calendar.MINUTE) + File.separator + "Temp");

				if (!fenWriteDir.exists()) {
					fenWriteDir.mkdirs();
				}

				if (!fenTempDir.exists()) {
					fenTempDir.mkdirs();
				}
			}
		}

		System.out.println("Handling file " + args[0]);
		File input = new File(args[0]);

		int fenPartFileCount = 1;
		// HashMap<String,FENbyUser> userMap = new HashMap<String,FENbyUser>();

		if (!input.exists()) {
			return;
		}
		File[] fileArray;
		if (input.isDirectory()) {
			System.out.println(input.getAbsolutePath());
			if (filter != null) {
				fileArray = input.listFiles(filter);
			} else {
				fileArray = input.listFiles();
			}
		} else {
			fileArray = new File[] { input };
		}

		Arrays.sort(fileArray); //sorting files by name
		System.out.println("Reading games..." + fileArray.length);

		fileCount = fileArray.length;

		FileOutputStream out = new FileOutputStream(args[1]);
		bw = new BufferedWriter(new OutputStreamWriter(out));

		// Pattern datePattern =
		// Pattern.compile("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}");
		int gameCount = 0;
		BufferedReader br = null;
		
		if (isSanityTest) {
			System.out.println("intitated FEN Sanity check task..");
			int start = fenStart * fenBatchSize;
			int end = start + fenBatchSize - 1;

			System.out.println("batch start : " + start + ", end : " + end);

			if (FenDumpDir == null || !FenDumpDir.exists()) {
				System.out.println("Problem accesing Fen Dump directory");
				return;
			}

			if (GameDumpDir == null || !GameDumpDir.exists()) {
				System.out.println("Problem accesing Game Dump directory");
				return;
			}

			ConcurrentHashMap<String, Integer> fenCountMap = new ConcurrentHashMap<String, Integer>();
			

			File[] arr = FenDumpDir.listFiles();
			Arrays.sort(arr); // sorting files to keep it consistent across the
								// system
			int count = 0;
			for (File f : arr) {
				try {
					System.out.println("reading FEN from : "+f.getName());
					br = new BufferedReader(new FileReader(f));
					String line = "";
					while ((line = br.readLine()) != null) {
						if (count < start) {
							count++;
							continue;
						}

						if (count >= start && count <= end) {
							fenCountMap.put(line.trim(), 0);
						}
						
						count++;
					}
					
					if(count > end)
					{
						System.out.println("Done collecting nessecary FEN");
						break;							
					}
				} catch (Exception e) {
					System.out.println(e.getMessage() + " at " + e.getStackTrace());
				}
			}

			
			
			System.out.println("collected FEN count : "+fenCountMap.size());
			
			if(fenCountMap.size() == 0)
			{
				return;
			}

			ExecutorService executor = Executors.newFixedThreadPool(16);

			for (File f : fileArray) {
				try {
					Runnable worker = new FENCountCheck(f, fenCountMap);
					executor.execute(worker);
				} catch (Exception e) {
					System.out.println(Arrays.toString(e.getStackTrace()) + "  " + e.getMessage());
				} finally {
					if (br != null) {
						br.close();
					}
				}
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			try {

				FileOutputStream out_1 = new FileOutputStream(
						fenWriteDir.getAbsolutePath() + File.separator + "FEN" + ".txt");
				bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
				Map<String,Integer> map;
				map = MapUtil.sortByValue(fenCountMap);
				WriteFENCountToFile(bw_fen, map);
				bw_fen.close();
				fenCountMap.clear();

				System.out.println("Done with process");
			} catch (Exception e) {
				System.out.print("Couldn't write to file " + e.getMessage());
			}

			PrintMemory();
			System.out.println("Done with the process");

			return;
		}

		// Testing if FEN fits memory or not
		if (isFenTest) {
			System.out.println("intitated FEN memory test..");

			if (FenDumpDir != null && !FenDumpDir.exists()) {
				System.out.println("FenDump dir is not specified");
				return;
			}

			if (GameDumpDir != null && !GameDumpDir.exists()) {
				System.out.println("GameDump dir is not specified");
				return;
			}

			if (DumpType == 1) {
				System.out.println("White moves are being collected");
			} else if (DumpType == 2) {
				System.out.println("Black moves are being collected");
			} else if (DumpType == 3) {
				System.out.println("Games are being collected");
			}
			ConcurrentHashMap<String, Boolean> fenIndexMap = new ConcurrentHashMap<String, Boolean>();
			ConcurrentHashMap<String, Boolean> gameIndexMap = new ConcurrentHashMap<String, Boolean>();
			ExecutorService executor = Executors.newFixedThreadPool(16);
			int fenIndex = 1;
			int gameIndex = 1;
			for (File f : fileArray) {
				try {
					doneFileCount++;
					fileCount--;
					Runnable worker = new FENCollectorThread(f, fenIndexMap, gameIndexMap, DumpType);
					executor.execute(worker);

					// System.out.println("done with "+doneFileCount+" remaining
					// "+fileCount);

				} catch (Exception e) {
					System.out.println(Arrays.toString(e.getStackTrace()) + "  " + e.getMessage());
				} finally {
					if (br != null) {
						br.close();
					}
				}
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			if (DumpType == 1 || DumpType == 2) {
				FileOutputStream out_1 = new FileOutputStream(
						FenDumpDir.getAbsolutePath() + File.separator + "FEN" + ".txt");
				bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
				fenIndex = 1;
				for (Map.Entry<String, Boolean> pair : fenIndexMap.entrySet()) {
					bw_fen.write(pair.getKey());
					bw_fen.newLine();
					fenIndex++;
					if (fenIndex % 1000000 == 0) {
						bw_fen.close();
						out_1 = new FileOutputStream(
								FenDumpDir.getAbsolutePath() + File.separator + "FEN_" + (fenIndex / 1000000) + ".txt");
						bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
					}
				}
				bw_fen.close();
			}

			if (DumpType == 3) {
				gameIndex = 1;

				FileOutputStream out_1 = new FileOutputStream(
						GameDumpDir.getAbsolutePath() + File.separator + "Games" + ".txt");
				bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
				for (Map.Entry<String, Boolean> pair : gameIndexMap.entrySet()) {
					bw_fen.write(pair.getKey());
					bw_fen.newLine();
					gameIndex++;
					if (gameIndex % 1000000 == 0) {
						bw_fen.close();
						out_1 = new FileOutputStream(GameDumpDir.getAbsolutePath() + File.separator + "Games_"
								+ (gameIndex / 1000000) + ".txt");
						bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
					}
				}
				bw_fen.close();
			}

			PrintMemory();
			System.out.println("Done with the process");
			return;
		}

		if (fenFiles) {
			System.out.println("intitated FEN Generation task with prev fen update..");
			int start = fenStart * fenBatchSize;
			int end = start + fenBatchSize - 1;

			System.out.println("batch start : " + start + ", end : " + end);

			if (FenDumpDir == null || !FenDumpDir.exists()) {
				System.out.println("Problem accesing Fen Dump directory");
				return;
			}

			if (GameDumpDir == null || !GameDumpDir.exists()) {
				System.out.println("Problem accesing Game Dump directory");
				return;
			}

			ConcurrentHashMap<String, Boolean> fenIndexMap = new ConcurrentHashMap<String, Boolean>();
			ConcurrentHashMap<String, Integer> gameIndexMap = new ConcurrentHashMap<String, Integer>();
			ConcurrentHashMap<String, FENProp> fenPropMap = new ConcurrentHashMap<String, FENProp>();
			ConcurrentHashMap<String, GameProp> gamePropMap = new ConcurrentHashMap<String, GameProp>();

			File[] arr = FenDumpDir.listFiles();
			Arrays.sort(arr); // sorting files to keep it consistent across the
								// system
			int count = 0;
			if (DumpType != 3) {
				for (File f : arr) {
					try {
						System.out.println("reading FEN from : "+f.getName());
						br = new BufferedReader(new FileReader(f));
						String line = "";
						while ((line = br.readLine()) != null) {
							if (count < start) {
								count++;
								continue;
							}

							if (count >= start && count <= end) {
								fenIndexMap.put(line.trim(), true);
								fenPropMap.put(line.trim(), new FENProp());
							}
							
							count++;
						}
						
						if(count > end)
						{
							System.out.println("Done collecting nessecary FEN");
							break;							
						}
					} catch (Exception e) {
						System.out.println(e.getMessage() + " at " + e.getStackTrace());
					}
				}
			}
			
			int gameIndex = 1;
			arr = GameDumpDir.listFiles();
			Arrays.sort(arr); // even games has to be in the same order,
								// otherwise id will be different
			for (File f : arr) {
				try {
					System.out.println("reading Games from : "+f.getName());
					br = new BufferedReader(new FileReader(f));
					String line = "";
					while ((line = br.readLine()) != null) {
						GameProp gp = new GameProp();
						gp.id = gameIndex;
						gameIndexMap.put(line.trim(), gameIndex);
						gamePropMap.put(line.trim(), gp);
						gameIndex++;
					}
				} catch (Exception e) {
					System.out.println(e.getMessage() + " at " + e.getStackTrace());
				}
			}
			
			System.out.println("collected FEN count : "+fenPropMap.size());
			System.out.println("collected Game count : "+gameIndexMap.size());
			
			if((fenPropMap.size() == 0 && gamePropMap.size() == 0) || gameIndexMap.size() == 0)
			{
				return;
			}

			ExecutorService executor = Executors.newFixedThreadPool(1);
			
			
			if(DumpType != 3)
			{
				for (File f : fileArray) {
					try {
						Runnable worker = new FENIndexDataGenerator(f, fenIndexMap, gameIndexMap, fenPropMap,DumpType);
						executor.execute(worker);
					} catch (Exception e) {
						System.out.println(Arrays.toString(e.getStackTrace()) + "  " + e.getMessage());
					} finally {
						if (br != null) {
							br.close();
						}
					}
				}
			}
			else
			{
				System.out.println("Processing only game data");
				for (File f : fileArray) {
					try {
						Runnable worker = new GamesIndexDataCollector(f, gamePropMap);;
						executor.execute(worker);
					} catch (Exception e) {
						System.out.println(Arrays.toString(e.getStackTrace()) + "  " + e.getMessage());
					} finally {
						if (br != null) {
							br.close();
						}
					}
				}
			}
			

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			try {

				if(DumpType != 3)
				{
					WriteFENToFileWithGameIndexMap(fenPropMap);
					//bw_fen.close();
					fenPropMap.clear();
				}
				else
				{
					WriteGameToFile(gamePropMap);
					//bw_fen.close();
					gamePropMap.clear();
				}
				

				System.out.println("Done with process");
			} catch (Exception e) {
				System.out.print("Couldn't write to file " + e.getMessage());
			}

			PrintMemory();
			System.out.println("Done with the process");

			return;
		}

		if (!isSolrDataGen && !fenFiles) {
			for (File f : fileArray) {
				boolean hasMoveNumIssue = false;
				boolean hasGameIdIssue = false;
				System.out.println("Reading games from " + f.getName());

				try {
					br = new BufferedReader(new FileReader(f));
				} catch (Exception exp) {
					System.out.println("Issue with " + f.getName() + " : " + exp.getMessage());
				}

				if (!fenFiles) {
					try {
						String line;
						Game currentGame = null;
						Move currentMove = null;
						while ((line = br.readLine()) != null) {
							if (line.startsWith("[Game")) {
								if (currentGame != null) {
									TestBenfordLaw(currentGame);
									TestZipfsLawForMoves(currentGame);
									TestPieceMovement(currentGame);
									SaveMoves(currentGame);
								}
								currentGame = new Game();
							} else if (line.startsWith("[WhiteElo")) {
								String elo = GetValue(line);
								System.out.println("###" + elo);
								currentGame.wElo = Integer.parseInt(elo.length() > 0 ? elo : "0");
							} else if (line.startsWith("[BlackElo")) {
								String elo = GetValue(line);
								// currentGame.wElo =
								// Integer.parseInt(elo.length() > 0 ? elo :
								// "0");
								currentGame.bElo = Integer.parseInt(elo.length() > 0 ? elo : "0");
							} else if (line.startsWith("[GID")) {
								if (currentMove != null && Math.abs(currentMove.Eval) <= 300) {
									AddToFENMap(currentMove);
									if (currentMove.Turn != null) {
										// TODO this one needs fix
										if (currentMove.Turn.contains("-w")) {
											if (!currentMove.IsCaptureMove()) {
												currentGame.wMoves.add(currentMove);
											}
										} else {
											if (!currentMove.IsCaptureMove()) {
												currentGame.bMoves.add(currentMove);
											}
										}
									}

								}
								currentMove = new Move();
								currentMove.Gid = Utils.GetQuotedValue(line);
							} else if (line.startsWith("[MovePlayed")) {
								String current = line.split(" ")[1];
								currentMove.MovePlayed = current.substring(1, current.lastIndexOf("\""));
							} else if (line.startsWith("[EngineMove")) {
								currentMove.EngineMove = Utils.GetQuotedValue(line);
							} else if (line.startsWith("[Eval")) {
								currentMove.Eval = Integer.parseInt(GetValue(line));
							} else if (line.startsWith("[Depth ")) {
								currentMove.Depth = Utils.GetQuotedValue(line);
							} else if (line.startsWith("[NumLegalMoves")) {
								currentMove.NumLegalMoves = Integer.parseInt(GetValue(line));
							} else if (line.startsWith("[Turn") || line.startsWith("[MoveNo")) {
								currentMove.Turn = Utils.GetQuotedValue(line);
							} else if (line.startsWith("[FEN")) {
								currentMove.FEN = Utils.GetQuotedValue(line);
							} else if (line.startsWith("[LegalMoves ")) {
								String err = null;
								try {
									if (currentMove.NumLegalMoves > 0) {
										// skip 4 lines and start reading
										// evaluation
										int count = 4;
										while (count > 0) {
											line = br.readLine();
											count--;
										}

										while (count < currentMove.NumLegalMoves && (line = br.readLine()) != null) {
											MoveEvaluation eval = new MoveEvaluation();
											String[] moveArray = line.split("\\s+");
											eval.Move = moveArray[0];
											for (int i = 1; i < moveArray.length; i++) {
												try {
													err = FixEvaluation(moveArray[i]);
													eval.Evaluation.add(Integer.parseInt(FixEvaluation(moveArray[i])));
												} catch (Exception exp) {
													// some issue with
													// evaluation value
													eval.Evaluation.add(0);
												}

											}
											currentMove.LegalMoves.add(eval);
											count++;
										}
									}
								} catch (Exception exp) {
									System.out.println(err);
									System.out.println(Arrays.toString(exp.getStackTrace()));
									// inner exception, continue with other
									// moves
								}

							}
						}

						if (currentMove != null && Math.abs(currentMove.Eval) <= 300) {
							if (currentMove.Turn.contentEquals("-w")) {
								if (!currentMove.IsCaptureMove()) {
									currentGame.wMoves.add(currentMove);
								}
							} else {
								if (!currentMove.IsCaptureMove()) {
									currentGame.bMoves.add(currentMove);
								}
							}
						}

						if (currentGame != null) {
							TestBenfordLaw(currentGame);
							TestZipfsLawForMoves(currentGame);
							TestPieceMovement(currentGame);
							SaveMoves(currentGame);
							// no need to add games to memory, this saves memory
						}

						System.out.println("..............");

						System.out.println("Done reading games...");

					} catch (Exception e) {
						System.out.println(Arrays.toString(e.getStackTrace()));
					} finally {
						if (br != null) {
							br.close();
						}
					}
				} else {
					try {
						doneFileCount++;
						fileCount--;
						String line;
						HashMap<GamePropEum, String> props = new HashMap<GamePropEum, String>();
						Boolean isValidDate = false;
						while ((line = br.readLine()) != null) {
							if (line.startsWith("[GameID")) {
								gameCount++;
								if (gameCount % 10000 == 0) {
									System.out.println("read " + gameCount + " games..");
									PrintMemory();
									System.out.println("done with " + doneFileCount + " remaining " + fileCount);
								}
								isValidDate = true; // TODO Some of the dates
													// are missing, need to fix
													// the issue
								String str = Utils.GetQuotedValue(line);
								props = Utils.GetGameProps(str);
								// if(datePattern.matcher(props.get(GamePropEum.TOURNMENT_DATE)).matches())
								// {
								// isValidDate = true;
								// }
							} else if (isValidDate) {
								FEN fen = new FEN(line);
								totalFenCount++;
								if (fen.isValidFen()) {
									if (fen.MoveNum() == 0) {
										zeroMoveNumCount++;
									}

									if (fen.MoveNum() == 0 && !hasMoveNumIssue) {
										if (!line.startsWith("{")) {
											System.out.println("found move num 0 in " + f.getName() + " at " + line);
											hasMoveNumIssue = true;
										}
									}
									try {
										if (!fenCountMap.containsKey(fen)) {
											fenCountMap.put(fen, new FENProp());
										}
									} catch (Exception e) {
										System.out.println(e.getMessage());
									}

									// fen.count = fenCountMap.get(fen)+1;
									if (props.containsKey(GamePropEum.GAME_ID)) {
										FENProp fProp = fenCountMap.get(fen);
										fProp.UpdateTurnCount(fen.moveNum, 1);
										fProp.UpdateFENProp(1, props.get(GamePropEum.GAME_ID),
												props.get(GamePropEum.GAME_RESULT));
										fenCountMap.put(fen, fProp);
									} else if (!hasGameIdIssue) {
										System.out.println("has Game id issue in " + f.getName());
										hasGameIdIssue = true;
									}
								} else {
									if (fen.isOpeningMove()) {
										first8MoveCount++;
									} else {
										discardedFenCount++;
									}
								}
								if (fenCountMap.size() == 100000) {
									FileOutputStream out_1 = new FileOutputStream(
											fenTempDir + File.separator + "FEN_" + fenPartFileCount + ".txt");
									bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
									WriteFENToFile(bw_fen);
									fenPartFileCount++;
									fenCountMap.clear();
								}
							} else {
								discardedGameCount++;
							}
						}

					} catch (Exception e) {
						System.out.println(Arrays.toString(e.getStackTrace()));

					} finally {
						if (br != null) {
							br.close();
						}
					}
				}

			}

			if (!fenFiles) {
				bw.write("#### Benford's law test ###\n");

				System.out.println("Testing Benford's law....");
				PrintTestResults(benfordArray);
				System.out.println("Done, writing results to " + args[1]);

				bw.write("#### Zipf's law test ###\n");

				System.out.println("Testing Zip's law....");

				System.out.println("Testing Zipf's law....");
				PrintTestResults(valueMap);
				System.out.println("Done, writing results to " + args[1]);

				System.out.println("Testing Piece frequency ....");
				PrintTestResults1(pieceMoveMap);
				System.out.println("Done, writing results to " + args[1]);

				System.out.println("Printing moves on to file");
				SaveMoveInfo();
			} else {
				System.out.println("Saving FEN info");
				FileOutputStream out_1 = new FileOutputStream(
						fenTempDir + File.separator + "FEN_" + fenPartFileCount + ".txt");
				bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
				WriteFENToFile(bw_fen);
				fenPartFileCount++;
				fenCountMap.clear();
				fenCountMap.clear();
				bw_fen.close();
			}

			bw.close();

			PrintMemory();

			System.out.print("Done with the process");
		} else if (!isSolrDataGen) {
			HashMap<String, FENProp> finalMap = new HashMap<String, FENProp>();

			// File[] outFiles = fenTempDir.listFiles(new
			// MyFilter(".txt","(?=.*"+(new File(args[1])).getName()+"_FEN*)"));
			// File[] outFiles = fenTempDir.listFiles();
			int fCount = 0;
			for (File f : fileArray) {
				fCount++;
				System.out.print(fCount + " ");
				if (fCount % 10 == 0) {
					PrintMemory();
				}
				System.out.println("reading from " + f.getName());
				try {
					br = new BufferedReader(new FileReader(f));
				} catch (Exception exp) {
					System.out.println("Issue with " + f.getName() + " : " + exp.getMessage());
				}

				String line = null;
				String currFen = "";
				while ((line = br.readLine()) != null) {
					if (line.startsWith("$FEN$")) {
						String[] arr = line.substring(5).split(":");
						currFen = arr[0];
						try {
							if (!finalMap.containsKey(currFen)) {
								finalMap.put(arr[0], new FENProp());
							}
							FENProp fProps = finalMap.get(currFen);
							fProps.count += Integer.parseInt(arr[1]);
							finalMap.put(currFen, fProps);
						} catch (Exception e) {
							// TODO: handle exception
						}
					} else if (line.startsWith("$GAME_RESULT$")) {
						if (finalMap.containsKey(currFen)) {
							FENProp fProps = finalMap.get(currFen);
							String[] arr = line.substring(13).split(Pattern.quote("||"));
							for (String str : arr) {
								String[] pairs = str.split(":");
								if (pairs.length == 2) {
									fProps.AddToResultMap(pairs[0], pairs[1]);
								}
							}

							finalMap.put(currFen, fProps);
						}
					} else if (line.startsWith("$TURNWISE_COUNT$")) {
						if (finalMap.containsKey(currFen)) {
							FENProp fProps = finalMap.get(currFen);
							String[] arr = line.substring(16).split(Pattern.quote("||"));
							for (String str : arr) {
								String[] pairs = str.split(":");
								try {
									if (pairs.length == 2) {
										fProps.UpdateTurnCount(Integer.parseInt(pairs[0].trim()),
												Integer.parseInt(pairs[1].trim()));
									}
								} catch (Exception e) {
									// what to do
								}
							}

							finalMap.put(currFen, fProps);
						}
					}
				}
			}

			PrintMemory();

			try {

				FileOutputStream out_1 = new FileOutputStream(
						fenWriteDir.getAbsolutePath() + File.separator + "FEN" + ".txt");
				bw_fen = new BufferedWriter(new OutputStreamWriter(out_1));
				WriteFENToFile(bw_fen, finalMap);
				bw_fen.close();
				finalMap.clear();

				System.out.println("Done with process");
			} catch (Exception e) {
				System.out.print("Couldn't write to file " + e.getMessage());
			}
		}
	}

	
	public static void WriteFENToFile(BufferedWriter bw) {
		FENCountQueue queue = new FENCountQueue();
		for (Map.Entry<FEN, FENProp> pair : fenCountMap.entrySet()) {
			pair.getKey().count = pair.getValue().count;
			pair.getKey().GameResultMap = pair.getValue().gameResultMap;
			pair.getKey().TurnWiseCount = pair.getValue().turnWiseCount;
			queue.queue.add(pair.getKey());
		}

		fenCountMap.clear();
		try {
			while (!queue.queue.isEmpty()) {
				FEN ele = queue.queue.poll();
				bw.write("$FEN$" + ele.justFen() + ":" + ele.count);
				bw.newLine();
				bw.write("$GAME_RESULT$");
				for (Map.Entry<String, String> pair : ele.GameResultMap.entrySet()) {
					bw.write(pair.getKey() + ":" + pair.getValue() + "||");
				}
				bw.newLine();
				bw.write("$TURNWISE_COUNT$");
				for (Map.Entry<Integer, Integer> pair : ele.TurnWiseCount.entrySet()) {
					bw.write(pair.getKey() + ":" + pair.getValue() + "||");
				}
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// this is currently being used
	public static void WriteFENToFile(BufferedWriter bw, HashMap<String, FENProp> map) {
		FENCountQueue queue = new FENCountQueue();
		HashMap<String, String> GameResultMap = new HashMap<String, String>();
		HashMap<String, Integer> GameIndexMap = new HashMap<String, Integer>();
		for (Map.Entry<String, FENProp> pair : map.entrySet()) {
			// doing partial work to save heap space -- Space time tradeoff
			for (Map.Entry<String, String> result : pair.getValue().gameResultMap.entrySet()) {
				GameResultMap.put(result.getKey(), result.getValue());
			}
		}

		try {
			int count = 0;

			int fenCount = 0;
			int fenBatchSize = 10000;

			int gameCount = 0;
			int gameBatchSize = 10000;

			String gameFileName = fenWriteDir.getAbsolutePath() + File.separator + "Games.txt";
			FileOutputStream out_1 = new FileOutputStream(gameFileName);
			bw = new BufferedWriter(new OutputStreamWriter(out_1));
			System.out.println("writing games to " + gameFileName);
			bw.write("[");
			for (Map.Entry<String, String> pair : GameResultMap.entrySet()) {
				GameIndexMap.put(pair.getKey(), count + 1);
				bw.write("{\"id\":" + (count + 1) + ",");
				bw.newLine();
				bw.write("\"GameId\":\"" + pair.getKey() + "\",");
				bw.newLine();
				bw.write("\"Result\":\"" + pair.getValue() + "\"}");
				gameCount++;
				count++;

				if (gameCount % gameBatchSize == 0) {
					bw.write("]");
					bw.flush();
					bw.close();
					bw.write("[");
					gameFileName = fenWriteDir.getAbsolutePath() + File.separator + "Games_"
							+ (gameCount / gameBatchSize) + ".txt";
					out_1 = new FileOutputStream(gameFileName);
					bw = new BufferedWriter(new OutputStreamWriter(out_1));
					System.out.println("writing games to " + gameFileName);
				} else {
					bw.write(",");
				}
			}
			bw.flush();
			bw.close();

			String fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN.txt";
			out_1 = new FileOutputStream(fenFileName);
			bw = new BufferedWriter(new OutputStreamWriter(out_1));

			GameResultMap.clear(); // not needed anymore, we have what we need
									// in GameIndexMap

			System.out.println("map size " + map.size());
			System.out.println("writing FENs to " + fenFileName);

			for (Map.Entry<String, FENProp> pair : map.entrySet()) {
				FEN fen = new FEN(pair.getKey(), pair.getValue().count);
				fen.GameResultMap = pair.getValue().gameResultMap;
				fen.TurnWiseCount = pair.getValue().turnWiseCount;
				queue.queue.add(fen);
			}

			map.clear(); // saving memory by clearing the unused map
			PrintMemory();

			System.out.println("Queue size " + queue.queue.size());

			bw.write("[");
			while (!queue.queue.isEmpty()) {
				FEN ele = queue.queue.poll();
				// bw.write("{\"id\":"+(count+1)+",");
				bw.write("\"id\":\"" + ele.justFen() + "\",");
				bw.newLine();
				bw.write("\"Count\":" + ele.count + ",");
				bw.newLine();
				bw.write("\"Games\":[");
				int ind = 0;
				for (Map.Entry<String, String> pair : ele.GameResultMap.entrySet()) {
					ind++;
					int GameInd = -1;
					if (GameIndexMap.containsKey(pair.getKey())) {
						GameInd = GameIndexMap.get(pair.getKey());
						bw.write(GameInd + "");
					}

					bw.write((ind < ele.GameResultMap.size() ? "," : ""));
				}
				bw.write("],");
				bw.newLine();
				bw.write("\"TurnwiseCount\":[");
				ind = 0;
				for (Map.Entry<Integer, Integer> pair : ele.TurnWiseCount.entrySet()) {
					ind++;
					bw.write("\"" + pair.getKey() + "," + pair.getValue() + "\"");
					bw.write((ind < ele.TurnWiseCount.size() ? "," : ""));
				}

				fenCount++;
				count++;

				if (fenCount % fenBatchSize == 0) {
					System.out.print("." + fenCount);
					bw.write("]}");
					bw.write("]");
					bw.flush();
					bw.close();
					fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN_" + (fenCount / fenBatchSize)
							+ ".txt";
					out_1 = new FileOutputStream(fenFileName);
					bw = new BufferedWriter(new OutputStreamWriter(out_1));
					System.out.println("writing FENs to " + fenFileName);
				} else {
					bw.write("]}\n,");
					bw.newLine();
				}
			}
			bw.write("]");
			bw.flush();
			// bw.write("Missed game count : "+missedGameCount);
			bw.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void WriteFENToFileWithGameIndexMap(ConcurrentHashMap<String, FENProp> map) {
		try {
			int fenCount = 0;
			int fenBatchSize = 100000;

			String fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN_"+fenStart+"_0.txt";
			FileOutputStream out_1 = new FileOutputStream(fenFileName);
			bw = new BufferedWriter(new OutputStreamWriter(out_1));

			System.out.println("writing FENs to " + fenFileName);

			Boolean fileStart = true;
			bw.write("[");
			for (Map.Entry<String, FENProp> pair : map.entrySet()) {
				if(!fileStart)
				{
					bw.write(",");
				}
				
				if(fileStart)
				{
					fileStart = false;
				}
				bw.write("{\"id\":\"" + pair.getKey() + "\",");
				bw.newLine();
				bw.write("\"Count\":" + pair.getValue().count + ",");
				bw.newLine();
				bw.write("\"Games\":[");
				int ind = 0;
				for (Map.Entry<String, String> gamePair : pair.getValue().gameResultMap.entrySet()) {
					ind++;
					bw.write("\"" + gamePair.getKey() + "," + gamePair.getValue() + "\"");
					bw.write((ind < pair.getValue().gameResultMap.size() ? "," : ""));
				}

				bw.write("],");
				bw.newLine();
				bw.write("\"TurnwiseCount\":[");
				ind = 0;
				for (Map.Entry<Integer, Integer> tunPair : pair.getValue().turnWiseCount.entrySet()) {
					ind++;
					bw.write("\"" + tunPair.getKey() + "," + tunPair.getValue() + "\"");
					bw.write((ind < pair.getValue().turnWiseCount.size() ? "," : ""));
				}
				
				bw.write("],");
				bw.newLine();
				bw.write("\"PrevFen\":[");
				ind = 0;
				for (String str : pair.getValue().prevFen) {
					ind++;
					bw.write("\"" + str + "\"");
					bw.write((ind < pair.getValue().prevFen.size() ? "," : ""));
				}

				fenCount++;

				if (fenCount % fenBatchSize == 0) {
					bw.write("]}");
					bw.write("]");
					bw.flush();
					bw.close();
					fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN_"+fenStart+"_" + (fenCount / fenBatchSize)
							+ ".txt";
					out_1 = new FileOutputStream(fenFileName);
					bw = new BufferedWriter(new OutputStreamWriter(out_1));
					fileStart = true;
					bw.write("[");
					System.out.println("writing FENs to " + fenFileName);
				} else {
					bw.write("]}\n");
					bw.newLine();
				}

			}

			bw.write("]");
			bw.flush();
			// bw.write("Missed game count : "+missedGameCount);
			bw.close();

			map.clear(); // saving memory by clearing the unused map
			PrintMemory();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void WriteGameToFile(ConcurrentHashMap<String, GameProp> map) {
		try {
			
			int gameCount = 0;
			int gameBatchSize = 100000;

			String gameFileName = fenWriteDir.getAbsolutePath() + File.separator + "Game_"+"0.txt";
			FileOutputStream out_1 = new FileOutputStream(gameFileName);
			bw = new BufferedWriter(new OutputStreamWriter(out_1));

			System.out.println("writing Games to " + gameFileName);

			Boolean fileStart = true;
			bw.write("[");
			for (Map.Entry<String, GameProp> pair : map.entrySet()) {
				if(!fileStart)
				{
					bw.write(",");
				}
				
				if(fileStart)
				{
					fileStart = false;
				}
				bw.write(pair.getValue().toString());

				gameCount++;

				if (gameCount % gameBatchSize == 0) {
					bw.write("]");
					bw.flush();
					bw.close();
					gameFileName = fenWriteDir.getAbsolutePath() + File.separator + "Game_"+"" + (gameCount / gameBatchSize)
							+ ".txt";
					out_1 = new FileOutputStream(gameFileName);
					bw = new BufferedWriter(new OutputStreamWriter(out_1));
					fileStart = true;
					bw.write("[");
					System.out.println("writing Games to " + gameFileName);
				} else {
					bw.newLine();
				}

			}

			bw.write("]");
			bw.flush();
			// bw.write("Missed game count : "+missedGameCount);
			bw.close();

			map.clear(); // saving memory by clearing the unused map
			PrintMemory();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void WriteFENCountToFile(BufferedWriter bw, Map<String, Integer> map) {
		try {
			int fenCount = 0;
			int fenBatchSize = 100000;

			String fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN.txt";
			FileOutputStream out_1 = new FileOutputStream(fenFileName);
			bw = new BufferedWriter(new OutputStreamWriter(out_1));

			System.out.println("writing FENs to " + fenFileName);

			bw.write("[");
			for (Map.Entry<String, Integer> pair : map.entrySet()) {
				bw.write(pair.getKey() + ","+pair.getValue());
				bw.newLine();
				fenCount++;

				if (fenCount % fenBatchSize == 0) {
					bw.flush();
					bw.close();
					fenFileName = fenWriteDir.getAbsolutePath() + File.separator + "FEN_" + (fenCount / fenBatchSize)
							+ ".txt";
					out_1 = new FileOutputStream(fenFileName);
					bw = new BufferedWriter(new OutputStreamWriter(out_1));
					System.out.println("writing FENs to " + fenFileName);
				} else {
					bw.write("\n,");
					bw.newLine();
				}

			}

			bw.flush();
			// bw.write("Missed game count : "+missedGameCount);
			bw.close();

			map.clear(); // saving memory by clearing the unused map
			PrintMemory();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void AddToFENMap(Move m) {
		if (m.FEN.length() > 0 && m.Gid.length() > 0) {
			if (!FENMap.containsKey(m.FEN)) {
				FENMap.put(m.FEN, new ArrayList<MoveByFEN>());
			}

			FENMap.get(m.FEN).add(new MoveByFEN(m.Gid, m.FEN));
		}
	}

	public static void PrintMemory() {
		System.gc();
		int mb = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		System.out.println("##### Heap utilization statistics [MB] #####");

		// Print used memory
		System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);

		System.out.println("Total fen count : " + totalFenCount);
		System.out.println("Discarded fen count : " + discardedFenCount);
		System.out.println("Retained fen count : " + (totalFenCount - discardedFenCount));
		System.out.println("Opening move fen count : " + first8MoveCount);
		System.out.println("Discarded Game count : " + discardedGameCount);
		System.out.println("Zero move num count : " + zeroMoveNumCount);

	}

	public static String GetValue(String inp) {
		String current = inp.replaceAll("[^0-9]", "");
		return current;
	}

	public static void SaveMoves(Game g) {
		if (!movesByRating.containsKey(g.GetWEloRange() + 1)) {
			movesByRating.put(g.GetWEloRange() + 1, g.GetWMoves());
		} else {
			ArrayList<Integer> list = movesByRating.get(g.GetWEloRange() + 1);
			list.addAll(g.GetWMoves());
			movesByRating.put(g.GetWEloRange() + 1, list);
		}

		if (!movesByRating.containsKey(g.GetBEloRange() + 2)) {
			movesByRating.put(g.GetBEloRange() + 2, g.GetBMoves());
		} else {
			ArrayList<Integer> list = movesByRating.get(g.GetBEloRange() + 2);
			list.addAll(g.GetBMoves());
			movesByRating.put(g.GetBEloRange() + 2, list);
		}
	}

	public static void TestPieceMovement(Game g) {
		// send false,true to get only white moves
		TreeMap<String, Double> moves = g.GetMovePreference(false, true);
		int rating = g.GetWEloRange();
		double[] arr = null;
		if (pieceMoveMap.containsKey(rating)) {
			arr = pieceMoveMap.get(rating);
		} else {
			arr = new double[6];
		}
		int i = 0;
		for (Entry<String, Double> entry : moves.entrySet()) {
			arr[i] = arr[i] + entry.getValue();
			i++;
		}
		i = 0;

		// remove check cases
		arr[1] = arr[1] - g.checksForLastAccesedMoves;

		pieceMoveMap.put(rating, arr);

		// both false gets only black moves
		moves = g.GetMovePreference(false, false);
		rating = g.GetBEloRange();

		if (pieceMoveMap.containsKey(rating)) {
			arr = pieceMoveMap.get(rating);
		} else {
			arr = new double[6];
		}

		for (Entry<String, Double> entry : moves.entrySet()) {

			arr[i] = arr[i] + entry.getValue();
			i++;
		}

		// remove check cases
		arr[1] = arr[1] - g.checksForLastAccesedMoves;
		pieceMoveMap.put(rating, arr);
	}

	public static void TestZipfsLawForMoves(Game g) {
		int[] bArray = null;
		int[] wArray = null;
		if (valueMap.containsKey(g.GetBEloRange())) {
			bArray = valueMap.get(g.GetBEloRange());
		} else {
			bArray = new int[50];
		}

		if (valueMap.containsKey(g.GetWEloRange())) {
			wArray = valueMap.get(g.GetWEloRange());
		} else {
			wArray = new int[50];
		}
		int[] arr = g.GetWEvaluation();
		for (int i = 0; i < arr.length; i++) {
			wArray[i] = wArray[i] + arr[i];
		}

		arr = g.GetWEvaluation();
		for (int i = 0; i < arr.length; i++) {
			bArray[i] = bArray[i] + arr[i];
		}

		valueMap.put(g.GetBEloRange(), bArray);
		valueMap.put(g.GetWEloRange(), wArray);

		if (g.bElo > g.wElo) {
			// black Elo is greater than white Elo
			if (lowRateValueMap.containsKey(g.GetWEloRange())) {
				wArray = lowRateValueMap.get(g.GetWEloRange());
			} else {
				wArray = new int[50];
			}
			arr = g.GetWEvaluation();
			for (int i = 0; i < arr.length; i++) {
				wArray[i] = wArray[i] + arr[i];
			}
			lowRateValueMap.put(g.GetWEloRange(), wArray);

			if (highRateValueMap.containsKey(g.GetBEloRange())) {
				bArray = highRateValueMap.get(g.GetBEloRange());
			} else {
				bArray = new int[50];
			}
			arr = g.GetBEvaluation();
			for (int i = 0; i < arr.length; i++) {
				bArray[i] = bArray[i] + arr[i];
			}
			highRateValueMap.put(g.GetBEloRange(), bArray);

		} else {
			if (highRateValueMap.containsKey(g.GetWEloRange())) {
				wArray = highRateValueMap.get(g.GetWEloRange());
			} else {
				wArray = new int[50];
			}
			arr = g.GetWEvaluation();
			for (int i = 0; i < arr.length; i++) {
				wArray[i] = wArray[i] + arr[i];
			}
			highRateValueMap.put(g.GetWEloRange(), wArray);

			if (lowRateValueMap.containsKey(g.GetBEloRange())) {
				bArray = lowRateValueMap.get(g.GetBEloRange());
			} else {
				bArray = new int[50];
			}
			arr = g.GetBEvaluation();
			for (int i = 0; i < arr.length; i++) {
				bArray[i] = bArray[i] + arr[i];
			}
			lowRateValueMap.put(g.GetBEloRange(), bArray);
		}
	}

	public static void TestBenfordLaw(Game g) throws IOException {
		for (Move m : g.wMoves) {
			for (MoveEvaluation eval : m.LegalMoves) {
				for (int val : eval.Evaluation) {
					benfordArray[Move.BenfordValue(val)] = benfordArray[Move.BenfordValue(val)] + 1;
				}
			}
		}
		for (Move m : g.bMoves) {
			for (MoveEvaluation eval : m.LegalMoves) {
				for (int val : eval.Evaluation) {
					benfordArray[Move.BenfordValue(val)] = benfordArray[Move.BenfordValue(val)] + 1;
				}
			}
		}
	}

	public static void PrintTestResults1(TreeMap<Integer, double[]> val) throws IOException {
		Iterator itr = val.keySet().iterator();
		while (itr.hasNext()) {
			int rating = (int) itr.next();
			double[] arr = pieceMoveMap.get(rating);
			Double total = 0.0;
			for (int i = 0; i < arr.length; i++) {
				total = total + arr[i];
			}
			bw.write("##### rating : " + rating + " #####\n");
			for (int i = 0; i < arr.length; i++) {
				bw.write(pieces[i] + " count : " + arr[i] + "  percentage : " + (((arr[i] * 1.0 / total) * 100))
						+ "% \n");
			}

		}
		bw.flush();
	}

	public static void PrintTestResults(TreeMap<Integer, int[]> val) throws IOException {
		Iterator itr = val.keySet().iterator();
		while (itr.hasNext()) {
			int rating = (int) itr.next();
			int[] arr = valueMap.get(rating);
			int total = 0;
			for (int i = 1; i < arr.length; i++) {
				total = total + arr[i];
			}
			bw.write("##### rating : " + rating + " #####\n");
			for (int i = 1; i < arr.length; i++) {
				bw.write("Move position : " + i + " count : " + arr[i] + "  percentage : "
						+ (((arr[i] * 1.0 / total) * 100)) + "% \n");
			}

		}
		bw.flush();
	}

	public static void PrintTestResults(int[] arr) throws IOException {
		int total = 0;
		for (int i = 1; i < arr.length; i++) {
			total = total + arr[i];
		}

		for (int i = 1; i < arr.length; i++) {
			bw.write("Digit : " + i + " count : " + arr[i] + "  percentage : " + (((arr[i] * 1.0 / total) * 100))
					+ "% \n");
		}

		bw.flush();
	}

	public static void SaveMoveInfo() {
		Set<Integer> keys = movesByRating.keySet();
		for (Integer i : keys) {
			try {
				bw.write(i + " " + Arrays.toString(movesByRating.get(i).toArray()));
				bw.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String FixEvaluation(String str) {
		// case where evaluation goes beyond 1000
		if (str.endsWith("x") || str.endsWith("X")) {
			return str.substring(0, str.length() - 1);
		}

		// Not sure where this case occurs
		if (str.startsWith("+M") || str.startsWith("-M")) {
			return str.charAt(0) + "" + str.substring(2, str.length());
		}

		// Not sure where this case occurs
		if (str.endsWith("C") || str.endsWith("c")) {
			return str.substring(0, str.length() - 1);
		}

		// Not sure where this case occurs
		if (str.equals("PRUN")) {
			return "0";
		}

		// Eval not available
		if (str.contains("n.a")) {
			return "0";
		}

		try {
			Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			return "0";
		} catch (Exception e) {
			return "0";
		}

		return str;
	}
}
