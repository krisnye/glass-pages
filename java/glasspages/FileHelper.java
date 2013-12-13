package glasspages;

import java.io.*;
import java.net.*;

public class FileHelper {

	//	TODO: Could cache the correct path for performance?
	public static String findRecursive(String path) throws IOException
	{
		String checkPath = path;
		int index = path.lastIndexOf('.');
		if (index >= 0)
		{
			String ext = path.substring(index);
			while (true)
			{
				if (new File(checkPath).exists())
					return checkPath;

				index = checkPath.lastIndexOf('/');
				if (index <= 0)
					break;
				//	now search back up the path for a more ancestral file with the same extension.
				checkPath = checkPath.substring(0, index) + ext;
			}
		}
		throw new RuntimeException("File not found: " + path);
	}

	public static String read(String path) throws IOException
	{
		InputStream input = new FileInputStream(path);
		return read(input);
	}

	public static String read(URL url) throws IOException
	{
		InputStream input = url.openStream();
		return read(input);
	}

	public static String read(BufferedReader reader) throws IOException
	{
		StringWriter writer = new StringWriter(256);
		int read;
		while ((read = reader.read()) >= 0)
			writer.write((char)read);
		return writer.toString();
	}

	public static String read(BufferedReader reader, int length) throws IOException
	{
		char[] chars = new char[length];
		reader.read(chars, 0, length);
		return new String(chars);
	}
	
	public static String read(InputStream input) throws IOException
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			return read(reader);
		}
		finally
		{
			input.close();
		}
	}

}
