package com.example.base;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by songyc on 2017/8/8.
 */
public class ConfigurationService implements IConfigurationService {

    private Map<String,String> map=new HashMap<String,String>();

    /**
     * 初始化加载各工程的配置文件信息
     */
    public ConfigurationService() {
        if (map.size()==0){
            String[] PropertiesFiles={"application","cache","database","search"};
            for (String fileName:PropertiesFiles){
                ResourceBundle bundle =  ResourceBundle.getBundle(fileName);
              //  LogUtils.info("读取配置文件"+fileName+".properties ......");
                Enumeration<String> allKey = bundle.getKeys();
                while (allKey.hasMoreElements()) {
                    String key = allKey.nextElement();
                    String value =new String(bundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    map.put(key, value);
                }
            }
        }else{
          //  LogUtils.info("配置文件不在读取......");
        }
    }

    /**
     * 读取配置项的配置值信息
     */
    @Override
    public String get(String name) {
        String value = map.get(name);
        if (value != null) {
            return value;
        }
        return "";
    }
}
