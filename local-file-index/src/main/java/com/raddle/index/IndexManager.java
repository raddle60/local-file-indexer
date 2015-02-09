package com.raddle.index;

import com.raddle.index.config.IndexConfig;
import com.raddle.index.observer.ProgressObserver;

/**
 * 类IndexManager.java的实现描述：索引管理
 * @author raddle60 2013-5-6 下午9:23:46
 */
public interface IndexManager {

    public void rebuildIndex(String docDir, ProgressObserver progressObserver, IndexConfig indexConfig);

    public void updateIndex(String docDir, ProgressObserver progressObserver, IndexConfig indexConfig);
}
