package burp.action;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import burp.yaml.LoadConfig;

/**
 * @author EvilChen
 */

public class MatchHTTP {
    // Match suffix
    LoadConfig lc = new LoadConfig();
    public boolean matchSuffix(String str) {
        Pattern pattern = new Pattern(String.format("[\\w]+[\\.](%s)", lc.getExcludeSuffix()), REFlags.IGNORE_CASE);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
