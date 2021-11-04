package com.es.service.impl;

import com.es.common.constants.EsConstant;
import com.es.service.EsService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.springframework.stereotype.Service;

/**
 * @author zetu
 * @date 2021/7/21
 */
@Service
@Slf4j
public class EsServiceImpl extends BaseEsService implements EsService {

    @Override
    public boolean checkIndexExists(String index) {
        return checkIndexExistsRequest(index);
    }

    @Override
    public void createIndex(String index) {
        createIndexRequest(index);
    }

    @Override
    public void deleteIndex(String index) {
        deleteIndexRequest(index);
    }

    @Override
    public void reindex(String sourceIndex, String targetIndex) {
        // 如果目标索引不存在，创建新的索引
        if (!checkIndexExists(targetIndex)) {
            createIndexRequest(targetIndex);
        }
        // 把已有的数据复制到新的索引
        // 更新新的索引的数据
        ReindexRequest request = new ReindexRequest();
        request.setSourceIndices(sourceIndex);
        request.setDestIndex(targetIndex);
        // 设置版本冲突时继续
        request.setConflicts("proceed");
        // 调用reindex后刷新索引
        request.setRefresh(true);

        ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkResponse) {
                log.info("reindex success. {}", bulkResponse.getTotal());
            }

            @Override
            public void onFailure(Exception e) {
                log.error("reindex failed. ", e);
            }
        };
        // 异步reindex
        client.reindexAsync(request, COMMON_OPTIONS, listener);
    }
}
