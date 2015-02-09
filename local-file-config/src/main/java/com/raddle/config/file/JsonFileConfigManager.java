package com.raddle.config.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.raddle.config.ConfigManager;
import com.raddle.index.config.GlobalConfig;
import com.raddle.index.config.IndexConfig;
import com.raddle.index.config.json.RootConfig;

/**
 * 类JsonFileConfigManager.java的实现描述：json文件存储
 * @author raddle60 2013-5-10 下午10:22:09
 */
public class JsonFileConfigManager implements ConfigManager {

    private final static Logger logger = LoggerFactory.getLogger(JsonFileConfigManager.class);

    private String configFile;

    private RootConfig rootConfig;

    public void init() {
        if (!new File(configFile).exists()) {
            rootConfig = new RootConfig();
        } else {
            try {
                String json = FileUtils.readFileToString(new File(configFile), "utf-8");
                if (StringUtils.isNotBlank(json)) {
                    rootConfig = JSONObject.parseObject(json, RootConfig.class);
                } else {
                    rootConfig = new RootConfig();
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void saveConfig() {
        if (!new File(configFile).exists()) {
            new File(configFile).getParentFile().mkdirs();
            logger.info("mkdir {}", new File(configFile).getParentFile().getAbsolutePath());
        }
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(configFile)), "utf-8");
            outputStreamWriter.write(JSONObject.toJSONString(rootConfig));
            outputStreamWriter.close();
            logger.info("save config to {}", new File(configFile).getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String saveOrUpdateIndexConfig(IndexConfig config) {
        if (StringUtils.isEmpty(config.getId())) {
            config.setId(UUID.randomUUID().toString());
        }
        rootConfig.getIndexConfigs().put(config.getId(), config);
        saveConfig();
        return config.getId();
    }

    @Override
    public IndexConfig getIndexConfig(String id) {
        return rootConfig.getIndexConfigs().get(id);
    }

    @Override
    public void deleteIndexConfig(String id) {
        rootConfig.getIndexConfigs().remove(id);
        saveConfig();
    }

    @Override
    public List<IndexConfig> getAllIndexConfig() {
        List<IndexConfig> list = new ArrayList<IndexConfig>(rootConfig.getIndexConfigs().values());
        Collections.sort(list, new Comparator<IndexConfig>() {

            @Override
            public int compare(IndexConfig o1, IndexConfig o2) {
                if (o1.getName() != null && o2.getName() != null) {
                    return o1.getName().compareTo(o2.getName());
                }
                return 0;
            }
        });
        return list;
    }

    @Override
    public GlobalConfig getGlobalConfig() {
        return rootConfig.getGlobalConfig();
    }

    @Override
    public void saveGlobalConfig(GlobalConfig globalConfig) {
        rootConfig.setGlobalConfig(globalConfig);
        saveConfig();
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

}
