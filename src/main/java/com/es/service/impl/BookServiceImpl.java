package com.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.es.common.constants.EsConstant;
import com.es.dto.book.ModifyBookReq;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.model.book.Book;
import com.es.service.BookService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Service
@Slf4j
public class BookServiceImpl extends BaseEsService implements BookService {

    @Override
    public Book getById(Long id) {
        GetRequest getRequest = new GetRequest(EsConstant.BOOK_INDEX_NAME).id(String.valueOf(id));
        try {
            GetResponse response = client.get(getRequest, COMMON_OPTIONS);
            Map<String, Object> sourceAsMap = response.getSourceAsMap();
            return BeanUtil.mapToBean(sourceAsMap, Book.class, false, new CopyOptions());
        } catch (IOException e) {
            log.error("Failed to get doc of id: {}", id, e);
            return null;
        }
    }

    @Override
    public Boolean add(ModifyBookReq modifyReq) {
        // TODO
        return null;
    }

    @Override
    public Boolean update(ModifyBookReq modifyReq) {
        // TODO
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        // 根据ID删除数据
        DeleteResponse response = deleteRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(id));
        return response.status().equals(RestStatus.OK);
    }

    @Override
    public SearchBookRes searchBook(SearchBookReq searchReq) {
        BoolQueryBuilder boolQuery = buildBookBoolQuery(searchReq);

        SearchRequest searchRequest = new SearchRequest(EsConstant.BOOK_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);

        if (Objects.nonNull(searchReq.getSortField())) {
            // 解析排序参数
            SortOrder sortOrder = SortOrder.ASC;
            if (EsConstant.DESC.equals(searchReq.getSort().name())) {
                sortOrder = SortOrder.DESC;
            }
            // 先按参数进行倒序排序
            searchSourceBuilder.sort(searchReq.getSortField().getField(), sortOrder);
        }
        // 默认按 id 正序排序
        searchSourceBuilder.sort("id", SortOrder.ASC);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to search data. param: {}", JSONUtil.toJsonStr(searchReq), e);
        }

        return convertToSearchRes(searchResponse);
    }

    /**
     * 转换搜索结果
     *
     * @param searchResponse
     * @return {@link SearchBookRes}
     * @author 泽兔
     * @date 2021/7/21 15:36
     */
    private SearchBookRes convertToSearchRes(SearchResponse searchResponse) {
        SearchBookRes searchRes = new SearchBookRes();
        searchRes.setBookList(Collections.emptyList());
        searchRes.setTotal(0L);
        if (searchResponse == null) {
            return searchRes;
        }
        List<Book> bookList = new ArrayList<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        // 将搜索结果转换为响应结果对象
        Arrays.stream(hits).forEach(hit -> {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Book book = BeanUtil.mapToBean(sourceAsMap, Book.class, false, new CopyOptions());
            if (Objects.nonNull(book.getId())) {
                bookList.add(book);
            }
        });
        searchRes.setBookList(bookList);
        searchRes.setTotal(searchRes.getTotal());

        return searchRes;
    }

    /**
     * 构建 ES 聚合查询条件
     *
     * @param searchReq 查询参数
     * @return {@link BoolQueryBuilder} 聚合查询条件
     */
    private BoolQueryBuilder buildBookBoolQuery(SearchBookReq searchReq) {
        // TODO
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


        return boolQuery;
    }
}
