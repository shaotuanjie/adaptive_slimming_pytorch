package com.itheima.cdl.service.impl;

import com.itheima.cdl.service.ApUserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {


    /**
     * 保存搜索历史记录
     * @param userId
     * @param keyword
     */
    @Async("taskExecutor")
    @Override
    public void insert(Integer userId, String keyword) {

        //保存用户记录  mongodb或mysql
        //执行业务

        log.info("用户搜索记录保存成功,用户id:{},关键字:{}",userId,keyword);

    }
}
