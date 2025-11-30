package com.documentpreview.modules.search.es;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的文档仓库实现，不再依赖Elasticsearch
 * 当Elasticsearch被禁用时，使用内存存储作为替代方案
 */
@Repository
public class EsDocumentRepository {
    
    // 使用内存存储作为替代方案
    private final ConcurrentHashMap<String, EsDocument> documentStore = new ConcurrentHashMap<>();
    
    /**
     * 保存文档
     */
    public EsDocument save(EsDocument document) {
        documentStore.put(document.getId(), document);
        return document;
    }
    
    /**
     * 批量保存文档
     */
    public Iterable<EsDocument> saveAll(Iterable<EsDocument> documents) {
        List<EsDocument> result = new ArrayList<>();
        for (EsDocument doc : documents) {
            result.add(save(doc));
        }
        return result;
    }
    
    /**
     * 根据ID查找文档
     */
    public Optional<EsDocument> findById(String id) {
        return Optional.ofNullable(documentStore.get(id));
    }
    
    /**
     * 查找所有文档
     */
    public Iterable<EsDocument> findAll() {
        return new ArrayList<>(documentStore.values());
    }
    
    /**
     * 根据ID列表查找文档
     */
    public Iterable<EsDocument> findAllById(Iterable<String> ids) {
        List<EsDocument> result = new ArrayList<>();
        for (String id : ids) {
            EsDocument doc = documentStore.get(id);
            if (doc != null) {
                result.add(doc);
            }
        }
        return result;
    }
    
    /**
     * 统计文档数量
     */
    public long count() {
        return documentStore.size();
    }
    
    /**
     * 根据ID删除文档
     */
    public void deleteById(String id) {
        documentStore.remove(id);
    }
    
    /**
     * 删除指定文档
     */
    public void delete(EsDocument document) {
        documentStore.remove(document.getId());
    }
    
    /**
     * 根据ID列表删除文档
     */
    public void deleteAllById(Iterable<? extends String> ids) {
        for (String id : ids) {
            documentStore.remove(id);
        }
    }
    
    /**
     * 删除所有文档
     */
    public void deleteAll() {
        documentStore.clear();
    }
    
    /**
     * 根据ID判断文档是否存在
     */
    public boolean existsById(String id) {
        return documentStore.containsKey(id);
    }
}