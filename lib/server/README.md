Glasspages allow you to use server side coffeebars templates to render pages.

TODO:
   X move the page building into glass build.
     remove page building from glass-pages.
     update the blank app engine project with glasspages.
     describe glass build setup for building pages.

To setup for server pages:

Include the /glasspages.jar in your app.
Register the glasspages.PageServlet in your web.xml
The source parameter takes ; delimited urls.  The urls can either be .js source files or .json array manifest files that list other relative sources.

    <servlet>
        <servlet-name>pageServlet</servlet-name>
        <servlet-class>glasspages.PageServlet</servlet-class>
        <init-param>
            <param-name>source</param-name>
            <param-value>classpath:glasspages/PageServlet.js;js/modules/manifest.json;WEB-INF/js/modules/manifest.json</param-value>
        </init-param>
        <init-param>
            <param-name>debug</param-name>
            <param-value>true</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>pageServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

Create and test a hello.page in the root:

    Hello {{ "Page" }}
