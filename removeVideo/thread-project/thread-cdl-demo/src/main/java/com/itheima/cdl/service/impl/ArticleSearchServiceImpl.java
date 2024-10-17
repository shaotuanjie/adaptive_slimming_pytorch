package com.itheima.cdl.service.impl;

import com.alibaba.fastjson.JSON;
import com.itheima.cdl.service.ApUserSearchService;
import com.itheima.cdl.service.ArticleSearchService;
import com.itheima.cdl.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {

    @Autowired
    private RestHighLevelClient client;

    private static final String ARTICLE_ES_INDEX = "app_info_article";

    private int userId = 1102;

    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * 文章搜索
     * @return
     */
    @Override
    public List<Map> search(String keyword) {

        try {
            SearchRequest request = new SearchRequest(ARTICLE_ES_INDEX);

            //设置查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //第一个条件
            if(null == keyword || "".equals(keyword)){
                request.source().query(QueryBuilders.matchAllQuery());
            }else {
                request.source().query(QueryBuilders.queryStringQuery(keyword).field("title").defaultOperator(Operator.OR));
                //保存搜索历史
                apUserSearchService.insert(userId,keyword);
            }
            //分页
            request.source().from(0);
            request.source().size(20);

            //按照时间倒序排序
            request.source().sort("publishTime", SortOrder.DESC);
            //搜索
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            //解析结果
            SearchHits searchHits = response.getHits();
            //获取具体文档数据
            SearchHit[] hits = searchHits.getHits();
            List<Map> resultList = new ArrayList<>();
            for (SearchHit hit : hits) {
                //文档数据
                Map map = JSON.parseObject(hit.getSourceAsString(), Map.class);
                resultList.add(map);
            }
            return resultList;

        } catch (IOException e) {
            throw new RuntimeException("搜索失败");
        }

    }
}
