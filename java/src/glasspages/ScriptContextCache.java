package glasspages;
import java.util.*;
import java.io.*;
import java.net.*;
import org.mozilla.javascript.*;

public class ScriptContextCache {

	long lastModified = 0;
	URL[] sourceUrls;
	URL[] files;
	String[] contents;
	Stack<ScriptContext> contexts = new Stack<ScriptContext>();
	boolean debug = false;

	public ScriptContextCache()
	{
		this(new URL[0]);
	}

	public ScriptContextCache(URL[] sourceUrls)
	{
		this.sourceUrls = sourceUrls;
	}

	void loadFiles() throws IOException {
		this.files = getSourceFiles(this.sourceUrls);
		// load all of these file contents.
		this.contents = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			URL file = files[i];
			contents[i] = FileHelper.read(file);
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
		long lastModified = 0;
		//	check last modified on any of our source urls
		for (URL url : this.sourceUrls) {
			if ("file".equals(url.getProtocol())) {
				long thisModified = new File(url.getPath()).lastModified();
				if (thisModified > lastModified)
					lastModified = thisModified;
			}
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
				URL file = files[i];
				String content = contents[i];
				sc.evaluate(content, file.toString());
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

	static URL[] getSourceFiles(URL[] sourceUrls) throws IOException
	{
		ArrayList<URL> sourceFiles = new ArrayList<URL>();
		Context context = null;
		Scriptable global = null;
		try
		{
			//	add our source urls
			for (URL sourceUrl : sourceUrls) {
				boolean isManifest = sourceUrl.toString().endsWith(".json");
				if (isManifest) {
					if (context == null) {
						context = Context.enter();
						global = context.initStandardObjects();
					}
					String manifestFile = sourceUrl.getPath();
					String manifestDirectory = new File(manifestFile).getParentFile().getPath();
					// System.out.println("Manifest: " + manifestFile);
					String content = FileHelper.read(manifestFile);
					// System.out.println("Manifest Content: " + content);
					Object jsArray = context.evaluateString(global, content, manifestFile, 0, null);
					// System.out.println("Manifest JSArray: " + jsArray);
					String[] array = (String[])context.jsToJava(jsArray, String[].class);
					// join path to its manifest root
					for (int i = 0; i < array.length; i++) {
						File file = new File(manifestDirectory, array[i]);
						sourceFiles.add(file.toURI().toURL());
					}
				}
				else {
					sourceFiles.add(sourceUrl);
				}
			}
		}
		finally
		{
			if (context != null)
				context.exit();
		}

		// System.out.println("Manifest String[]: " + array);
		return sourceFiles.toArray(new URL[sourceFiles.size()]);
	}

}
