import com.documentpreview.modules.search.service.SearchExpressionParser;
import com.documentpreview.modules.search.domain.SearchExpression;

public class TestSearchParser {
    public static void main(String[] args) {
        SearchExpressionParser parser = new SearchExpressionParser();
        
        // 测试"起床 清华大学"的解析
        String searchText = "起床 清华大学";
        System.out.println("测试搜索词: " + searchText);
        
        try {
            SearchExpression expression = parser.parse(searchText);
            System.out.println("解析结果: " + expression.toString());
            System.out.println("表达式类型: " + expression.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("解析错误: " + e.getMessage());
        }
        
        // 测试"起床 AND 清华大学"的解析
        String searchText2 = "起床 AND 清华大学";
        System.out.println("\n测试搜索词: " + searchText2);
        
        try {
            SearchExpression expression2 = parser.parse(searchText2);
            System.out.println("解析结果: " + expression2.toString());
            System.out.println("表达式类型: " + expression2.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("解析错误: " + e.getMessage());
        }
    }
}