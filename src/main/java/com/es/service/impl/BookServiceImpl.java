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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
            MatchPhraseQueryBuilder bookNameQuery = QueryBuilders.matchPhraseQuery("bookName", searchReq.getBookName());
            // 另外一种模糊匹配功能与 sql 中 like '%%' 相似，两端需要加上匹配符
//            WildcardQueryBuilder bookNameQuery2 = QueryBuilders.wildcardQuery("bookName", "*" + searchReq.getBookName() + "*");
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

        // introduction 图书介绍，全文搜索，以及匹配样式返回
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
