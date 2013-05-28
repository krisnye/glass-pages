package glasspages;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RouterServlet extends HttpServlet
{

	static String insecureProtocol = "http:";
	static String secureProtocol = "https:";

	String defaultPage;
	String extension = ".rsp";
	
	@Override public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		defaultPage = config.getInitParameter("defaultPage");
	}

	public void service(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{
		String url = request.getRequestURL().toString();
		String queryString = request.getQueryString();
		queryString = (queryString != null && queryString.length() > 0) ? "?" + queryString : "";
		String serverName = request.getServerName();
		int index = url.indexOf(serverName);
		int slash = url.indexOf('/', index + serverName.length());
		String path = slash < 0 ? "" : url.substring(slash);

		String uri = request.getRequestURI();

//	    Logger log = Logger.getLogger("router logger");

		//	the default page of /index.jsp will cause random urls to end with .jsp which will send them to the default page.
		if (uri == null || uri.length() == 0 || uri.equals("/") || uri.endsWith(extension))
		{
//			log.warning("REDIRECTION FROM: " + uri);
			response.sendRedirect(defaultPage);
			return;
		}

		//	localize here.
		String forward = uri + extension;

		this.getServletContext().getRequestDispatcher(forward).forward(request, response);
		//	after this, then we force the JSESSIONID cookie to be secure
//		Utility.setCookie(this, request, response, "JSESSIONID", request.getSession().getId(), -1);

	}

}

