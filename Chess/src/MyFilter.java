import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class MyFilter implements FileFilter {

	String[] pattern;
	Pattern regex = null;
	boolean regexMatch = false;
	
	public MyFilter(String p,String regex)
	{		
		this.pattern = new String[]{p};
		if(regex.length() > 0)
		{
			this.regex = Pattern.compile(regex);
			regexMatch = true;
		}
		
	}
	@Override
	public boolean accept(File pathname) {
		
		if(regexMatch)
		{
			return regex.matcher(pathname.toString()).find();
		}
		else
		{
			for(String str : this.pattern)
			{
				if(!pathname.getName().endsWith(str))
				{
					return false;
				}
			}
		}
		return true;
	}
	
}
