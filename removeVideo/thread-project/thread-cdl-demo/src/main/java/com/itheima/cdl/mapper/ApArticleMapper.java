package com.itheima.cdl.mapper;

import com.itheima.cdl.pojo.SearchArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper {

    public List<SearchArticleVo> loadArticleList(@Param("page") int page, @Param("pageSize") int pageSize);

    public int selectCount();
}
