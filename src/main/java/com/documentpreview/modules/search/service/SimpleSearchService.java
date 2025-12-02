package com.documentpreview.modules.search.service;

import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchExpression;
import com.documentpreview.modules.search.repository.SearchMetaRepository;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.domain.FilesIndexItem;
import com.documentpreview.modules.preview.service.PreviewService;
import com.documentpreview.shared.ddd.Result;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SimpleSearchService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSearchService.class);

    private final PreviewService previewService;
    private final SearchMetaRepository searchMetaRepository;
    private final SearchExpressionParser parser;
    @Value("${app.scan.root-dirs}")
    private String rootDir;

    public SimpleSearchService(PreviewService previewService, SearchMetaRepository searchMetaRepository) {
        this.previewService = previewService;
        this.searchMetaRepository = searchMetaRepository;
        this.parser = new SearchExpressionParser();
    }

    public Result<List<SearchResult>> search(FilesIndex index, String keyword, int limit) {
        try {
            String q = (keyword == null ? "" : keyword).trim();
            if (q.isEmpty()) {
                // return top N by name for empty query (quick suggestions)
            List<SearchResult> defaults = index.getFiles().stream()
                    .limit(Math.max(1, limit))
                    .map(i -> new SearchResult(i.getPath(), i.getName(), i.getPath().contains("/") ? i.getPath().substring(0, i.getPath().lastIndexOf('/')) : "", FileType.fromExtension(org.apache.commons.io.FilenameUtils.getExtension(i.getName()).toLowerCase(Locale.ROOT)), 0, null))
                    .collect(Collectors.toList());
                return Result.success(defaults);
            }

            List<SearchResult> results = new ArrayList<>();
            // load prebuilt metas for content search
            List<SearchMeta> metas = searchMetaRepository.loadAll().getValue().orElse(List.of());
            var metaByPath = metas.stream().collect(java.util.stream.Collectors.toMap(SearchMeta::getFilePath, m -> m));
            
            // 解析搜索表达式
            SearchExpression expression;
            try {
                expression = parser.parse(q);
            } catch (IllegalArgumentException e) {
                // 搜索语法错误，返回错误信息
                return Result.failure("搜索语法错误: " + e.getMessage() + "\n\n" + parser.getSyntaxHelp());
            }
            
            for (FilesIndexItem item : index.getItems()) {
                if (!"file".equalsIgnoreCase(item.getType())) continue;

                String fileName = item.getName();
                String filePath = item.getPath();
                String parentDir = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf('/')) : "";
                String ext = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);
                FileType fileType = FileType.fromExtension(ext);

                String snippet = null;
                boolean isMatch = false;

                // 使用搜索表达式匹配
                SearchMeta meta = metaByPath.get(filePath);
                if (meta != null) {
                    isMatch = expression.matches(meta);
                } else {
                    // 如果没有搜索元数据，只检查文件名
                    SearchMeta tempMeta = new SearchMeta(filePath, fileName, parentDir, fileType, fileName, null, null, 0L);
                    isMatch = expression.matches(tempMeta);
                }

                // 如果匹配，构建摘要
                if (isMatch) {
                    // 简单的摘要生成，使用第一个关键字
                    String firstKeyword = q.split("\\s+")[0];
                    String firstKeywordLower = firstKeyword.toLowerCase(Locale.ROOT);
                    
                    // 文件名匹配检查
                    if (fileName.toLowerCase(Locale.ROOT).contains(firstKeywordLower)) {
                        int pos = fileName.toLowerCase(Locale.ROOT).indexOf(firstKeywordLower);
                        int start = Math.max(0, pos - 10);
                        int end = Math.min(fileName.length(), pos + firstKeyword.length() + 10);
                        // 添加高亮标签
                        String snippetBefore = fileName.substring(start, pos);
                        String matchedText = fileName.substring(pos, pos + firstKeyword.length());
                        String snippetAfter = fileName.substring(pos + firstKeyword.length(), end);
                        snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                    } else if (meta != null && meta.getContentText() != null) {
                        // 内容匹配检查
                        String contentLower = meta.getContentText().toLowerCase(Locale.ROOT);
                        int pos = contentLower.indexOf(firstKeywordLower);
                        if (pos >= 0) {
                            int start = Math.max(0, pos - 40);
                            int end = Math.min(meta.getContentText().length(), pos + firstKeyword.length() + 40);
                            // 添加高亮标签
                            String content = meta.getContentText();
                            String snippetBefore = content.substring(start, pos);
                            String matchedText = content.substring(pos, pos + firstKeyword.length());
                            String snippetAfter = content.substring(pos + firstKeyword.length(), end);
                            snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                            snippet = snippet.replaceAll("\n", " ");
                        }
                    }
                }

                if (isMatch) {
                    results.add(new SearchResult(filePath, fileName, parentDir, fileType, 0, snippet));
                }
            }

            List<SearchResult> sorted = results.stream()
                    .limit(Math.max(1, limit))
                    .collect(Collectors.toList());
            return Result.success(sorted);
        } catch (Exception e) {
            logger.error("Search failed: {}", e.getMessage(), e);
            return Result.failure("Search failed: " + e.getMessage());
        }
    }
}
