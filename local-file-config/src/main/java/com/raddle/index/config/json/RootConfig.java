package com.raddle.index.config.json;

import java.util.HashMap;
import java.util.Map;

import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;

public class RootConfig {

    private Map<String, IndexConfig> indexConfigs = new HashMap<String, IndexConfig>();
    private GlobalConfig globalConfig = new GlobalConfig();

    public Map<String, IndexConfig> getIndexConfigs() {
        if (indexConfigs == null) {
            indexConfigs = new HashMap<String, IndexConfig>();
        }
        return indexConfigs;
    }

    public void setIndexConfigs(Map<String, IndexConfig> indexConfigs) {
        this.indexConfigs = indexConfigs;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

}
