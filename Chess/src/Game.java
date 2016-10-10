import java.util.*;
public class Game {
	List<Move> wMoves = new ArrayList<Move>();
	List<Move> bMoves = new ArrayList<Move>();
	public int wElo;
	public int bElo;
	public int checksForLastAccesedMoves = 0;
	public HashMap<String,String> pieceNotationMap = new HashMap<>();
	
	public Game()	
	{
		pieceNotationMap.put("N", "Knight");
		pieceNotationMap.put("B", "Bishop");
		pieceNotationMap.put("K", "King");
		pieceNotationMap.put("Q", "Queen");
		pieceNotationMap.put("R", "Rook");
	}
	
	public TreeMap<String,Double> GetMovePreference(boolean includeAll,boolean isWhite)
	{
		this.checksForLastAccesedMoves = 0;
		TreeMap<String,Double> map = new TreeMap<>();
		List<Move> moves;
		if(includeAll)
		{
			moves = this.wMoves;
			moves.addAll(this.bMoves);
		}
		else if(isWhite)
		{
			moves = this.wMoves;
		}
		else
		{
			moves = this.bMoves;
		}
		
		for(Move m : moves)
		{
			if(m.MovePlayed.endsWith("+"))
			{
				this.checksForLastAccesedMoves++;
			}
			double val = 0;
			for(int i = 0; i < m.NumLegalMoves;i++)
			{
				if(m.LegalMoves.get(i).Move.equals(m.MovePlayed))
				{
					//use absolute value because in case of black turn evaluation is inverted
					val = Math.abs(m.LegalMoves.get(0).Evaluation.get(m.LegalMoves.get(0).Evaluation.size() - 1) - m.LegalMoves.get(i).Evaluation.get(m.LegalMoves.get(i).Evaluation.size() - 1));					
					break;
				}
			}
			
			String piecePlayed = GetPieceInfoFromMove(m.MovePlayed);
			int len = piecePlayed.length();
			if(len <= 1)
			{
				if(len == 0)
				{
					double count = Math.log(1 + val);					
					if(map.containsKey("Pawn"))
					{
						count = count + map.get("Pawn");
					}
					map.put("Pawn", count);
				}
				else
				{
					double count = Math.log(1 + val);
					if(pieceNotationMap.containsKey(piecePlayed))
					{
						if(map.containsKey(pieceNotationMap.get(piecePlayed)))
						{
							count = count + map.get(pieceNotationMap.get(piecePlayed));
						}
						map.put(pieceNotationMap.get(piecePlayed), count);
					}					
				}
			}
		}		
		return map;
	}
	
	public static String GetPieceInfoFromMove(String move)
	{
		String movedPiece = "";
		int length = move.length();
		if(length <= 2)
		{
			return movedPiece;
		}
		
		movedPiece = move;
		if(movedPiece.endsWith("+"))
		{
			movedPiece = move.substring(0, length -1);
		}
		
		//castling case
		if(movedPiece.contains("-"))
		{
			return "K";
		}
		
		if(movedPiece.contains("x"))
		{
			movedPiece = movedPiece.replace("x", "");
		}
		
		if(movedPiece.length() <= 2)
		{
			return "";
		}
		
		//remove the position info too
		movedPiece = movedPiece.substring(0, movedPiece.length()-1);
		
		return movedPiece.substring(0, 1);
	}
	public int[] GetWEvaluation()
	{
		int[] wArray = new int[50];
		for(Move m : wMoves)
		{
			for(int i = 0; i < m.NumLegalMoves;i++)
			{
				if(m.LegalMoves.get(i).Move.equals(m.MovePlayed))
				{
					wArray[i+1] = wArray[i+1]+1;
				}
			}
		}
		
		return wArray;
	}
	
	public int[] GetBEvaluation()
	{
		int[] bArray = new int[50];
		for(Move m : bMoves)
		{
			for(int i = 0; i < m.NumLegalMoves;i++)
			{
				if(m.LegalMoves.get(i).Move.equals(m.MovePlayed))
				{
					bArray[i+1] = bArray[i+1]+1;
				}
			}			
		}
		return bArray;
	}
	
	public ArrayList<Integer> GetWMoves()
	{
		ArrayList<Integer> moves = new ArrayList<Integer>();
		for(int i = 0 ; i < this.wMoves.size();i ++)
		{
			moves.add(this.wMoves.get(i).Eval);
		}
		
		return moves;
	}
	
	public ArrayList<Integer> GetBMoves()
	{
		ArrayList<Integer> moves = new ArrayList<Integer>();
		for(int i = 0 ; i < this.bMoves.size();i ++)
		{
			moves.add(this.bMoves.get(i).Eval);
		}
		
		return moves;
	}
	
	public int[] GetMoves()
	{
		int[] moves = new int[this.bMoves.size()+this.wMoves.size()];
		int wSize = this.wMoves.size();
		int bSize = this.bMoves.size();
		
		int size = (wSize >= bSize ? wSize : bSize );
		int count = 0;
		for(int i = 0; i < size ; i++)
		{
			if(i < wSize)
			{
				moves[count] = this.wMoves.get(i).Eval;
				count++;
			}
			
			if(i < bSize)
			{
				moves[count] = this.bMoves.get(i).Eval;
				count++;
			}
		}
		
		return moves;
	}
	
	public int GetWEloRange()
	{
		return (this.wElo / 100) * 100;
	}
	
	public int GetBEloRange()
	{
		return (this.bElo / 100) * 100;
	}
}
