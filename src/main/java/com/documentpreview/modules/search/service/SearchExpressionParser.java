package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.domain.SearchExpression;
import com.documentpreview.modules.search.domain.KeywordExpression;
import com.documentpreview.modules.search.domain.AndExpression;
import com.documentpreview.modules.search.domain.OrExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 搜索表达式解析器，负责解析用户输入的搜索字符串，将其转换为抽象语法树(AST)
 */
public class SearchExpressionParser {
    
    // 正则表达式，用于匹配关键字和运算符
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\s*(AND|OR|\\S+)\\s*");
    
    /**
     * 解析搜索字符串，返回抽象语法树
     * @param searchString 搜索字符串
     * @return 抽象语法树
     * @throws IllegalArgumentException 如果搜索字符串格式无效
     */
    public SearchExpression parse(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            // 空搜索字符串，返回一个匹配所有的表达式
            return new KeywordExpression("");
        }
        
        // 执行词法分析，获取标记列表
        List<String> tokens = tokenize(searchString.trim());
        if (tokens.isEmpty()) {
            return new KeywordExpression("");
        }
        
        // 执行语法分析，构建抽象语法树
        return parseExpression(tokens, 0).expression;
    }
    
    /**
     * 词法分析，将搜索字符串分解为标记列表
     * @param searchString 搜索字符串
     * @return 标记列表
     */
    private List<String> tokenize(String searchString) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(searchString);
        
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token != null) {
                tokens.add(token);
            }
        }
        
        return tokens;
    }
    
    /**
     * 语法分析结果类
     */
    private static class ParseResult {
        SearchExpression expression;
        int position;
        
        ParseResult(SearchExpression expression, int position) {
            this.expression = expression;
            this.position = position;
        }
    }
    
    /**
     * 解析表达式
     * expression = or_expression
     * @param tokens 标记列表
     * @param position 当前位置
     * @return 解析结果
     */
    private ParseResult parseExpression(List<String> tokens, int position) {
        return parseOrExpression(tokens, position);
    }
    
    /**
     * 解析OR表达式
     * or_expression = term (OR term)*
     * @param tokens 标记列表
     * @param position 当前位置
     * @return 解析结果
     */
    private ParseResult parseOrExpression(List<String> tokens, int position) {
        // 解析第一个关键字
        ParseResult result = parseTerm(tokens, position);
        List<SearchExpression> expressions = new ArrayList<>();
        expressions.add(result.expression);
        
        // 继续解析后续的关键词（空格分隔的多个关键词默认使用OR关系）
        while (result.position < tokens.size()) {
            String token = tokens.get(result.position);
            // 如果遇到显式的AND操作符，停止解析OR关系
            if ("AND".equalsIgnoreCase(token)) {
                break;
            }
            
            // 如果是显式的OR操作符，跳过它
            if ("OR".equalsIgnoreCase(token)) {
                result.position++; // 跳过OR操作符
                if (result.position >= tokens.size()) {
                    throw new IllegalArgumentException("Unexpected end of search string after OR operator");
                }
            }
            
            // 解析下一个关键字（空格分隔的关键词默认使用OR关系）
            ParseResult nextResult = parseTerm(tokens, result.position);
            expressions.add(nextResult.expression);
            result = nextResult;
        }
        
        // 如果只有一个表达式，直接返回
        if (expressions.size() == 1) {
            return result;
        }
        
        // 否则返回OR表达式
        return new ParseResult(new OrExpression(expressions), result.position);
    }
    
    /**
     * 解析关键字
     * term = keyword
     * @param tokens 标记列表
     * @param position 当前位置
     * @return 解析结果
     */
    private ParseResult parseTerm(List<String> tokens, int position) {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of search string");
        }
        
        String token = tokens.get(position);
        // 检查是否为运算符
        if ("AND".equalsIgnoreCase(token) || "OR".equalsIgnoreCase(token)) {
            throw new IllegalArgumentException("Unexpected operator: " + token);
        }
        
        // 返回关键字表达式
        return new ParseResult(new KeywordExpression(token), position + 1);
    }
    
    /**
     * 验证搜索字符串格式是否有效
     * @param searchString 搜索字符串
     * @return 是否有效
     */
    public boolean isValid(String searchString) {
        try {
            parse(searchString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取搜索语法说明
     * @return 搜索语法说明
     */
    public String getSyntaxHelp() {
        return "搜索语法说明：\n" +
               "1. 多个关键字用空格分隔，默认逻辑关系为OR\n" +
               "2. 支持显式AND和OR运算符，例如：keyword1 AND keyword2 OR keyword3\n" +
               "3. AND优先级高于OR，例如：keyword1 OR keyword2 AND keyword3 等价于 keyword1 OR (keyword2 AND keyword3)\n" +
               "4. 示例：\n" +
               "   - 搜索包含keyword1或keyword2的文件：keyword1 keyword2\n" +
               "   - 搜索同时包含keyword1和keyword2的文件：keyword1 AND keyword2\n" +
               "   - 搜索包含keyword1或同时包含keyword2和keyword3的文件：keyword1 OR keyword2 AND keyword3";
    }
}
