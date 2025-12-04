package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchExpression;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.Locale;

/**
 * 关键字表达式，代表单个关键字
 */
public class KeywordExpression implements SearchExpression {
    
    private final String keyword;
    private final String keywordLower;
    
    /**
     * 构造函数
     * @param keyword 关键字
     */
    public KeywordExpression(String keyword) {
        this.keyword = keyword;
        this.keywordLower = keyword.toLowerCase(Locale.ROOT);
    }
    
    /**
     * 获取关键字
     * @return 关键字
     */
    public String getKeyword() {
        return keyword;
    }
    
    @Override
    public boolean matches(SearchMeta meta) {
        if (meta == null) {
            return false;
        }
        
        // 检查文件名是否包含关键字
        if (meta.getFileName() != null && meta.getFileName().toLowerCase(Locale.ROOT).contains(keywordLower)) {
            return true;
        }
        
        // 检查标题是否包含关键字
        if (meta.getTitle() != null && meta.getTitle().toLowerCase(Locale.ROOT).contains(keywordLower)) {
            return true;
        }
        
        // 检查内容是否包含关键字
        if (meta.getContentText() != null && meta.getContentText().toLowerCase(Locale.ROOT).contains(keywordLower)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Criteria buildCriteria() {
        // 构建多字段匹配查询，匹配文件名、标题和内容
        return new Criteria("fileName").contains(keyword)
                .or("title").contains(keyword)
                .or("content").contains(keyword);
    }
    
    @Override
    public String toString() {
        return keyword;
    }
}
