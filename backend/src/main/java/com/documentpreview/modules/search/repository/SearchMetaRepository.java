package com.documentpreview.modules.search.repository;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.shared.ddd.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SearchMetaRepository {
    private static final Logger logger = LoggerFactory.getLogger(SearchMetaRepository.class);

    @Value("${app.data.dir}")
    private String dataDir;

    private static final String SEARCH_META_JSON = "search.meta.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchMetaRepository() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Result<List<SearchMeta>> saveAll(List<SearchMeta> list) {
        try {
            Path dir = Paths.get(dataDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path jsonPath = dir.resolve(SEARCH_META_JSON);
            objectMapper.writeValue(jsonPath.toFile(), list);
            return Result.success(list);
        } catch (Exception e) {
            logger.error("Failed to save search meta: {}", e.getMessage(), e);
            return Result.failure("Failed to save search meta: " + e.getMessage());
        }
    }

    public Result<List<SearchMeta>> loadAll() {
        try {
            Path jsonPath = Paths.get(dataDir, SEARCH_META_JSON);
            if (!Files.exists(jsonPath)) return Result.success(new ArrayList<>());
            SearchMeta[] arr = objectMapper.readValue(jsonPath.toFile(), SearchMeta[].class);
            java.util.List<SearchMeta> list = new java.util.ArrayList<>();
            if (arr != null) {
                java.util.Collections.addAll(list, arr);
            }
            return Result.success(list);
        } catch (Exception e) {
            logger.error("Failed to load search meta: {}", e.getMessage(), e);
            return Result.failure("Failed to load search meta: " + e.getMessage());
        }
    }
}
