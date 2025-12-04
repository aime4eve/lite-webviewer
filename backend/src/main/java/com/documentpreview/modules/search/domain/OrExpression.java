package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchExpression;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * OR表达式，代表多个表达式的OR逻辑关系
 */
public class OrExpression implements SearchExpression {
    
    private final List<SearchExpression> expressions;
    
    /**
     * 构造函数
     * @param expressions 子表达式列表
     */
    public OrExpression(List<SearchExpression> expressions) {
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
        // 只要有一个子表达式匹配就返回true
        for (SearchExpression expression : expressions) {
            if (expression.matches(meta)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Criteria buildCriteria() {
        if (expressions.isEmpty()) {
            return new Criteria();
        }
        
        // 构建OR查询，只要有一个子表达式的条件满足即可
        Criteria criteria = expressions.get(0).buildCriteria();
        for (int i = 1; i < expressions.size(); i++) {
            criteria = criteria.or(expressions.get(i).buildCriteria());
        }
        return criteria;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append(expressions.get(i).toString());
        }
        return sb.toString();
    }
}
