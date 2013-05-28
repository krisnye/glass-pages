package glasspages;
import java.util.*;
import java.io.*;
import org.mozilla.javascript.*;

public class ScriptContextCache {

	long lastModified = 0;
	String[] manifestFiles;
	String[] files;
	String[] contents;
	Stack<ScriptContext> contexts = new Stack<ScriptContext>();
	boolean debug = false;

	public ScriptContextCache()
	{
		this(new String[0]);
	}

	public ScriptContextCache(String[] manifestFiles)
	{
		this.manifestFiles = manifestFiles;
	}

	void loadFiles() throws IOException {
		this.files = getSourceFiles(this.manifestFiles);
		// load all of these file contents.
		this.contents = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			contents[i] = FileHelper.read(file);
			System.out.println("Loaded: " + files[i]);
		}
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	ScriptContext pop()
	{
		try {
			// small possibility of another thread removal between these lines.
			if (contexts.size() > 0)
				return contexts.pop();
		}
		catch (EmptyStackException e) {
			System.out.println(e);
		}
		return null;
	}

	public boolean checkLastModified() throws IOException
	{
		// check the manifestFiles lastModified dates.
		long lastModified = 0;
		for (String file : this.manifestFiles) {
			long thisModified = new File(file).lastModified();
			if (thisModified > lastModified)
				lastModified = thisModified;
		}

		if (lastModified > this.lastModified) {
			this.lastModified = lastModified;
			// flush all our contexts.
			this.contexts.clear();
			return true;
		}
		return false;
	}
	
	public ScriptContext getContext() throws IOException
	{
		if ((debug && checkLastModified()) || this.files == null)
			this.loadFiles();

		ScriptContext sc = pop();
		if (sc == null) {
			Context context = Context.enter();
			Scriptable global = context.initStandardObjects();
			sc = new ScriptContext(context, global);

			System.out.println("New ScriptContext");
			// now load up all of our source files
			for (int i = 0; i < files.length; i++) {
				String file = files[i];
				String content = contents[i];
				sc.evaluate(content, file);
			}
		} else {
			sc.getContext().enter();
		}
		return sc;
	}

	public void returnContext(ScriptContext context)
	{
		// unbind from this context
		context.getContext().exit();
		contexts.push(context);
	}

	static String[] getSourceFiles(String[] manifestFiles) throws IOException
	{
		ArrayList<String> sourceFiles = new ArrayList<String>();
		if (manifestFiles.length > 0) {
			// json parse the content into a file array.
			Context context = Context.enter();
			try
			{
				Scriptable global = context.initStandardObjects();
				for (String manifestFile : manifestFiles) {
					String manifestDirectory = new File(manifestFile).getParentFile().getPath();
					// System.out.println("Manifest: " + manifestFile);
					String content = FileHelper.read(manifestFile);
					// System.out.println("Manifest Content: " + content);
					Object jsArray = context.evaluateString(global, content, manifestFile, 0, null);
					// System.out.println("Manifest JSArray: " + jsArray);
					String[] array = (String[])context.jsToJava(jsArray, String[].class);
					// join path to its manifest root
					for (int i = 0; i < array.length; i++) {
						String path = new File(manifestDirectory, array[i]).getPath();
						sourceFiles.add(path);
					}
				}
			}
			finally
			{
				context.exit();
			}
		}
		// System.out.println("Manifest String[]: " + array);
		return sourceFiles.toArray(new String[sourceFiles.size()]);
	}

}
