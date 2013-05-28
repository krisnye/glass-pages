package glasspages;
import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.mozilla.javascript.*;

@SuppressWarnings("serial")
public class RspServlet extends HttpServlet {


	Map<String,BaseFunction> pageFunctionCache;
	ScriptContextCache contextCache;
	boolean debug;
	
	@Override
	public void init()
	{
		this.debug = Boolean.parseBoolean(this.getInitParameter("debug"));

		if (!this.debug)
			this.pageFunctionCache = new HashMap<String,BaseFunction>();

		String manifestParameter = this.getInitParameter("manifest");
		if (manifestParameter == null)
			throw new RuntimeException("Missing init parameter 'manifest'");
		String[] manifestFiles = manifestParameter.split(";");
		this.contextCache = new ScriptContextCache(manifestFiles);
		this.contextCache.setDebug(this.debug);
	}

	BaseFunction getPageFunction(ScriptContext context, String path) throws IOException
	{
		BaseFunction function = null;
		if (pageFunctionCache != null)
			function = this.pageFunctionCache.get(path);
		if (function == null) {
			String source = FileHelper.readRecursive(path);
			Object object = context.evaluate("(function(){" + source + "})", path);
			function = (BaseFunction)object;
			if (pageFunctionCache != null)
				pageFunctionCache.put(path, function);
		}
		return function;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/plain");

		String path = request.getRequestURI().substring(1);
		String source = null;
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
			e.printStackTrace(response.getWriter());
		}
		finally
		{
			contextCache.returnContext(context);
		}
	}

}
