package glasspages;

import java.io.*;

public class FileHelper {

	//	TODO: Could cache the correct path for performance?
	public static String readRecursive(String path) throws IOException
	{
		String checkPath = path;
		String ext = path.substring(path.lastIndexOf('.'));
		while (true)
		{
//			Utility.log("checkPath: " + checkPath);
			if (new File(checkPath).exists())
				return read(checkPath);

			int index = checkPath.lastIndexOf('/');
			if (index <= 0)
				throw new RuntimeException("File not found: " + path);
			//	now search back up the path for a more ancestral file with the same extension.
			checkPath = checkPath.substring(0, index) + ext;
		}
	}
	
	//	should provide a cached version of the read function.
	public static String read(String path) throws IOException
	{
		InputStream input = new FileInputStream(path);
		return read(input);
	}
	
	public static String read(InputStream input) throws IOException
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer buffer = new StringBuffer(Math.max(input.available(), 1024));
			String lineSeparator = System.getProperty("line.separator");
			String text = null;
			while ((text = reader.readLine()) != null)
			{
				if (buffer.length() > 0)
					buffer.append(lineSeparator);
				buffer.append(text);
			}
			return buffer.toString();
		}
		finally
		{
			input.close();
		}
	}

}
