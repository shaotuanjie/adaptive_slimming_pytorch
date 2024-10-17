package com.itheima.cdl.pojo;

import lombok.Data;
import java.util.Date;

@Data
public class SearchArticleVo {

    // 文章id
    private Long id;
    // 文章标题
    private String title;
    // 文章发布时间
    private Date publishTime;
    // 封面
    private String images;

}