package glasspages;

import java.util.regex.*;

public class Utility
{

    public static String find(String source, String pattern)
    {
        return find(source, pattern, 0);
    }
    
    public static String find(String source, String pattern, int group)
    {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        return m.find() ? m.group(0) : null;
    }

}