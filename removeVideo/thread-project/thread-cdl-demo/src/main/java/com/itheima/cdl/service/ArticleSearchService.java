package com.itheima.cdl.service;


import java.util.List;
import java.util.Map;

public interface ArticleSearchService {


    /**
     * 文章搜索
     * @return
     */
    public List<Map> search(String keyword);
}
