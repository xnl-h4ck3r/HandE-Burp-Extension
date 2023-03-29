package burp.action;

import burp.BurpExtender;
import java.nio.charset.StandardCharsets;
import java.util.*;
import burp.Config;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import jregex.Matcher;
import jregex.Pattern;

/**
 * @author EvilChen 
 */

public class ExtractContent {

    public Map<String, Map<String, Object>> matchRegex(byte[] content, String headers, byte[] body, String scopeString, String host) {
        Map<String, Map<String, Object>> map = new HashMap<>(); // The final result returned
        Config.ruleConfig.keySet().forEach(i -> {
            for (Object[] objects : Config.ruleConfig.get(i)) {
                // Multi-threaded execution reduces blocking to a certain extent
                Thread t = new Thread(() -> {
                    String matchContent = "";
                    // Traversal to get the rules
                    List<String> result = new ArrayList<>();
                    Map<String, Object> tmpMap = new HashMap<>();

                    String name = objects[1].toString();
                    boolean loaded = (Boolean) objects[0];
                    String regex = objects[2].toString();
                    String color = objects[3].toString();
                    String scope = objects[4].toString();
                    String engine = objects[5].toString();
                    boolean sensitive = (Boolean) objects[6];
                    // Determine whether the rule is enabled and its scope
                    if (loaded && (scope.contains(scopeString) || scope.contains("any"))) {
                        switch (scope) {
                            case "any":
                            case "request":
                            case "response":
                                matchContent = new String(content, StandardCharsets.UTF_8).intern();
                                break;
                            case "any header":
                            case "request header":
                            case "response header":
                                matchContent = headers;
                                break;
                            case "any body":
                            case "request body":
                            case "response body":
                                matchContent = new String(body, StandardCharsets.UTF_8).intern();
                                break;
                            default:
                                break;
                        }

                        if ("nfa".equals(engine)) {
                            Pattern pattern;
                            // Determine whether the rule is case-sensitive
                            if (sensitive) {
                                pattern = new Pattern(regex);
                            } else {
                                pattern = new Pattern(regex, Pattern.IGNORE_CASE);
                            }

                            Matcher matcher = pattern.matcher(matchContent);
                            while (matcher.find()) {
                                // Add matching data to list
                                // Force users to use () wrapped regular
                                result.add(matcher.group(1));
                            }
                        } else {
                            RegExp regexp = new RegExp(regex);
                            Automaton auto = regexp.toAutomaton();
                            RunAutomaton runAuto = new RunAutomaton(auto, true);
                            AutomatonMatcher autoMatcher = runAuto.newMatcher(matchContent);
                            while (autoMatcher.find()) {
                                // Add matching data to list
                                // Force users to use () wrapped regular
                                result.add(autoMatcher.group());
                            }
                        }

                        // De-dupe
                        HashSet tmpList = new HashSet(result);
                        result.clear();
                        result.addAll(tmpList);

                        if (!result.isEmpty()) {
                            tmpMap.put("color", color);
                            String dataStr = String.join("\n", result);
                            tmpMap.put("data", dataStr);

                            // Added to global variables for easy Databoard retrieval
                            if (!host.isEmpty()) {
                                String anyHost = host.replace(host.split("\\.")[0], "*");
                                List<String> dataList = Arrays.asList(dataStr.split("\n"));
                                if (Config.globalDataMap.containsKey(host)) {
                                    Map<String, List<String>> gRuleMap = Config.globalDataMap.get(host);
                                    // Determine whether the matching rule exists (the logic is the same as that of Host)
                                    if (gRuleMap.containsKey(name)) {
                                        List<String> gDataList = gRuleMap.get(name);
                                        List<String> mergeDataList = new ArrayList<>(gDataList);
                                        // Merge two lists
                                        mergeDataList.addAll(dataList);
                                        // De-dupe
                                        tmpList = new HashSet(mergeDataList);
                                        mergeDataList.clear();
                                        mergeDataList.addAll(tmpList);
                                        // Replace
                                        gRuleMap.replace(name, gDataList, mergeDataList);
                                    } else {
                                        gRuleMap.put(name, dataList);
                                    }
                                } else if (!Config.globalDataMap.containsKey(anyHost)) {
                                    // Add wildcard Host
                                    Config.globalDataMap.put(anyHost, new HashMap<>());
                                } else {
                                    Map<String, List<String>> ruleMap = new HashMap<>();
                                    ruleMap.put(name, dataList);
                                    // Add a single host
                                    Config.globalDataMap.put(host, ruleMap);
                                }
                            }

                            map.put(name, tmpMap);

                        }
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });
        return map;
    }
}
