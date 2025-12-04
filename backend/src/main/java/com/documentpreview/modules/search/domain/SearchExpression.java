package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.search.domain.SearchMeta;
import org.springframework.data.elasticsearch.core.query.Criteria;

/**
 * 搜索表达式接口，定义搜索表达式的基本方法
 */
public interface SearchExpression {
    
    /**
     * 判断给定的搜索元数据是否匹配该表达式
     * @param meta 搜索元数据
     * @return 是否匹配
     */
    boolean matches(SearchMeta meta);
    
    /**
     * 构建Elasticsearch查询条件
     * @return Elasticsearch查询条件
     */
    Criteria buildCriteria();
    
    /**
     * 获取表达式的字符串表示
     * @return 表达式的字符串表示
     */
    String toString();
}
