package burp.action;

import burp.BurpExtender;
import burp.IExtensionHelpers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessMessage {
    MatchHTTP mh = new MatchHTTP();
    ExtractContent ec = new ExtractContent();
    DoAction da = new DoAction();
    GetColorKey gck = new GetColorKey();
    UpgradeColor uc = new UpgradeColor();

    public List<Map<String, String>> processMessageByContent(IExtensionHelpers helpers, byte[] content, boolean isRequest, boolean messageInfo, String host) {
        List<Map<String, String>> result = new ArrayList<>();;
        Map<String, Map<String, Object>> obj;

        if (isRequest) {

            // Get header
            List<String> requestTmpHeaders = helpers.analyzeRequest(content).getHeaders();
            String requestHeaders = String.join("\n", requestTmpHeaders);

            try {
                String urlString = requestTmpHeaders.get(0).split(" ")[1];
                urlString = urlString.indexOf("?") > 0 ? urlString.substring(0, urlString.indexOf("?")) : urlString;
                if (mh.matchSuffix(urlString)) {
                    return result;
                }
            } catch (Exception e) {
                return result;
            }

            // Get message body 
            int requestBodyOffset = helpers.analyzeRequest(content).getBodyOffset();
            byte[] requestBody = Arrays.copyOfRange(content, requestBodyOffset, content.length);

            obj = ec.matchRegex(content, requestHeaders, requestBody, "request", host);
        } else {
            try {
                String inferredMimeType = String.format("hae.%s", helpers.analyzeResponse(content).getInferredMimeType().toLowerCase());
                String statedMimeType = String.format("hae.%s", helpers.analyzeResponse(content).getStatedMimeType().toLowerCase());

                if (mh.matchSuffix(statedMimeType) || mh.matchSuffix(inferredMimeType)) {
                    return result;
                }
            } catch (Exception e) {
                return result;
            }
            // Get header 
            List<String> responseTmpHeaders = helpers.analyzeResponse(content).getHeaders();
            String responseHeaders = String.join("\n", responseTmpHeaders);

            // Get message body
            int responseBodyOffset = helpers.analyzeResponse(content).getBodyOffset();
            byte[] responseBody = Arrays.copyOfRange(content, responseBodyOffset, content.length);

            obj = ec.matchRegex(content, responseHeaders, responseBody, "response", host);
        }

        if (obj.size() > 0) {
            if (messageInfo) {
                List<List<String>> resultList = da.highlightAndComment(obj);
                List<String> colorList = resultList.get(0);
                List<String> commentList = resultList.get(1);
                if (colorList.size() != 0 && commentList.size() != 0) {
                    String color = uc.getEndColor(gck.getColorKeys(colorList));
                    Map<String, String> colorMap = new HashMap<String, String>(){{
                        put("color", color);
                    }};
                    Map<String, String> commentMap = new HashMap<String, String>(){{
                        put("comment", String.join(" | ", commentList));
                    }};
                    result.add(colorMap);
                    result.add(commentMap);
                }
            } else {
                result.add(da.extractString(obj));
                //BurpExtender.stdout.println("result: " + result);
            }
        }

        return result;

    }
}