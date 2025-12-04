package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchExpression;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * AND表达式，代表多个表达式的AND逻辑关系
 */
public class AndExpression implements SearchExpression {
    
    private final List<SearchExpression> expressions;
    
    /**
     * 构造函数
     * @param expressions 子表达式列表
     */
    public AndExpression(List<SearchExpression> expressions) {
        this.expressions = new ArrayList<>(expressions);
    }
    
    /**
     * 添加子表达式
     * @param expression 子表达式
     */
    public void addExpression(SearchExpression expression) {
        expressions.add(expression);
    }
    
    /**
     * 获取子表达式列表
     * @return 子表达式列表
     */
    public List<SearchExpression> getExpressions() {
        return new ArrayList<>(expressions);
    }
    
    @Override
    public boolean matches(SearchMeta meta) {
        // 所有子表达式都必须匹配
        for (SearchExpression expression : expressions) {
            if (!expression.matches(meta)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Criteria buildCriteria() {
        if (expressions.isEmpty()) {
            return new Criteria();
        }
        
        // 构建AND查询，所有子表达式的条件都必须满足
        Criteria criteria = expressions.get(0).buildCriteria();
        for (int i = 1; i < expressions.size(); i++) {
            criteria = criteria.and(expressions.get(i).buildCriteria());
        }
        return criteria;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append(expressions.get(i).toString());
        }
        return sb.toString();
    }
}
