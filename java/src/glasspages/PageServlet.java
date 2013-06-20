package glasspages;
import java.io.*;
import java.util.*;
import java.net.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.mozilla.javascript.*;

@SuppressWarnings("serial")
public class PageServlet extends HttpServlet {


	Map<String,BaseFunction> pageFunctionCache;
	ScriptContextCache contextCache;
	boolean debug;
	
	@Override
	public void init()
	{
		this.debug = Boolean.parseBoolean(this.getInitParameter("debug"));

		if (!this.debug)
			this.pageFunctionCache = new HashMap<String,BaseFunction>();

		String sourceParameter = this.getInitParameter("source");
		if (sourceParameter == null)
			throw new RuntimeException("Missing init parameter 'source'");
		String[] sourceFiles = sourceParameter.split(";");
		URL[] sourceUrls = new URL[sourceFiles.length];
		for (int i = 0; i < sourceFiles.length; i++) {
			String sourceFile = sourceFiles[i];
			if (sourceFile.indexOf(':') < 0)
				sourceFile = "file:" + sourceFile;
			try
			{
				sourceUrls[i] = new URL(sourceFile);
			}
			catch (MalformedURLException e)
			{
				throw new RuntimeException(e);
			}
		}

		try
		{
			this.contextCache = new ScriptContextCache(sourceUrls);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	BaseFunction getPageFunction(ScriptContext context, String path) throws IOException
	{
		BaseFunction function = null;
		if (pageFunctionCache != null)
			function = this.pageFunctionCache.get(path);
		if (function == null) {
			String file = "WEB-INF/pages" + path + ".js";
			String source = FileHelper.readRecursive(file);
			Object object = context.evaluate(source, path);
			function = (BaseFunction)object;
			if (pageFunctionCache != null)
				pageFunctionCache.put(path, function);
		}
		return function;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/html");

		String path = request.getRequestURI();
		if (path.endsWith("/"))
			path += "index";
		String source = null;
		// check fo changed first
		if (this.debug && contextCache.checkForChanges()) {
			if (this.pageFunctionCache != null)
				this.pageFunctionCache.clear();
		}
		ScriptContext context = contextCache.getContext();
		try
		{
			BaseFunction function = getPageFunction(context, path);
			context.put("request", request);
			context.put("response", response);
			context.put("servlet", this);
			context.call(function);
		}
		catch(Exception e)
		{
			// if (response.getStatus() < 400)
			// 	response.setStatus(500);
			response.setContentType("text/plain");
			if (debug) {
				e.printStackTrace(response.getWriter());
			}
			else {
				response.getWriter().write(e.getMessage());
			}

		}
		finally
		{
			contextCache.returnContext(context);
		}
	}

}
