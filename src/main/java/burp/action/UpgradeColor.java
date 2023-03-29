package burp.action;
import burp.Config;
import java.util.*;
import burp.yaml.LoadConfig;

/**
 * @author EvilChen
 */

public class UpgradeColor {
    private String endColor = "";
    /**
     * Color Upscaling Recursive Algorithm
     */
    private void colorUpgrade(List<Integer> colorList) {
        int colorSize = colorList.size();
        String[] colorArray = Config.colorArray;
        colorList.sort(Comparator.comparingInt(Integer::intValue));
        LoadConfig lc = new LoadConfig();

        if (lc.getHighlightMethod().equals(Config.highlightMethod[0])) {
            this.endColor = colorArray[colorList.get(0)];
        } else {
            int i = 0;
            List<Integer> stack = new ArrayList<>();
            while (i < colorSize) {
                if (stack.isEmpty()) {
                    stack.add(colorList.get(i));
                } else {
                    if (!Objects.equals(colorList.get(i), stack.stream().reduce((first, second) -> second).orElse(99999999))) {
                        stack.add(colorList.get(i));
                    } else {
                        stack.set(stack.size() - 1, stack.get(stack.size() - 1) - 1);
                    }
                }
                i++;
            }

            // Use HashSet to delete duplicate elements
            HashSet tmpList = new HashSet(stack);
            if (stack.size() == tmpList.size()) {
                stack.sort(Comparator.comparingInt(Integer::intValue));
                // If the first element of the stack array list is less than 0, then set it to Red (0)
                if(stack.get(0) < 0) {
                    this.endColor = colorArray[0];
                } else {
                    // Else set the color of the first element in the stack array
                    this.endColor = colorArray[stack.get(0)];
                }
            } else {
                this.colorUpgrade(stack);
            }
        }
    }

    public String getEndColor(List<Integer> colorList) {
        colorUpgrade(colorList);
        return endColor;
    }
}
