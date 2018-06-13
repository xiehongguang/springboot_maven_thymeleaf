package com.example.base;


public interface IConfigurationService {
    /**
     * 读取本地配置文件某个配置项
     * @param name
     * @return
     */
    String get(String name);

}
