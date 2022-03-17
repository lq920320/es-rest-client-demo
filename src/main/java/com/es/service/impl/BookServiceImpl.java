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
        // 这里的测试我们可以考虑以下场景
        // 考虑到书名某个字输入错误，现在要订正书名，那么逻辑就是将书名是旧书名的更新为新书名
        UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(EsConstant.BOOK_INDEX_NAME);
        // 设置版本冲突时继续
        updateRequest.setConflicts("proceed");
        updateRequest.setRefresh(true);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 设置查询条件，将书名是旧书名的数据找出来
        boolQuery.must(QueryBuilders.matchPhrasePrefixQuery("bookName", oldBookName));
        updateRequest.setQuery(boolQuery);
        // 调用update后刷新索引
        updateRequest.setRefresh(true);
        // 每次更新的文档数量
        // 注意：如果你更新的数量超过了这个值，需要根据别的条件进行循环更新，比如先找出ID列表，分批次执行
        updateRequest.setBatchSize(1000);

        Map<String, Object> params = new HashMap<>(2);
        params.put("bookName", newBookName);
        // 设置请求参数
        // 失败重试次数
        updateRequest.setMaxRetries(20);
        // 每个请求/秒执行的更新文档数
        updateRequest.setRequestsPerSecond(1000);
        // 在多个分片上并行执行
        updateRequest.setSlices(3);

        // 设置更新脚本，将图书名称修改为参数中的名称
        // 注意，当更新的数量过多时，最好使用params这种方式进行更新，因为每次通过这种方式更新，如果使用拼接字符串的方式，都会重新编译脚本，而编译脚本是很耗时的，通过 params 这种方式，只需要编译一次即可，否则会报 "Too many dynamic script compilations within" 的错误
        updateRequest.setScript(new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG,
                "ctx._source.bookName = params.bookName;", params));
        try {
            BulkByScrollResponse updateByQueryResponse = client.updateByQuery(updateRequest, COMMON_OPTIONS);
            return CollectionUtils.isEmpty(updateByQueryResponse.getBulkFailures());
        } catch (IOException e) {
            log.error("通过查询更新图书信息失败", e);
        }
        return false;
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
        String[] includes = {"id", "bookName", "introduction"};
        String[] excludes = {};
        searchSourceBuilder.fetchSource(includes, excludes);

        // 高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("introduction");
        highlightBuilder.field("bookName");
        highlightBuilder.boundaryScannerLocale("zh_CN");
        searchSourceBuilder.highlighter(highlightBuilder);

        // sortField 排序字段，sort 排序顺序
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

    @Override
    public List<CategoryGroup> categoryGroup() {
        SearchResponse groupSearchResponse = searchGroupData();
        if (Objects.isNull(groupSearchResponse)) {
            return Collections.emptyList();
        }
        Aggregations aggregations = groupSearchResponse.getAggregations();
        List<CategoryGroup> groupList = new ArrayList<>();
        if (Objects.nonNull(aggregations)) {
            // 获取最外层的分组
            ParsedLongTerms group = aggregations.get("group");
            // 获取总数的 aggregation
            ParsedCardinality totalCount = aggregations.get("total");
            // 这里的总数我们暂时不返回，只是打印
            log.info("得到分组的总数：{}", totalCount);
            // 遍历得到的桶
            for (Terms.Bucket bucket : group.getBuckets()) {
                // 获取每个桶中的top1数据
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
                // 键值即为类目ID
                categoryGroup.setCategoryId(Integer.valueOf(bucket.getKeyAsString()));
                categoryGroup.setBooks(books);
                groupList.add(categoryGroup);
            }
        }

        return groupList;
    }

    @Override
    public Boolean updatePrices(UpdatePricesReq updateReq) {
        // update bulk，批量更新
        List<Long> bookIds = updateReq.getBookIds();
        Double price = updateReq.getPrice();
        // 构建批量请求
        BulkRequest bulkRequest = new BulkRequest();
        Map<String, Object> map;
        for (Long bookId : bookIds) {
            map = new HashMap<>(2);
            map.put("price", price);
            UpdateRequest updateRequest = new UpdateRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(bookId))
                    .doc(map, XContentType.JSON);
            // 将请求加入到 bulk 请求中
            bulkRequest.add(updateRequest);
        }
        try {
            // 同步批量更新
            BulkResponse bulkResponse = client.bulk(bulkRequest, COMMON_OPTIONS);
            // 如果没有失败的，表明全部更新成功
            return !bulkResponse.hasFailures();
        } catch (Exception e) {
            log.error("批量更新图书价格失败：{}", JSONUtil.toJsonStr(updateReq), e);
            return false;
        }
    }

    /**
     * 聚合搜索图书信息
     *
     * @return {@link SearchResponse}
     */
    private SearchResponse searchGroupData() {
        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest(EsConstant.BOOK_INDEX_NAME);

        AggregationBuilder groupAggregation = AggregationBuilders
                // 这里是定义的最外层聚合的名字
                .terms("group")
                // 这是返回的分组的数量，默认是10
                .size(10)
                // 要分组的字段
                .field("categoryId");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 不需要返回hits中的数据，设置 size 为 0
        searchSourceBuilder.size(0);

        // TOP hits 取图书的 top1
        // 定义返回的字段
        String[] includes = {"id", "bookName", "categoryId", "categoryName", "price"};
        String[] excludes = {};
        AggregationBuilder topHits = AggregationBuilders
                // 自定义的topHits名称
                .topHits("price_top_1")
                // 按照价格倒序
                .sort(SortBuilders.fieldSort("price").order(SortOrder.DESC))
                .fetchSource(includes, excludes)
                // 只取1条
                .size(1);
        // 将两个聚合结合起来，topHits 作为分组的子聚合
        groupAggregation.subAggregation(topHits);

        // 求分组之后的总数
        CardinalityAggregationBuilder totalAggregation = AggregationBuilders.cardinality("total")
                .field("categoryId");
        // 把聚合放在搜索构造器里
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
        // 总的聚合查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // isbnNo 图书编码，这里是精确搜索
        if (StringUtils.isNotBlank(searchReq.getIsbnNo())) {
            TermQueryBuilder isbnNoQuery = QueryBuilders.termQuery("isbnNo", searchReq.getIsbnNo());
            // 使用 must 聚合
            boolQuery.must(isbnNoQuery);
        }

        // bookName 图书名称，模糊搜索
        if (StringUtils.isNotBlank(searchReq.getBookName())) {
            // 模糊搜索，使用短语匹配
            BoolQueryBuilder bookNameQuery = QueryBuilders.boolQuery();
            bookNameQuery.should(QueryBuilders.matchQuery("bookName", searchReq.getBookName()));
            bookNameQuery.should(QueryBuilders.matchPhraseQuery("bookName", searchReq.getBookName()));
            // 另外一种模糊匹配功能与 sql 中 like '%%' 相似，两端需要加上匹配符
//            bookNameQuery.should(QueryBuilders.wildcardQuery("bookName.keyword", "*" + searchReq.getBookName() + "*"));
            // 使用 must 聚合
            boolQuery.must(bookNameQuery);
        }

        // 此处根据图书作者进行搜索，只是为了说明情况，不一定用于真实应用场景，大家可以由此进行类推
        // bookAuthorLastName 图书作者姓氏，bookAuthorFirstName 图书作者名字，查询作者名称的时候既要满足姓氏，也要满足名字
        if (StringUtils.isNotBlank(searchReq.getBookAuthorLastName()) && StringUtils.isNotBlank(searchReq.getBookAuthorFirstName())) {
            // 满足两个条件的，需要使用 nested 嵌套结构，查询使用
            BoolQueryBuilder nameQuery = QueryBuilders.boolQuery();
            nameQuery.must(QueryBuilders.termQuery("authors.lastName", searchReq.getBookAuthorLastName()
                    .toLowerCase(Locale.ROOT)));
            nameQuery.must(QueryBuilders.termQuery("authors.firstName", searchReq.getBookAuthorFirstName()
                    .toLowerCase(Locale.ROOT)));

            NestedQueryBuilder authorNameQuery = QueryBuilders.nestedQuery("authors", nameQuery, ScoreMode.Max);
            // 使用 must 聚合
            boolQuery.must(authorNameQuery);
        }

        // tags 图书标签，多选，多个匹配
        if (CollectionUtils.isNotEmpty(searchReq.getTags())) {
            // 多个标签进行匹配，多个匹配
            TermsQueryBuilder tagsQuery = QueryBuilders.termsQuery("tags", searchReq.getTags());
            // 使用 must 聚合
            boolQuery.must(tagsQuery);
        }

        // introduction 图书介绍，全文搜索，以及匹配高亮样式返回
        if (StringUtils.isNotBlank(searchReq.getIntroduction())) {
            // TODO
        }

        // categoryId 类别搜索，在 categoryChain 中匹配
        if (searchReq.getCategoryId() != null) {
            TermQueryBuilder categoryQuery = QueryBuilders.termQuery("categoryChain.id", searchReq.getCategoryId());
            // 使用 must 进行聚合
            boolQuery.must(categoryQuery);
        }

        // publishTimeStart 发版时间区间起始，publishTimeEnd 发版时间区间末端，使用区间搜索
        if (StringUtils.isNotBlank(searchReq.getPublishTimeStart()) && StringUtils.isNotBlank(searchReq.getPublishTimeEnd())) {
            RangeQueryBuilder publishTimeQuery = QueryBuilders.rangeQuery("publishTime");
            // from 大于等于
            publishTimeQuery.gte(searchReq.getPublishTimeStart());
            // to 小于等于
            try {
                publishTimeQuery.lte(DateFormatUtils.format(
                        DateUtils.addDays(DateUtils.parseDate(searchReq.getPublishTimeEnd(), DATE_PATTERN), 1),
                        DATE_PATTERN));
            } catch (ParseException e) {
                log.error("failed to parse string to date, {}.", searchReq.getPublishTimeEnd(), e);
            }
            // 使用 must 进行聚合
            boolQuery.must(publishTimeQuery);
        }

        // pressId 出版社ID，精确匹配
        if (StringUtils.isNotBlank(searchReq.getPressId())) {
            TermQueryBuilder pressQuery = QueryBuilders.termQuery("press.pressId", searchReq.getPressId());
            // 使用 must 进行聚合
            boolQuery.must(pressQuery);
        }

        // priceStart 售价范围起始，priceEnd 售价范围末端，使用区间搜索
        if (searchReq.getPriceStart() != null) {
            RangeQueryBuilder priceStartQuery = QueryBuilders.rangeQuery("price");
            // from 大于等于
            priceStartQuery.gte(searchReq.getPriceStart());
            // 使用 must 进行聚合
            boolQuery.must(priceStartQuery);
        }
        if (searchReq.getPriceEnd() != null) {
            RangeQueryBuilder priceEndQuery = QueryBuilders.rangeQuery("price");
            // to 小于等于
            priceEndQuery.lte(searchReq.getPriceEnd());
            // 使用 must 进行聚合
            boolQuery.must(priceEndQuery);
        }

        return boolQuery;
    }
}
