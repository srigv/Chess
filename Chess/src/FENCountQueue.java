import java.util.Comparator;
import java.util.PriorityQueue;

public class FENCountQueue {
	
	public PriorityQueue<FEN> queue = new PriorityQueue<FEN>(new Comparator<FEN>(){

		@Override
		public int compare(FEN one, FEN two) {
			// TODO Auto-generated method stub
			return two.count < one.count ? -1 : 1;
		}
		
	});
	
	

}
