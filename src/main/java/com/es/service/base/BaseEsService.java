package com.es.service.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.es.config.ElasticsearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

/**
 * @author zetu
 * @date 2021/4/1
 */
@Slf4j
public abstract class BaseEsService {

    @Autowired
    @Qualifier(value = "restHighLevelClient")
    protected RestHighLevelClient client;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    protected static final RequestOptions COMMON_OPTIONS;
    protected static final Integer BATCH_NUMBER = 99;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        // 默认缓冲限制为100MB，此处修改为50MB。
        builder.setHttpAsyncResponseConsumerFactory(new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(50 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    /**
     * check index if exists
     *
     * @param index elasticsearch index name
     * @return exists
     */
    protected boolean checkIndexExistsRequest(String index) {
        try {
            GetIndexRequest indexRequest = new GetIndexRequest(index);
            return client.indices().exists(indexRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to check index. {}", index, e);
            throw new ElasticsearchException("检查索引 {" + index + "} 是否存在失败");
        }
    }

    /**
     * create elasticsearch index (asyc)
     *
     * @param index elasticsearch index
     */
    protected void createIndexRequest(String index) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);
            // Settings for this index
            request.settings(Settings.builder()
                    .put("index.number_of_shards", elasticsearchProperties.getIndex().getNumberOfShards())
                    .put("index.number_of_replicas", elasticsearchProperties.getIndex().getNumberOfReplicas()));

            CreateIndexResponse createIndexResponse = client.indices().create(request, COMMON_OPTIONS);

            log.info(" whether all of the nodes have acknowledged the request : {}", createIndexResponse.isAcknowledged());
            log.info(" Indicates whether the requisite number of shard copies were started for each shard in the index before timing out :{}", createIndexResponse.isShardsAcknowledged());
        } catch (IOException e) {
            log.error("failed to create index. ", e);
            throw new ElasticsearchException("创建索引 {" + index + "} 失败");
        }
    }

    /**
     * delete elasticsearch index
     *
     * @param index elasticsearch index name
     */
    protected void deleteIndexRequest(String index) {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
        try {
            client.indices().delete(deleteIndexRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("failed to delete index. ", e);
            throw new ElasticsearchException("删除索引 {" + index + "} 失败");
        }
    }

    /**
     * build IndexRequest
     *
     * @param index  elasticsearch index name
     * @param id     request object id
     * @param object request object
     * @return {@link IndexRequest}
     */
    protected static IndexRequest buildIndexRequest(String index, String id, Object object) {
        return new IndexRequest(index).id(id).source(JSONUtil.toJsonStr(object), XContentType.JSON);
    }

    /**
     * build IndexRequest
     *
     * @param index  elasticsearch index name
     * @param id     request object id
     * @param object request object
     * @return {@link UpdateRequest}
     */
    protected static UpdateRequest buildUpdateRequest(String index, String id, Object object) {
        return new UpdateRequest(index, id).doc(JSONUtil.toJsonStr(object), XContentType.JSON);
    }

    /**
     * exec updateRequest
     *
     * @param index  elasticsearch index name
     * @param id     Document id
     * @param object request object
     * @return {@link UpdateResponse} 更新结果
     */
    protected UpdateResponse updateRequest(String index, String id, Object object) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(index, id).doc(BeanUtil.beanToMap(object), XContentType.JSON);
            return client.update(updateRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            throw new ElasticsearchException("更新索引 {" + index + "} 数据 {" + object + "} 失败");
        }
    }

    /**
     * exec deleteRequest
     *
     * @param index elasticsearch index name
     * @param id    Document id
     */
    protected DeleteResponse deleteRequest(String index, String id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            return client.delete(deleteRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            throw new ElasticsearchException("删除索引 {" + index + "} 数据id {" + id + "} 失败");
        }
    }

    /**
     * search all
     *
     * @param index elasticsearch index name
     * @return {@link SearchResponse}
     */
    protected SearchResponse search(String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 可以先按 age 进行倒序排序
        searchSourceBuilder.sort("age", SortOrder.DESC);
        // 再按 id 正序排序
        searchSourceBuilder.sort("id", SortOrder.ASC);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to search all data.", e);
        }
        return searchResponse;
    }

    /**
     * search by id
     *
     * @param index elasticsearch index name
     * @param id    id of doc
     * @return {@link SearchResponse}
     */
    protected SearchResponse searchById(String index, String id) {
        SearchRequest searchRequest = new SearchRequest(index);
        // 根据 ID 查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id", id));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to search one doc by id : {}, index: {}.", id, index, e);
        }
        return searchResponse;
    }

    /**
     * scroll search
     *
     * @param searchRequest search request with params
     * @param page          the page number
     * @param pageSize      the size of per-page
     * @return {@link SearchResponse}
     * @throws IOException search exception
     */
    protected SearchResponse esScrollSearch(SearchRequest searchRequest, Integer page, Integer pageSize) throws IOException {

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest.scroll(scroll);
        SearchResponse searchResponse = client.search(searchRequest, COMMON_OPTIONS);

        String scrollId = searchResponse.getScrollId();
        SearchHits searchHits = searchResponse.getHits();
        long totalHitCount = searchHits.getTotalHits().value;

        long maxPage = (totalHitCount % pageSize == 0) ? (totalHitCount / pageSize) : (totalHitCount / pageSize + 1);

        int scrollCount = maxPage >= page ? page : (int) maxPage;
        if (page > 1) {
            for (int i = 1; i < scrollCount; i++) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);

                searchResponse = client.scroll(scrollRequest, COMMON_OPTIONS);
                scrollId = searchResponse.getScrollId();
            }
        }

        return searchResponse;
    }

    /**
     * 获取分批的批次数量
     *
     * @param totalSize 列表总大小
     * @return 分批数量
     */
    protected int getBatchCount(int totalSize) {
        int quotient = totalSize / BATCH_NUMBER;
        int remainder = totalSize % BATCH_NUMBER;
        int batchCount = quotient;
        if (remainder > 0) {
            batchCount += 1;
        }
        return batchCount;
    }
}
