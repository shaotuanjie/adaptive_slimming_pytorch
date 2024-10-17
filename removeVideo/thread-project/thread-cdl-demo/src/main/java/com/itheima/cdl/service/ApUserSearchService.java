package com.itheima.cdl.service;

public interface ApUserSearchService {

    /**
     * 保存搜索历史记录
     * @param userId
     * @param keyword
     */
    public void insert(Integer userId,String keyword);

}
