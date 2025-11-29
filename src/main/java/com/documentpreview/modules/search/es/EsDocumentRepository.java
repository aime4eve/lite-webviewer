package com.documentpreview.modules.search.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, String> {
}
