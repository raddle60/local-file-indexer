package com.raddle.config;

import java.util.List;

import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;

/**
 * 类ConfigManager.java的实现描述：配置管理器
 * @author raddle60 2013-5-10 下午10:16:15
 */
public interface ConfigManager {

    public String saveOrUpdateIndexConfig(IndexConfig config);

    public IndexConfig getIndexConfig(String id);

    public void deleteIndexConfig(String id);

    public List<IndexConfig> getAllIndexConfig();

    public GlobalConfig getGlobalConfig();

    public void saveGlobalConfig(GlobalConfig globalConfig);
}
