package com.documentpreview.modules.search.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch文档仓库接口
 * 基于Spring Data Elasticsearch提供文档的CRUD操作
 */
@Repository
public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, String> {
    
    /**
     * 根据文件路径查询文档
     */
    List<EsDocument> findByFilePath(String filePath);
    
    /**
     * 根据文件类型查询文档
     */
    List<EsDocument> findByFileType(String fileType);
    
    /**
     * 根据父目录查询文档
     */
    List<EsDocument> findByParentDir(String parentDir);
    
    /**
     * 根据文件路径删除文档
     */
    void deleteByFilePath(String filePath);
    
    /**
     * 根据父目录删除文档
     */
    void deleteByParentDir(String parentDir);
    
    /**
     * 查询是否存在指定文件路径的文档
     */
    boolean existsByFilePath(String filePath);
}