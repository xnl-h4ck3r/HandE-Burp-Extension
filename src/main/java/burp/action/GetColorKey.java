package burp.action;

import burp.Config;
import java.util.ArrayList;
import java.util.List;

/**
 * @author EvilChen
 */

public class GetColorKey {
    /**
     * Color subscript acquisition
     */
    public List<Integer> getColorKeys(List<String> keys){
        List<Integer> result = new ArrayList<>();
        String[] colorArray = Config.colorArray;
        int size = colorArray.length;
        // Get subscript by color
        for (String key : keys) {
            for (int v = 0; v < size; v++) {
                if (colorArray[v].equals(key)) {
                    result.add(v);
                }
            }
        }
        return result;
    }
}
