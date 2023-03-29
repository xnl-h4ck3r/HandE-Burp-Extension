package burp;

/**
 * @author EvilChen
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static String excludeSuffix = "3g2|3gp|7z|aac|abw|aif|aifc|aiff|arc|au|avi|azw|bin|bmp|bz|bz2|cmx|cod|csh|css|csv|deb|dmg|doc|docx|eot|eps|epub|flv|gif|gltf|gz|htc|ico|ics|ief|image|img|jar|jfif|jpe|jpeg|jpg|m3u|m4a|m4p|mid|midi|mjs|mov|mp2|mp3|mp4|mpa|mpe|mpeg|mpg|mpkg|mpp|mpv2|odp|ods|odt|oga|ogv|ogx|otf|pbm|pdf|pict|pgm|png|pnm|ppm|ppt|pptx|ra|ram|rar|ras|rgb|rmi|rtf|rpm|scss|snd|svg|svgz|swf|tar|tif|tiff|ttf|vsd|wav|weba|webm|webp|woff|woff2|xbm|xls|xlsx|xpm|xul|xwd|zip";

    public static String[] highlightMethod = new String[] {
        "top color only",
        "color upscaling"
    };

    public static String[] scopeArray = new String[] {
            "any",
            "any header",
            "any body",
            "response",
            "response header",
            "response body",
            "request",
            "request header",
            "request body"
    };

    public static String[] engineArray = new String[] {
            "nfa",
            "dfa"
    };

    public static String[] colorArray = new String[] {
            "red",
            "orange",
            "yellow",
            "green",
            "cyan",
            "blue",
            "pink",
            "magenta",
            "gray",
            "none"
    };

    public static Map<String,Object[][]> ruleConfig = null;

    public static Map<String, Map<String, List<String>>> globalDataMap = new HashMap<>();
}