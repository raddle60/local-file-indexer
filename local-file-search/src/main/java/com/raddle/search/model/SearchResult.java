package com.raddle.search.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raddle.index.enums.IndexedField;

/**
 * 类SearchResult.java的实现描述：搜索结果
 * @author raddle60 2013-5-7 下午10:37:12
 */
public class SearchResult {

    private List<Map<IndexedField, Object>> results = new ArrayList<Map<IndexedField, Object>>();
    private int totalCount;

    public List<Map<IndexedField, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<IndexedField, Object>> results) {
        this.results = results;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
