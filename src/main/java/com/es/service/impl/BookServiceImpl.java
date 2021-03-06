package com.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.es.common.constants.EsConstant;
import com.es.dto.book.ModifyBookReq;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.dto.book.UpdatePricesReq;
import com.es.model.book.Book;
import com.es.model.book.CategoryGroup;
import com.es.service.BookService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Service
@Slf4j
public class BookServiceImpl extends BaseEsService implements BookService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

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
        IndexRequest request = buildIndexRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(modifyReq.getId()), modifyReq);
        try {
            client.index(request, COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            log.error("Failed to insert.", e);
        }
        return false;
    }

    @Override
    public Boolean addList(List<ModifyBookReq> list) {
        insertList(list);
        return true;
    }

    private void insertList(List<ModifyBookReq> list) {
        BulkRequest bulkRequest = new BulkRequest();
        String index = EsConstant.BOOK_INDEX_NAME;
        list.forEach(book -> {
            IndexRequest request = buildIndexRequest(index, String.valueOf(book.getId()), book);
            UpdateRequest updateRequest = buildUpdateRequest(index, String.valueOf(book.getId()), book).upsert(request);

            bulkRequest.add(updateRequest);
        });
        try {
            client.bulkAsync(bulkRequest, COMMON_OPTIONS, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    log.info("success to insert a book bulk data, size : {}", bulkRequest.numberOfActions());
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("failed to insert a book bulk data", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to insert batch", e);
        }
    }

    @Override
    public Boolean update(Long bookId, ModifyBookReq modifyReq) {
        UpdateRequest request = buildUpdateRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(bookId), modifyReq);
        try {
            client.update(request, COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            log.error("Failed to insert.", e);
        }
        return false;
    }

    @Override
    public Boolean updateByQuery(String oldBookName, String newBookName) {
        // ?????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(EsConstant.BOOK_INDEX_NAME);
        // ???????????????????????????
        updateRequest.setConflicts("proceed");
        updateRequest.setRefresh(true);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // ????????????????????????????????????????????????????????????
        boolQuery.must(QueryBuilders.matchPhrasePrefixQuery("bookName", oldBookName));
        updateRequest.setQuery(boolQuery);
        // ??????update???????????????
        updateRequest.setRefresh(true);
        // ???????????????????????????
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????ID????????????????????????
        updateRequest.setBatchSize(1000);

        Map<String, Object> params = new HashMap<>(2);
        params.put("bookName", newBookName);
        // ??????????????????
        // ??????????????????
        updateRequest.setMaxRetries(20);
        // ????????????/???????????????????????????
        updateRequest.setRequestsPerSecond(1000);
        // ??????????????????????????????
        updateRequest.setSlices(3);

        // ???????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????params??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? params ????????????????????????????????????????????????????????? "Too many dynamic script compilations within" ?????????
        updateRequest.setScript(new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG,
                "ctx._source.bookName = params.bookName;", params));
        try {
            BulkByScrollResponse updateByQueryResponse = client.updateByQuery(updateRequest, COMMON_OPTIONS);
            return CollectionUtils.isEmpty(updateByQueryResponse.getBulkFailures());
        } catch (IOException e) {
            log.error("????????????????????????????????????", e);
        }
        return false;
    }

    @Override
    public Boolean delete(Long id) {
        // ??????ID????????????
        DeleteResponse response = deleteRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(id));
        return response.status().equals(RestStatus.OK);
    }

    @Override
    public SearchBookRes searchBook(SearchBookReq searchReq) {
        BoolQueryBuilder boolQuery = buildBookBoolQuery(searchReq);

        SearchRequest searchRequest = new SearchRequest(EsConstant.BOOK_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);
        String[] includes = {"id", "bookName", "introduction"};
        String[] excludes = {};
        searchSourceBuilder.fetchSource(includes, excludes);

        // ????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("introduction");
        highlightBuilder.field("bookName");
        highlightBuilder.boundaryScannerLocale("zh_CN");
        searchSourceBuilder.highlighter(highlightBuilder);

        // sortField ???????????????sort ????????????
        if (Objects.nonNull(searchReq.getSortField())) {
            // ??????????????????
            SortOrder sortOrder = SortOrder.ASC;
            if (EsConstant.DESC.equals(searchReq.getSort().name())) {
                sortOrder = SortOrder.DESC;
            }
            // ??????????????????????????????
            searchSourceBuilder.sort(searchReq.getSortField().getField(), sortOrder);
        }
        // ????????? id ????????????
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

    @Override
    public List<CategoryGroup> categoryGroup() {
        SearchResponse groupSearchResponse = searchGroupData();
        if (Objects.isNull(groupSearchResponse)) {
            return Collections.emptyList();
        }
        Aggregations aggregations = groupSearchResponse.getAggregations();
        List<CategoryGroup> groupList = new ArrayList<>();
        if (Objects.nonNull(aggregations)) {
            // ????????????????????????
            ParsedLongTerms group = aggregations.get("group");
            // ??????????????? aggregation
            ParsedCardinality totalCount = aggregations.get("total");
            // ???????????????????????????????????????????????????
            log.info("????????????????????????{}", totalCount);
            // ??????????????????
            for (Terms.Bucket bucket : group.getBuckets()) {
                // ?????????????????????top1??????
                ParsedTopHits topHits = bucket.getAggregations().get("price_top_1");
                SearchHits groupHits = topHits.getHits();
                SearchHit[] hits = groupHits.getHits();
                List<Book> books = new ArrayList<>();
                Arrays.stream(hits).forEach(hit -> {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    Book book = BeanUtil.mapToBean(sourceAsMap, Book.class, false, new CopyOptions());
                    if (Objects.nonNull(book.getId())) {
                        books.add(book);
                    }
                });
                CategoryGroup categoryGroup = new CategoryGroup();
                // ??????????????????ID
                categoryGroup.setCategoryId(Integer.valueOf(bucket.getKeyAsString()));
                categoryGroup.setBooks(books);
                groupList.add(categoryGroup);
            }
        }

        return groupList;
    }

    @Override
    public Boolean updatePrices(UpdatePricesReq updateReq) {
        // update bulk???????????????
        List<Long> bookIds = updateReq.getBookIds();
        Double price = updateReq.getPrice();
        // ??????????????????
        BulkRequest bulkRequest = new BulkRequest();
        Map<String, Object> map;
        for (Long bookId : bookIds) {
            map = new HashMap<>(2);
            map.put("price", price);
            UpdateRequest updateRequest = new UpdateRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(bookId))
                    .doc(map, XContentType.JSON);
            // ?????????????????? bulk ?????????
            bulkRequest.add(updateRequest);
        }
        try {
            // ??????????????????
            BulkResponse bulkResponse = client.bulk(bulkRequest, COMMON_OPTIONS);
            // ????????????????????????????????????????????????
            return !bulkResponse.hasFailures();
        } catch (Exception e) {
            log.error("?????????????????????????????????{}", JSONUtil.toJsonStr(updateReq), e);
            return false;
        }
    }

    /**
     * ????????????????????????
     *
     * @return {@link SearchResponse}
     */
    private SearchResponse searchGroupData() {
        // ??????????????????
        SearchRequest searchRequest = new SearchRequest(EsConstant.BOOK_INDEX_NAME);

        AggregationBuilder groupAggregation = AggregationBuilders
                // ??????????????????????????????????????????
                .terms("group")
                // ??????????????????????????????????????????10
                .size(10)
                // ??????????????????
                .field("categoryId");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ???????????????hits????????????????????? size ??? 0
        searchSourceBuilder.size(0);

        // TOP hits ???????????? top1
        // ?????????????????????
        String[] includes = {"id", "bookName", "categoryId", "categoryName", "price"};
        String[] excludes = {};
        AggregationBuilder topHits = AggregationBuilders
                // ????????????topHits??????
                .topHits("price_top_1")
                // ??????????????????
                .sort(SortBuilders.fieldSort("price").order(SortOrder.DESC))
                .fetchSource(includes, excludes)
                // ??????1???
                .size(1);
        // ??????????????????????????????topHits ????????????????????????
        groupAggregation.subAggregation(topHits);

        // ????????????????????????
        CardinalityAggregationBuilder totalAggregation = AggregationBuilders.cardinality("total")
                .field("categoryId");
        // ?????????????????????????????????
        searchSourceBuilder.aggregation(groupAggregation);
        searchSourceBuilder.aggregation(totalAggregation);

        searchRequest.source(searchSourceBuilder);
        try {
            return client.search(searchRequest, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to aggregations search data", e);
        }
        return null;
    }

    /**
     * ??????????????????
     *
     * @param searchResponse
     * @return {@link SearchBookRes}
     * @author ??????
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
        // ??????????????????????????????????????????
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
     * ?????? ES ??????????????????
     *
     * @param searchReq ????????????
     * @return {@link BoolQueryBuilder} ??????????????????
     */
    private BoolQueryBuilder buildBookBoolQuery(SearchBookReq searchReq) {
        // ??????????????????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // isbnNo ????????????????????????????????????
        if (StringUtils.isNotBlank(searchReq.getIsbnNo())) {
            TermQueryBuilder isbnNoQuery = QueryBuilders.termQuery("isbnNo", searchReq.getIsbnNo());
            // ?????? must ??????
            boolQuery.must(isbnNoQuery);
        }

        // bookName ???????????????????????????
        if (StringUtils.isNotBlank(searchReq.getBookName())) {
            // ?????????????????????????????????
            BoolQueryBuilder bookNameQuery = QueryBuilders.boolQuery();
            bookNameQuery.should(QueryBuilders.matchQuery("bookName", searchReq.getBookName()));
            bookNameQuery.should(QueryBuilders.matchPhraseQuery("bookName", searchReq.getBookName()));
            // ????????????????????????????????? sql ??? like '%%' ????????????????????????????????????
//            bookNameQuery.should(QueryBuilders.wildcardQuery("bookName.keyword", "*" + searchReq.getBookName() + "*"));
            // ?????? must ??????
            boolQuery.must(bookNameQuery);
        }

        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // bookAuthorLastName ?????????????????????bookAuthorFirstName ???????????????????????????????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(searchReq.getBookAuthorLastName()) && StringUtils.isNotBlank(searchReq.getBookAuthorFirstName())) {
            // ???????????????????????????????????? nested ???????????????????????????
            BoolQueryBuilder nameQuery = QueryBuilders.boolQuery();
            nameQuery.must(QueryBuilders.termQuery("authors.lastName", searchReq.getBookAuthorLastName()
                    .toLowerCase(Locale.ROOT)));
            nameQuery.must(QueryBuilders.termQuery("authors.firstName", searchReq.getBookAuthorFirstName()
                    .toLowerCase(Locale.ROOT)));

            NestedQueryBuilder authorNameQuery = QueryBuilders.nestedQuery("authors", nameQuery, ScoreMode.Max);
            // ?????? must ??????
            boolQuery.must(authorNameQuery);
        }

        // tags ????????????????????????????????????
        if (CollectionUtils.isNotEmpty(searchReq.getTags())) {
            // ???????????????????????????????????????
            TermsQueryBuilder tagsQuery = QueryBuilders.termsQuery("tags", searchReq.getTags());
            // ?????? must ??????
            boolQuery.must(tagsQuery);
        }

        // introduction ????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(searchReq.getIntroduction())) {
            // TODO
        }

        // categoryId ?????????????????? categoryChain ?????????
        if (searchReq.getCategoryId() != null) {
            TermQueryBuilder categoryQuery = QueryBuilders.termQuery("categoryChain.id", searchReq.getCategoryId());
            // ?????? must ????????????
            boolQuery.must(categoryQuery);
        }

        // publishTimeStart ???????????????????????????publishTimeEnd ?????????????????????????????????????????????
        if (StringUtils.isNotBlank(searchReq.getPublishTimeStart()) && StringUtils.isNotBlank(searchReq.getPublishTimeEnd())) {
            RangeQueryBuilder publishTimeQuery = QueryBuilders.rangeQuery("publishTime");
            // from ????????????
            publishTimeQuery.gte(searchReq.getPublishTimeStart());
            // to ????????????
            try {
                publishTimeQuery.lte(DateFormatUtils.format(
                        DateUtils.addDays(DateUtils.parseDate(searchReq.getPublishTimeEnd(), DATE_PATTERN), 1),
                        DATE_PATTERN));
            } catch (ParseException e) {
                log.error("failed to parse string to date, {}.", searchReq.getPublishTimeEnd(), e);
            }
            // ?????? must ????????????
            boolQuery.must(publishTimeQuery);
        }

        // pressId ?????????ID???????????????
        if (StringUtils.isNotBlank(searchReq.getPressId())) {
            TermQueryBuilder pressQuery = QueryBuilders.termQuery("press.pressId", searchReq.getPressId());
            // ?????? must ????????????
            boolQuery.must(pressQuery);
        }

        // priceStart ?????????????????????priceEnd ???????????????????????????????????????
        if (searchReq.getPriceStart() != null) {
            RangeQueryBuilder priceStartQuery = QueryBuilders.rangeQuery("price");
            // from ????????????
            priceStartQuery.gte(searchReq.getPriceStart());
            // ?????? must ????????????
            boolQuery.must(priceStartQuery);
        }
        if (searchReq.getPriceEnd() != null) {
            RangeQueryBuilder priceEndQuery = QueryBuilders.rangeQuery("price");
            // to ????????????
            priceEndQuery.lte(searchReq.getPriceEnd());
            // ?????? must ????????????
            boolQuery.must(priceEndQuery);
        }

        return boolQuery;
    }
}
