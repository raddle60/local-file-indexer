package com.raddle.search;

import com.raddle.search.model.SearchResult;

public interface SearchManager {

    public SearchResult search(String keywords);

    public String[] parseForInFileSearch(String keywords);
}
