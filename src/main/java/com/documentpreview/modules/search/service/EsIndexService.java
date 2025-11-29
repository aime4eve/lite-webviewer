package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.es.EsDocumentRepository;
import com.documentpreview.shared.ddd.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(value = "app.search.use-es", havingValue = "true")
@Service
public class EsIndexService {
    private final EsDocumentRepository repo;
    private final com.documentpreview.modules.search.repository.SearchMetaRepository metaRepo;

    @Value("${app.search.use-es:true}")
    private boolean useEs;

    public EsIndexService(EsDocumentRepository repo, com.documentpreview.modules.search.repository.SearchMetaRepository metaRepo) {
        this.repo = repo;
        this.metaRepo = metaRepo;
    }

    public boolean enabled() { return true; }

    public Result<Long> reindexFromMeta(boolean clear) {
        try {
            // proceed when bean is active
            if (clear) {
                repo.deleteAll();
            }
            List<SearchMeta> metas = metaRepo.loadAll().getValue().orElse(List.of());
            List<EsDocument> docs = new ArrayList<>();
            for (SearchMeta m : metas) {
                EsDocument d = new EsDocument();
                d.setFilePath(m.getFilePath());
                d.setFileName(m.getFileName());
                d.setParentDir(m.getParentDir());
                d.setFileType(m.getFileType() == null ? null : m.getFileType().name());
                d.setTitle(m.getTitle());
                d.setContent(m.getContentText());
                d.setSize(m.getSize());
                d.setModifiedAt(m.getModifiedAt());
                docs.add(d);
            }
            repo.saveAll(docs);
            return Result.success((long) docs.size());
        } catch (Exception e) {
            return Result.failure("Reindex failed: " + e.getMessage());
        }
    }
}
