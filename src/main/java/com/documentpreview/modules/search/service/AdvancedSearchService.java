package com.documentpreview.modules.search.service;

import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchRequest;
import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.modules.search.domain.SearchExpression;
import com.documentpreview.modules.search.repository.SearchMetaRepository;
import com.documentpreview.shared.ddd.Result;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvancedSearchService {
    private final SearchMetaRepository repo;
    private final SearchExpressionParser parser;
    @Value("${app.scan.root-dirs}")
    private String rootDir;

    public AdvancedSearchService(SearchMetaRepository repo) { 
        this.repo = repo; 
        this.parser = new SearchExpressionParser();
    }

    /**
     * 构建搜索结果摘要
     * @param meta 搜索元数据
     * @param keyword 搜索关键字
     * @return 搜索结果摘要
     */
    private String buildSnippet(SearchMeta meta, String keyword) {
        // 提取所有关键字，尝试每个关键字来生成摘要
        String[] keywords = keyword.split("\\s+");
        
        // 优先尝试在文件名中匹配
        if (meta.getFileName() != null) {
            String fileName = meta.getFileName();
            for (String kw : keywords) {
                String keywordLower = kw.toLowerCase(Locale.ROOT);
                int pos = fileName.toLowerCase(Locale.ROOT).indexOf(keywordLower);
                if (pos >= 0) {
                    int start = Math.max(0, pos - 40);
                    int end = Math.min(fileName.length(), pos + kw.length() + 40);
                    String snippetBefore = fileName.substring(start, pos);
                    String matchedText = fileName.substring(pos, pos + kw.length());
                    String snippetAfter = fileName.substring(pos + kw.length(), end);
                    return snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                }
            }
        }
        
        // 优先尝试在标题中匹配
        if (meta.getTitle() != null) {
            String title = meta.getTitle();
            for (String kw : keywords) {
                String keywordLower = kw.toLowerCase(Locale.ROOT);
                int pos = title.toLowerCase(Locale.ROOT).indexOf(keywordLower);
                if (pos >= 0) {
                    int start = Math.max(0, pos - 40);
                    int end = Math.min(title.length(), pos + kw.length() + 40);
                    String snippetBefore = title.substring(start, pos);
                    String matchedText = title.substring(pos, pos + kw.length());
                    String snippetAfter = title.substring(pos + kw.length(), end);
                    return snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                }
            }
        }
        
        // 尝试在内容中匹配
        if (meta.getContentText() != null) {
            String content = meta.getContentText();
            for (String kw : keywords) {
                String keywordLower = kw.toLowerCase(Locale.ROOT);
                int pos = content.toLowerCase(Locale.ROOT).indexOf(keywordLower);
                if (pos >= 0) {
                    int start = Math.max(0, pos - 40);
                    int end = Math.min(content.length(), pos + kw.length() + 40);
                    String snippetBefore = content.substring(start, pos);
                    String matchedText = content.substring(pos, pos + kw.length());
                    String snippetAfter = content.substring(pos + kw.length(), end);
                    return snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                }
            }
        }
        

        
        // 对于PDF文件进行额外的内容匹配
        if (meta.getFileType() == FileType.PDF) {
            for (String kw : keywords) {
                String keywordLower = kw.toLowerCase(Locale.ROOT);
                try (org.apache.pdfbox.pdmodel.PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(new java.io.File(java.nio.file.Paths.get(rootDir, meta.getFilePath()).toString()))) {
                    int total = pdf.getNumberOfPages();
                    StringBuilder pat = new StringBuilder();
                    for (int i = 0; i < keywordLower.length(); i++) {
                        String ch = String.valueOf(keywordLower.charAt(i));
                        // escape regex special chars
                        pat.append(Pattern.quote(ch));
                        if (i < keywordLower.length() - 1) pat.append("[\\s\\-]*");
                    }
                    Pattern regex = Pattern.compile(pat.toString(), Pattern.CASE_INSENSITIVE);
                    for (int p = 1; p <= total; p++) {
                        org.apache.pdfbox.text.PDFTextStripper s = new org.apache.pdfbox.text.PDFTextStripper();
                        s.setSortByPosition(true);
                        s.setStartPage(p);
                        s.setEndPage(p);
                        String pageText = s.getText(pdf);
                        if (pageText == null || pageText.isBlank()) continue;
                        Matcher matcher = regex.matcher(pageText);
                        if (matcher.find()) {
                                int st = Math.max(0, matcher.start() - 60);
                                int ed = Math.min(pageText.length(), matcher.end() + 60);
                                String snippetBefore = pageText.substring(st, matcher.start());
                                String matchedText = pageText.substring(matcher.start(), matcher.end());
                                String snippetAfter = pageText.substring(matcher.end(), ed);
                                return snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                            }
                    }
                } catch (Exception ignore) {}
            }
        }
        
        // 如果没有找到匹配，返回默认摘要
        return "...";
    }
    
    public Result<List<SearchResult>> search(SearchRequest req, int limit) {
        try {
            List<SearchMeta> metas = repo.loadAll().getValue().orElse(List.of());
            String keyword = (req.getKeyword() == null ? "" : req.getKeyword()).trim();
            Set<String> typeSet = req.getFileTypes() == null ? Set.of() : req.getFileTypes().stream().filter(Objects::nonNull).map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
            String parent = (req.getParentDir() == null ? "" : req.getParentDir());

            List<SearchResult> results = new ArrayList<>();
            
            // 解析搜索表达式
            SearchExpression expression;
            try {
                expression = parser.parse(keyword);
            } catch (IllegalArgumentException e) {
                // 搜索语法错误，返回错误信息
                return Result.failure("搜索语法错误: " + e.getMessage() + "\n\n" + parser.getSyntaxHelp());
            }
            
            for (SearchMeta m : metas) {
                if (m.getFileType() != null && !typeSet.isEmpty() && !typeSet.contains(m.getFileType().name())) continue;
                if (parent != null && !parent.isBlank() && (m.getParentDir() == null || !m.getParentDir().startsWith(parent))) continue;
                if (req.getMinSize() != null && m.getSize() != null && m.getSize() < req.getMinSize()) continue;
                if (req.getMaxSize() != null && m.getSize() != null && m.getSize() > req.getMaxSize()) continue;
                if (req.getStartDate() != null && m.getModifiedAt() != null && m.getModifiedAt() < req.getStartDate()) continue;
                if (req.getEndDate() != null && m.getModifiedAt() != null && m.getModifiedAt() > req.getEndDate()) continue;

                String snippet = null;
                boolean isMatch = keyword.isEmpty(); // 空查询默认匹配

                if (!keyword.isEmpty()) {
                    // 使用搜索表达式匹配
                    isMatch = expression.matches(m);
                    
                    // 如果匹配，构建摘要
                    if (isMatch) {
                        // 简单的摘要生成，使用第一个匹配的关键字
                        snippet = buildSnippet(m, keyword);
                    }
                }

                if (isMatch) {
                    results.add(new SearchResult(m.getFilePath(), m.getFileName(), m.getParentDir(), m.getFileType(), 0, snippet));
                }
            }

            // sort
            Comparator<SearchResult> cmp = null;
            String sort = (req.getSort() == null || req.getSort().isBlank()) ? "name" : req.getSort();
            String order = (req.getOrder() == null || req.getOrder().isBlank()) ? "asc" : req.getOrder();
            
            // 默认为按文件名排序
            cmp = Comparator.comparing(SearchResult::getFileName, String.CASE_INSENSITIVE_ORDER);
            if ("desc".equalsIgnoreCase(order)) {
                cmp = cmp.reversed();
            }
            
            List<SearchResult> out = results.stream()
                    .sorted(cmp)
                    .limit(Math.max(1, limit))
                    .collect(Collectors.toList());
            return Result.success(out);
        } catch (Exception e) {
            return Result.failure("Advanced search failed: " + e.getMessage());
        }
    }
}
