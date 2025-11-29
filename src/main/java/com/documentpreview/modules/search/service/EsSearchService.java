package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.domain.SearchRequest;
import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.es.EsDocumentRepository;
import com.documentpreview.shared.ddd.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// keep JSON DSL via StringQuery to avoid client builder incompatibilities

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ConditionalOnProperty(value = "app.search.use-es", havingValue = "true")
@Service
public class EsSearchService {
    private final ElasticsearchOperations operations;
    private final EsDocumentRepository repo;

    @Value("${app.search.use-es:true}")
    private boolean useEs;

    @Value("${spring.elasticsearch.uris:}")
    private String esUris;
    
    @Value("${app.search.es-index-name:documents}")
    private String esIndexName;

    public EsSearchService(ElasticsearchOperations operations, EsDocumentRepository repo) {
        this.operations = operations;
        this.repo = repo;
    }

    public boolean enabled() { 
        try {
            // 检查Elasticsearch连接和索引是否正常
            if (!useEs || esUris == null || esUris.trim().isEmpty()) {
                return false;
            }
            // 简单的连接测试
            String base = esUris.split(",")[0].trim();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/" + esIndexName))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200; // 索引存在返回true
        } catch (Exception e) {
            // 连接失败或索引不存在，返回false
            return false;
        }
    }

