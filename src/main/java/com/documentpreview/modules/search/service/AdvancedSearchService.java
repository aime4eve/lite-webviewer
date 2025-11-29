package com.documentpreview.modules.search.service;

import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.domain.SearchRequest;
import com.documentpreview.modules.search.domain.SearchResult;
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
    @Value("${app.scan.root-dirs}")
    private String rootDir;

    public AdvancedSearchService(SearchMetaRepository repo) { this.repo = repo; }

    public Result<List<SearchResult>> search(SearchRequest req, int limit) {
        try {
            List<SearchMeta> metas = repo.loadAll().getValue().orElse(List.of());
            String q = (req.getKeyword() == null ? "" : req.getKeyword()).trim().toLowerCase(Locale.ROOT);
            Set<String> typeSet = req.getFileTypes() == null ? Set.of() : req.getFileTypes().stream().filter(Objects::nonNull).map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
            String parent = (req.getParentDir() == null ? "" : req.getParentDir());

            List<SearchResult> results = new ArrayList<>();
            for (SearchMeta m : metas) {
                if (m.getFileType() != null && !typeSet.isEmpty() && !typeSet.contains(m.getFileType().name())) continue;
                if (parent != null && !parent.isBlank() && (m.getParentDir() == null || !m.getParentDir().startsWith(parent))) continue;
                if (req.getMinSize() != null && m.getSize() != null && m.getSize() < req.getMinSize()) continue;
                if (req.getMaxSize() != null && m.getSize() != null && m.getSize() > req.getMaxSize()) continue;
                if (req.getStartDate() != null && m.getModifiedAt() != null && m.getModifiedAt() < req.getStartDate()) continue;
                if (req.getEndDate() != null && m.getModifiedAt() != null && m.getModifiedAt() > req.getEndDate()) continue;

                String snippet = null;
                boolean isMatch = q.isEmpty(); // 空查询默认匹配

                if (!q.isEmpty()) {
                    // fileName/title match
                    String nameLower = m.getFileName() == null ? "" : m.getFileName().toLowerCase(Locale.ROOT);
                    String titleLower = m.getTitle() == null ? "" : m.getTitle().toLowerCase(Locale.ROOT);
                    
                    // 检查文件名匹配
                    if (nameLower.contains(q)) {
                        isMatch = true;
                        int pos = nameLower.indexOf(q);
                        int start = Math.max(0, pos - 40);
                        int end = Math.min(m.getFileName().length(), pos + q.length() + 40);
                        // 添加高亮标签
                        String fileName = m.getFileName();
                        String snippetBefore = fileName.substring(start, pos);
                        String matchedText = fileName.substring(pos, pos + q.length());
                        String snippetAfter = fileName.substring(pos + q.length(), end);
                        snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                        snippet = snippet.replaceAll("\n", " ");
                    }
                    // 检查标题匹配
                    else if (titleLower.contains(q)) {
                        isMatch = true;
                        int pos = titleLower.indexOf(q);
                        int start = Math.max(0, pos - 40);
                        int end = Math.min(m.getTitle().length(), pos + q.length() + 40);
                        // 添加高亮标签
                        String title = m.getTitle();
                        String snippetBefore = title.substring(start, pos);
                        String matchedText = title.substring(pos, pos + q.length());
                        String snippetAfter = title.substring(pos + q.length(), end);
                        snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                        snippet = snippet.replaceAll("\n", " ");
                    }
                    // 检查内容匹配
                    else if (m.getContentText() != null) {
                        String contentLower = m.getContentText().toLowerCase(Locale.ROOT);
                        int pos = contentLower.indexOf(q);
                        if (pos >= 0) {
                            isMatch = true;
                            int start = Math.max(0, pos - 40);
                            int end = Math.min(m.getContentText().length(), pos + q.length() + 40);
                            // 添加高亮标签
                            String content = m.getContentText();
                            String snippetBefore = content.substring(start, pos);
                            String matchedText = content.substring(pos, pos + q.length());
                            String snippetAfter = content.substring(pos + q.length(), end);
                            snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                            snippet = snippet.replaceAll("\n", " ");
                        }
                    }
                    // 对于PDF文件进行额外的内容匹配
                    else if (m.getFileType() == FileType.PDF) {
                        try (org.apache.pdfbox.pdmodel.PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(new java.io.File(java.nio.file.Paths.get(rootDir, m.getFilePath()).toString()))) {
                            int total = pdf.getNumberOfPages();
                            String qLower = q;
                            StringBuilder pat = new StringBuilder();
                            for (int i = 0; i < qLower.length(); i++) {
                                String ch = String.valueOf(qLower.charAt(i));
                                // escape regex special chars
                                pat.append(Pattern.quote(ch));
                                if (i < qLower.length() - 1) pat.append("[\\s\\-]*");
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
                                        isMatch = true;
                                        int st = Math.max(0, matcher.start() - 60);
                                        int ed = Math.min(pageText.length(), matcher.end() + 60);
                                        // 添加高亮标签
                                        String snippetBefore = pageText.substring(st, matcher.start());
                                        String matchedText = pageText.substring(matcher.start(), matcher.end());
                                        String snippetAfter = pageText.substring(matcher.end(), ed);
                                        snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                                        snippet = snippet.replaceAll("\n", " ");
                                        break;
                                    }
                            }
                        } catch (Exception ignore) {}
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
