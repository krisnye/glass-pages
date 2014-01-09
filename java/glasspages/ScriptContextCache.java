package glasspages;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import org.mozilla.javascript.*;

class ScriptContextCache {

    //  searches for require statements
    //  and returns an array of dependencies.
    static String[] findDependencies(String source) {
        Matcher m = requirePattern.matcher(source);
        ArrayList<String> deps = new ArrayList<String>();
        while (m.find()) {
            int start = m.start(1);
            int end = m.end(1);
            String found = source.substring(start, end);
            deps.add(found);
        }
        return deps.toArray(new String[deps.size()]);
    }
    static Pattern requirePattern = Pattern.compile("\\brequire\\s*\\(\\s*['\"][\\./]+([^'\"]+)['\"]\\s*\\)");

    static class SourceScript
    {
        private URL url;
        private String content;
        private String[] dependencies;

        public SourceScript(URL url)
        {
            this.url = url;
        }
        public void loadContent() throws IOException
        {
            this.content = FileHelper.read(url);
            // search for dependents now.
            this.dependencies = findDependencies(this.content);
            // System.out.println("----------------------------------");
            // System.out.println(this.url);
            // for (int i = 0; i < dependencies.length; i++) {
            //  System.out.println("    " + this.dependencies[i]);
            // }
            // System.out.println("----------------------------------");
        }
        void ensureLoaded() throws IOException
        {
            if (this.content == null)
                this.loadContent();
        }
        public String[] getDependencies() throws IOException {
            this.ensureLoaded();
            return this.dependencies;
        }
        public String getContent() throws IOException {
            this.ensureLoaded();
            return this.content;
        }
        public URL getURL() {
            return this.url;
        }
        public long getLastModified() {
            String path = this.url.getPath();
            // we can't get changes on jar includes
            if (path.indexOf(".jar!") >= 0)
                return 0;
            File file = new File(path);
            return file.lastModified();
        }
        public void evaluate(ScriptContext sc) throws IOException {
            sc.evaluate(this.getContent(), this.toString());
        }
        public String toString() {
            return this.url.toString();
        }
    }

    long lastModified = 0;
    // the original source urls
    URL[] sourceUrls;
    // file urls expanded from manifest urls
    List<SourceScript> sourceScripts;
    // the reusable set of contexts.
    Stack<ScriptContext> contexts = new Stack<ScriptContext>();

    public ScriptContextCache() throws IOException
    {
        this(new URL[0]);
    }

    public ScriptContextCache(URL[] sourceUrls) throws IOException
    {
        this.sourceUrls = sourceUrls;
        loadSourceScripts();
    }

    void loadSourceScripts() throws IOException
    {
        this.sourceScripts = new ArrayList<SourceScript>(Arrays.asList(getSourceScripts(this.sourceUrls)));
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

    long checkLastModified(URL url, long lastModified) throws IOException
    {
        if ("file".equals(url.getProtocol())) {
            long thisModified = new File(url.getPath()).lastModified();
            if (thisModified > lastModified)
                lastModified = thisModified;
        }
        return lastModified;
    }

    public boolean checkForChanges() throws IOException
    {
        long lastModified = 0;

        for (URL url : this.sourceUrls) {
            lastModified = checkLastModified(url, lastModified);
        }

        //  check last modified on any of our source urls
        if (this.sourceScripts != null) {
            for (SourceScript script : this.sourceScripts) {
                URL url = script.getURL();
                lastModified = checkLastModified(url, lastModified);
            }
        }

        if (lastModified > this.lastModified) {
            this.updateContexts(this.lastModified);
            this.lastModified = lastModified;
            return true;
        }
        return false;
    }

    boolean addScriptAndDependents(SourceScript source, List<SourceScript> changed) throws IOException {
        if (changed.contains(source))
            return false;
        changed.add(source);
        String url = source.getURL().toString();
        // remove trailing ".js"
        if (url.endsWith(".js"))
            url = url.substring(0, url.length() - ".js".length());
        // O n^2 algorithm
        for (SourceScript script : sourceScripts) {
            String[] deps = script.getDependencies();
            for (String dep : deps) {
                if (url.endsWith(dep)) {
                    addScriptAndDependents(script, changed);
                    break;
                }
            }
        }
        return true;
    }

    void updateContexts(long lastModified) throws IOException {
        // trivial reload scripts, discard contexts.
        // this.loadSourceScripts();
        // this.contexts.clear();

        // find which files specifically have changed
        ArrayList<SourceScript> changed = new ArrayList<SourceScript>();
        for (SourceScript script : sourceScripts) {
            long thisModified = script.getLastModified();
            if (thisModified > lastModified) {
                addScriptAndDependents(script, changed);
            }
        }

        // print out all the changed and reload them
        for (SourceScript script : changed) {
            System.out.println(script);
            script.loadContent();
        }

        for (int i = 0; i < this.contexts.size(); i++) {
            ScriptContext sc = this.contexts.get(i);
            sc.getContext().enter();
            try
            {
                for (SourceScript script : changed) {
                    script.evaluate(sc);
                }
            }
            finally
            {
                sc.getContext().exit();
            }
        }
    }
    
    public ScriptContext getContext() throws IOException
    {
        ScriptContext sc = pop();
        if (sc == null) {
            Context context = Context.enter();
            Scriptable global = context.initStandardObjects();
            sc = new ScriptContext(context, global);
            System.out.println("New ScriptContext");
            // now load up all of our source files
            for (SourceScript script : sourceScripts) {
                script.evaluate(sc);
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

    static SourceScript[] getSourceScripts(URL[] sourceUrls) throws IOException
    {
        ArrayList<SourceScript> sourceFiles = new ArrayList<SourceScript>();
        Context context = null;
        Scriptable global = null;
        try
        {
            //  add our source urls
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
                    Scriptable jsObject = (Scriptable)context.evaluateString(global, "(" + content + ")", manifestFile, 0, null);
                    // System.out.println("Manifest JSObject: " + jsObject);
                    Scriptable jsArray = (Scriptable)jsObject.get("files", null);
                    // System.out.println("Manifest JSArray: " + jsArray);
                    String[] array = (String[])context.jsToJava(jsArray, String[].class);
                    // join path to its manifest root
                    for (int i = 0; i < array.length; i++) {
                        File file = new File(manifestDirectory, array[i]);
                        sourceFiles.add(new SourceScript(file.toURI().toURL()));
                    }
                }
                else {
                    sourceFiles.add(new SourceScript(sourceUrl));
                }
            }
        }
        finally
        {
            if (context != null)
                context.exit();
        }

        // System.out.println("Manifest String[]: " + array);
        return sourceFiles.toArray(new SourceScript[sourceFiles.size()]);
    }

}
