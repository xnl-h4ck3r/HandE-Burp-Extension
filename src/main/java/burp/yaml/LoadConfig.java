package burp.yaml;

import burp.Config;
import burp.yaml.template.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.nodes.Tag;

public class LoadConfig {
    private static final Yaml yaml = new Yaml();
    private static String HaEConfigPath = String.format("%s/.config/H&E", System.getProperty("user.home"));
    private static String SettingPath = String.format("%s/%s", HaEConfigPath, "Setting.yml");
    private static String ConfigPath =  String.format("%s/%s", HaEConfigPath, "Config.yml");

    public LoadConfig() {
        // Constructor, initialize configuration
        File HaEConfigPathFile = new File(HaEConfigPath);
        if (!(HaEConfigPathFile.exists() && HaEConfigPathFile.isDirectory())) {
            HaEConfigPathFile.mkdirs();
        }

        File settingPathFile = new File(SettingPath);
        if (!(settingPathFile.exists() && settingPathFile.isFile())) {
            initSetting();
            initRules();
        }
        Config.ruleConfig = LoadConfig.getRules();
    }


    // Initialize setting information
    public void initSetting() {
        Map<String, Object> r = new HashMap<>();
        r.put("configPath", ConfigPath);
        r.put("excludeSuffix", getExcludeSuffix());
        r.put("highlightMethod", getHighlightMethod());
        try {
            Writer ws = new OutputStreamWriter(new FileOutputStream(SettingPath), StandardCharsets.UTF_8);
            yaml.dump(r, ws);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Initialize rule configuration
    public void initRules() {
        Rule rule = new Rule();
        rule.setLoaded(true);
        rule.setName("Email");
        rule.setColor("yellow");
        rule.setEngine("nfa");
        rule.setScope("response");
        rule.setRegex("(([a-zA-Z0-9][_|\\.])*[a-zA-Z0-9]+@([a-zA-Z0-9][-|_|\\.])*[a-zA-Z0-9]+\\.((?!js|css|jpg|jpeg|png|ico)[a-zA-Z]{2,}))");
        rule.setSensitive(false);

        Rules rules = new Rules();
        rules.setType("Basic Information");
        ArrayList<Rule> rl = new ArrayList<>();
        rl.add(rule);
        rules.setRule(rl);
        ArrayList<Rules> rls = new ArrayList<>();
        rls.add(rules);
        RulesConfig config = new RulesConfig();
        config.setRules(rls);

        DumperOptions dop = new DumperOptions();
        dop.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer();
        representer.addClassTag(Config.class, Tag.MAP);

        Yaml yaml = new Yaml(new Constructor(),representer,dop);
        File f = new File(ConfigPath);
        try{
            Writer ws = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
            yaml.dump(config,ws);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Get configuration path
    public static String getConfigPath(){
        try {
            InputStream inorder = new FileInputStream(SettingPath);
            Map<String,Object> r = yaml.load(inorder);
            return r.get("configPath").toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ConfigPath;
        }

    }

    // Ge the color highlighting setting
    public String getHighlightMethod(){
        String highlightMethod = Config.highlightMethod[0];
        File yamlSetting = new File(SettingPath);
        if (yamlSetting.exists() && yamlSetting.isFile()) {
            try {
                InputStream inorder = new FileInputStream(SettingPath);
                Map<String,Object> r = yaml.load(inorder);
                highlightMethod = r.get("highlightMethod").toString();
            } catch (Exception e) {
                e.printStackTrace();
                highlightMethod = Config.highlightMethod[0];
            }
        } else {
            highlightMethod = Config.highlightMethod[0];
        }
        return highlightMethod;
    }

    // Get the suffix to exclude
    public String getExcludeSuffix(){
        String excludeSuffix = "";
        File yamlSetting = new File(SettingPath);
        if (yamlSetting.exists() && yamlSetting.isFile()) {
            try {
                InputStream inorder = new FileInputStream(SettingPath);
                Map<String,Object> r = yaml.load(inorder);
                excludeSuffix = r.get("excludeSuffix").toString();
            } catch (Exception e) {
                e.printStackTrace();
                excludeSuffix = "";
            }
        } else {
            excludeSuffix = Config.excludeSuffix;
        }
        return excludeSuffix;
    }

    // Get rule configuration
    public static Map<String,Object[][]> getRules(){
        InputStream inorder = null;
        {
            try {
                inorder = new FileInputStream(getConfigPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        Yaml yaml = new Yaml(new Constructor(RulesConfig.class));
        RulesConfig rulesConfig = yaml.loadAs(inorder, RulesConfig.class);
        Map<String,Object[][]> resRule = new HashMap<>();
        rulesConfig.rules.forEach(i->{
            ArrayList<Object[]> data = new ArrayList<>();
            i.rule.forEach(j->{
                try {
                    data.add(j.getRuleObject());
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            resRule.put(i.getType(), data.toArray(new Object[data.size()][]));
        });
        return resRule;
    }


    // Set the file exclusion list
    public void setExcludeSuffix(String excludeSuffix, String highlightMethod){
        Map<String,Object> r = new HashMap<>();
        r.put("configPath", getConfigPath());
        r.put("excludeSuffix", excludeSuffix);
        r.put("highlightMethod", highlightMethod);
        try{
            Writer ws = new OutputStreamWriter(new FileOutputStream(SettingPath), StandardCharsets.UTF_8);
            yaml.dump(r, ws);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}