package glasspages;
import org.mozilla.javascript.*;

class ScriptContext {
	
	private Context context;
	private Scriptable global;
	
	//	creates a new ScriptContext and automatically creates the underlying context and initializes a global object.
	public ScriptContext(Context context, Scriptable global)
	{
		this.context = context;
		this.global = global;
//		System.out.println("Creating new ScriptContext " + this);
	}

	public Context getContext() { return this.context; }
	public Scriptable getGlobal() { return this.global; }

	//	sets a global property
	public void put(String property, Object value)
	{
		global.put(property, global, value);
	}
	
	//	executes a script
	public Object evaluate(String source, String sourceName)
	{
		Object result = context.evaluateString(global, source, sourceName, 0, null);
		return result;
	}

	static Object[] emptyArgs = new Object[0];

	public Object call(BaseFunction function)
	{
		return this.call(function, emptyArgs);
	}

	public Object call(BaseFunction function, Object[] args)
	{
		return function.call(context, global, global, args);
	}

}
