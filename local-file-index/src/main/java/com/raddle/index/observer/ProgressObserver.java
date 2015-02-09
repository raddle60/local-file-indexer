package com.raddle.index.observer;

public interface ProgressObserver {

    /**
     * 正在遍历文件
     * @param count
     */
    public void fileCollecting(int count);

    /**
     * 已遍历完成
     * @param count
     * @param total
     * @param ignored
     */
    public void fileCollected(int count, int total, int ignored);

    /**
     * 正在建立索引
     * @param count
     */
    public void fileIndexing(int count);

    /**
     * 已建立索引
     * @param count
     * @param terminated
     */
    public void fileIndexed(int count, boolean terminated);

    /**
     * 是否暂停
     * @return
     */
    public boolean isPaused();

    /**
     * 是否终止
     * @return
     */
    public boolean isTerminated();
}
