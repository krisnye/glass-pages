package glasspages;
import java.io.*;
import java.util.*;
import java.net.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.mozilla.javascript.*;

class PageInfo {
	public BaseFunction function;
	public String path;
	public PageInfo(BaseFunction function, String path) {
		this.function = function;
		this.path = path;
	}
}

@SuppressWarnings("serial")
public class PageServlet extends HttpServlet {


	Map<String,PageInfo> pageInfoCache;
	ScriptContextCache contextCache;
	boolean debug;
	String errorPath;
	Properties routes;
	
	@Override
	public void init()
	{
		try
		{
			this.debug = Boolean.parseBoolean(this.getInitParameter("debug"));

			if (!this.debug)
				this.pageInfoCache = new HashMap<String,PageInfo>();

			String routesParameter = this.getInitParameter("routes");
			if (routesParameter != null) {
				this.routes = new Properties();
				routes.load(new StringReader(routesParameter));
			}

			String sourceParameter = this.getInitParameter("source");
			if (sourceParameter == null)
				throw new RuntimeException("Missing init parameter 'source'");
			// automatically insert a reference to our PageServlet.js if not present
			String sourceDelimiter = ";";
			if (sourceParameter.indexOf("PageServlet.js") < 0)
				sourceParameter = "jar:file:WEB-INF/lib/glasspages.jar!/glasspages/PageServlet.js" + sourceDelimiter + sourceParameter;
			this.errorPath = this.getInitParameter("error");
			String[] sourceFiles = sourceParameter.split(sourceDelimiter);
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
			this.contextCache = new ScriptContextCache(sourceUrls);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	static String prefix = "WEB-INF/pages";
	static String suffix = ".js";
	public String getPagePath(String actualPath) {
		String path = actualPath.substring(prefix.length(), actualPath.length() - suffix.length());
		return path;
	}

	PageInfo getPageInfo(ScriptContext context, String path) throws IOException
	{
		PageInfo info = null;
		if (pageInfoCache != null)
			info = this.pageInfoCache.get(path);
		if (info == null) {
			String file = prefix + path + suffix;
			String actualPath = FileHelper.findRecursive(file);
			String source = FileHelper.read(actualPath);
			Object object = context.evaluate(source, path);
			info = new PageInfo((BaseFunction)object, actualPath);
			if (pageInfoCache != null)
				pageInfoCache.put(path, info);
		}
		return info;
	}

	void serve(HttpServletRequest request, HttpServletResponse response, ScriptContext context, String path, Exception error) throws IOException
	{
		PageInfo info = getPageInfo(context, path);
		context.put("request", request);
		context.put("response", response);
		context.put("path", path);
		context.put("servlet", this);
		context.put("__filename", info.path);
		context.put("__pagepath", getPagePath(info.path));
		if (error != null)
			context.put("error", error);
		context.call(info.function);
	}

	HttpServletRequest getRequestWrapper(HttpServletRequest request, final String path) {
		return new HttpServletRequestWrapper(request) {
			public String getRequestURI() { return path; }
		};
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/html");

		String path = request.getRequestURI();
		// use routes if present
		if (this.routes != null) {
			String route = this.routes.getProperty(path);
			if (route != null) {
				path = route;
				// then we also need to replace the HttpServletRequest.getRequestURI method
				// request = getRequestWrapper(request, path);
			}
		}

		if (path.endsWith("/"))
			path += "index";
		String source = null;
		// check for changes first
		if (this.debug && contextCache.checkForChanges()) {
			if (this.pageInfoCache != null)
				this.pageInfoCache.clear();
		}
		ScriptContext context = contextCache.getContext();
		try
		{
			serve(request, response, context, path, null);
		}
		catch (Exception e)
		{
			int status = getStatus(e);
			response.setStatus(status);
			response.setContentType("text/plain");
			System.out.println(e.toString());
			if (errorPath != null && status >= 500) {
				try {
					serve(request, response, context, errorPath, e);
				} catch (Exception f) {
					e.printStackTrace(response.getWriter());
					response.getWriter().write("ERROR IN ERROR HANDLER PAGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					f.printStackTrace(response.getWriter());
				}
			}
			else if (debug && status == 500) {
				e.printStackTrace(response.getWriter());
			}
			else if (e instanceof JavaScriptException) {
				JavaScriptException je = (JavaScriptException)e;
				response.getWriter().write(je.getValue().toString());
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

	int getStatus(Exception e)
	{
		if (e instanceof JavaScriptException) {
			JavaScriptException je = (JavaScriptException)e;
			Object value = ((ScriptableObject)je.getValue()).get("message", null);
			if (value != null) {
				String status = Utility.find(value.toString(), "^\\d\\d\\d\\b");
				if (status != null)
					return Integer.parseInt(status);
			}
		}
		return 500;
	}

}