    public Result<List<SearchResult>> search(SearchRequest req, int limit) {
        try {
            String q = (req.getKeyword() == null ? "" : req.getKeyword()).trim();
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"bool\":{");
            boolean hasPrev = false;
            if (!q.isEmpty()) {
                json.append("\"must\":[{\"multi_match\":{\"query\":\"")
                    .append(q.replace("\"","\\\""))
                    .append("\",\"fields\":[\"fileName^3\",\"title^2\",\"content\"]}}]");
                hasPrev = true;
            }
            java.util.List<String> filters = new java.util.ArrayList<>();
            if (req.getFileTypes() != null && !req.getFileTypes().isEmpty()) {
                StringBuilder f = new StringBuilder();
                f.append("{\"terms\":{\"fileType\":[");
                for (int i=0;i<req.getFileTypes().size();i++){ if(i>0) f.append(","); f.append("\"").append(req.getFileTypes().get(i)).append("\""); }
                f.append("]}}");
                filters.add(f.toString());
            }
            if (req.getParentDir() != null && !req.getParentDir().isBlank()) {
                filters.add("{\"prefix\":{\"parentDir\":\"" + req.getParentDir().replace("\"","\\\"") + "\"}}");
            }
            if (req.getMinSize() != null || req.getMaxSize() != null) {
                StringBuilder f = new StringBuilder("{\"range\":{\"size\":{");
                boolean first=true; if(req.getMinSize()!=null){ f.append("\"gte\":"+req.getMinSize()); first=false; }
                if(req.getMaxSize()!=null){ if(!first) f.append(","); f.append("\"lte\":"+req.getMaxSize()); }
                f.append("}}}"); filters.add(f.toString());
            }
            if (req.getStartDate() != null || req.getEndDate() != null) {
                StringBuilder f = new StringBuilder("{\"range\":{\"modifiedAt\":{");
                boolean first=true; if(req.getStartDate()!=null){ f.append("\"gte\":"+req.getStartDate()); first=false; }
                if(req.getEndDate()!=null){ if(!first) f.append(","); f.append("\"lte\":"+req.getEndDate()); }
                f.append("}}}"); filters.add(f.toString());
            }
            if(!filters.isEmpty()){ if(hasPrev) json.append(","); json.append("\"filter\":["); for(int i=0;i<filters.size();i++){ if(i>0) json.append(","); json.append(filters.get(i)); } json.append("]"); }
            json.append("}");
            json.append("}");

            StringQuery query = new StringQuery(json.toString());
            query.setPageable(PageRequest.of(0, Math.max(1, limit)));

            // 首先检查索引是否存在
            if (!operations.indexOps(IndexCoordinates.of(esIndexName)).exists()) {
                return Result.failure("ES index does not exist");
            }
            SearchHits<EsDocument> hits = operations.search(query, EsDocument.class, IndexCoordinates.of(esIndexName));
            List<SearchResult> out = new ArrayList<>();
            for (SearchHit<EsDocument> hit : hits) {
                EsDocument d = hit.getContent();
                String snippet = null;
                java.util.Map<String, java.util.List<String>> hls = hit.getHighlightFields();
                if (hls != null && !hls.isEmpty()) {
                    // 优先使用高亮的content
                    if (hls.containsKey("content") && hls.get("content") != null && !hls.get("content").isEmpty()) {
                        snippet = hls.get("content").get(0);
                    } else if (hls.containsKey("title") && hls.get("title") != null && !hls.get("title").isEmpty()) {
                        snippet = hls.get("title").get(0);
                    } else if (hls.containsKey("fileName") && hls.get("fileName") != null && !hls.get("fileName").isEmpty()) {
                        snippet = hls.get("fileName").get(0);
                    }
                } else if (q != null && !q.isEmpty() && d.getContent() != null) {
                    // 如果没有高亮字段，尝试从content中截取包含关键字的片段并添加高亮标签
                    String content = d.getContent();
                    String lowerContent = content.toLowerCase(Locale.ROOT);
                    String lowerQuery = q.toLowerCase(Locale.ROOT);
                    int pos = lowerContent.indexOf(lowerQuery);
                    if (pos >= 0) {
                        int st = Math.max(0, pos - 60);
                        int ed = Math.min(content.length(), pos + q.length() + 60);
                        // 添加高亮标签
                        String snippetBefore = content.substring(st, pos);
                        String matchedText = content.substring(pos, pos + q.length());
                        String snippetAfter = content.substring(pos + q.length(), ed);
                        snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                    }
                }
                // 规范化片段格式
                if (snippet != null) {
                    snippet = snippet.replaceAll("\n", " ").replaceAll("\\s+", " ");
                }
                // 确保只有真正匹配关键词的文档才会被返回
                if (q.isEmpty() || (d.getContent() != null && d.getContent().contains(q)) || 
                    (d.getTitle() != null && d.getTitle().contains(q)) || 
                    (d.getFileName() != null && d.getFileName().contains(q))) {
                    out.add(new SearchResult(d.getFilePath(), d.getFileName(), d.getParentDir(), 
                        com.documentpreview.modules.document.domain.FileType.fromExtension(
                        d.getFileType() == null ? null : d.getFileType().toLowerCase(Locale.ROOT)), 
                        0, snippet));
                }
            }
            return Result.success(out);
        } catch (Exception e) {
            String q = (req.getKeyword() == null ? "" : req.getKeyword()).trim();
            StringBuilder boolJson = new StringBuilder();
            boolJson.append("{");
            boolJson.append("\"bool\":{");
            boolean hasPrev = false;
            if (!q.isEmpty()) {
                boolJson.append("\"must\":[{\"multi_match\":{\"query\":\"")
                    .append(q.replace("\"","\\\""))
                    .append("\",\"fields\":[\"fileName^3\",\"title^2\",\"content\"]}}]");
                hasPrev = true;
            }
            java.util.List<String> filters = new java.util.ArrayList<>();
            if (req.getFileTypes() != null && !req.getFileTypes().isEmpty()) {
                StringBuilder f = new StringBuilder();
                f.append("{\"terms\":{\"fileType\":[");
                for (int i=0;i<req.getFileTypes().size();i++){ if(i>0) f.append(","); f.append("\"").append(req.getFileTypes().get(i)).append("\""); }
                f.append("]}}");
                filters.add(f.toString());
            }
            if (req.getParentDir() != null && !req.getParentDir().isBlank()) {
                filters.add("{\"prefix\":{\"parentDir\":\"" + req.getParentDir().replace("\"","\\\"") + "\"}}");
            }
            if (req.getMinSize() != null || req.getMaxSize() != null) {
                StringBuilder f = new StringBuilder("{\"range\":{\"size\":{");
                boolean first=true; if(req.getMinSize()!=null){ f.append("\"gte\":"+req.getMinSize()); first=false; }
                if(req.getMaxSize()!=null){ if(!first) f.append(","); f.append("\"lte\":"+req.getMaxSize()); }
                f.append("}}}"); filters.add(f.toString());
            }
            if (req.getStartDate() != null || req.getEndDate() != null) {
                StringBuilder f = new StringBuilder("{\"range\":{\"modifiedAt\":{");
                boolean first=true; if(req.getStartDate()!=null){ f.append("\"gte\":"+req.getStartDate()); first=false; }
                if(req.getEndDate()!=null){ if(!first) f.append(","); f.append("\"lte\":"+req.getEndDate()); }
                f.append("}}}"); filters.add(f.toString());
            }
            if(!filters.isEmpty()){ if(hasPrev) boolJson.append(","); boolJson.append("\"filter\":["); for(int i=0;i<filters.size();i++){ if(i>0) boolJson.append(","); boolJson.append(filters.get(i)); } boolJson.append("]"); }
            boolJson.append("}");
            boolJson.append("}");

            StringBuilder body = new StringBuilder();
            body.append("{");
            body.append("\"query\":").append(boolJson);
            body.append(",\"highlight\":{\"fields\":{\"content\":{},\"title\":{},\"fileName\":{}},\"pre_tags\":[\"<em class='highlight'>\"],\"post_tags\":[\"</em>\"]}");
            body.append(",\"size\":").append(Math.max(1, limit));
            body.append("}");

            try {
                String base = (esUris == null ? "" : esUris).split(",")[0].trim();
                if (base.isEmpty()) {
                    return Result.failure("ES search failed: " + e.getMessage());
                }
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest reqHttp = HttpRequest.newBuilder()
                        .uri(URI.create(base + "/" + esIndexName + "/_search"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();
                HttpResponse<String> resp = client.send(reqHttp, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(resp.body());
                    JsonNode hitsArr = root.path("hits").path("hits");
                    List<SearchResult> out = new ArrayList<>();
                    for (int i = 0; i < hitsArr.size(); i++) {
                        JsonNode h = hitsArr.get(i);
                        JsonNode src = h.path("_source");
                        String filePath = src.path("filePath").asText(null);
                        String fileName = src.path("fileName").asText(null);
                        String parentDir = src.path("parentDir").asText(null);
                        String fileType = src.path("fileType").asText(null);
                        String snippet = null;
                        JsonNode hl = h.path("highlight");
                        if (hl.isObject()) {
                            // 优先使用高亮的content
                            if (hl.path("content").isArray() && hl.path("content").size() > 0) {
                                snippet = hl.path("content").get(0).asText();
                            } else if (hl.path("title").isArray() && hl.path("title").size() > 0) {
                                snippet = hl.path("title").get(0).asText();
                            } else if (hl.path("fileName").isArray() && hl.path("fileName").size() > 0) {
                                snippet = hl.path("fileName").get(0).asText();
                            }
                        } else if (!q.isEmpty()) {
                            // 如果没有高亮字段，尝试从content中截取包含关键字的片段并添加高亮标签
                            String content = src.path("content").asText("");
                            String lowerContent = content.toLowerCase(Locale.ROOT);
                            String lowerQuery = q.toLowerCase(Locale.ROOT);
                            int pos = lowerContent.indexOf(lowerQuery);
                            if (pos >= 0) {
                                int start = Math.max(0, pos - 60);
                                int end = Math.min(content.length(), pos + q.length() + 60);
                                // 添加高亮标签
                                String snippetBefore = content.substring(start, pos);
                                String matchedText = content.substring(pos, pos + q.length());
                                String snippetAfter = content.substring(pos + q.length(), end);
                                snippet = snippetBefore + "<em class='highlight'>" + matchedText + "</em>" + snippetAfter;
                            }
                        }
                        // 规范化片段格式
                        if (snippet != null) {
                            snippet = snippet.replaceAll("\n", " ").replaceAll("\\s+", " ");
                        }
                        // 确保只有真正匹配关键词的文档才会被返回
                        String content = src.path("content").asText("");
                        String title = src.path("title").asText("");
                        if (q.isEmpty() || content.contains(q) || title.contains(q) || fileName.contains(q)) {
                            out.add(new SearchResult(filePath, fileName, parentDir, 
                                com.documentpreview.modules.document.domain.FileType.fromExtension(
                                fileType == null ? null : fileType.toLowerCase(Locale.ROOT)), 
                                0, snippet));
                        }
                    }
                    return Result.success(out);
                } else {
                    // 如果是404错误，可能是索引不存在，返回明确的错误信息
                    if (resp.statusCode() == 404) {
                        return Result.failure("ES search failed: index not found (http 404)");
                    }
                    return Result.failure("ES search failed: http " + resp.statusCode());
                }
            } catch (Exception ex) {
                return Result.failure("ES search failed: " + e.getMessage());
            }
        }
    }
}
