package com.es;

import com.es.common.constants.EsConstant;
import com.es.dto.book.ModifyBookReq;
import com.es.model.book.BookAuthor;
import com.es.model.book.Press;
import com.es.service.BookService;
import com.es.service.EsService;
import io.micrometer.core.instrument.util.TimeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zetu
 * @date 2021/5/10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BookEsTests {

    @Autowired
    private BookService bookService;
    @Autowired
    private EsService esService;

    /**
     * 检查索引是否存在
     */
    @Test
    public void checkIndexExistTest() {
        Assert.assertTrue(esService.checkIndexExists(EsConstant.BOOK_INDEX_NAME));
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndexTest() {
        esService.createIndex(EsConstant.BOOK_INDEX_NAME);
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndex2Test() {
        esService.createIndex(EsConstant.BOOK_INDEX_2_NAME);
    }

    /**
     * 创建索引
     */
    @Test
    public void reindexTest() throws InterruptedException {
        boolean indexExists = esService.checkIndexExists(EsConstant.BOOK_INDEX_2_NAME);
        System.out.println("book2的索引是否存在: " + indexExists);
        if (indexExists) {
            System.out.println("删除book2的索引");
            esService.deleteIndex(EsConstant.BOOK_INDEX_2_NAME);
        }
        Thread.sleep(10000);
        System.out.println("book2的索引是否存在: " + indexExists);
        Assert.assertFalse(indexExists);
        esService.reindex(EsConstant.BOOK_INDEX_NAME, EsConstant.BOOK_INDEX_2_NAME);
        Thread.sleep(10000);
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndexTest() {
        esService.deleteIndex(EsConstant.BOOK_INDEX_NAME);
    }

    /**
     * 批量更新/插入数据
     */
    @Test
    public void batchUpsertTest() throws InterruptedException {
        List<ModifyBookReq> bookReqs = buildBookReqList();

        bookService.addList(bookReqs);
        Thread.sleep(2000);
    }

    private List<ModifyBookReq> buildBookReqList() {
        List<ModifyBookReq> bookReqs = new ArrayList<>();

        List<BookAuthor> book1Authors = new ArrayList<>();
        book1Authors.add(new BookAuthor() {{
            setFirstName("三");
            setLastName("张");
            setUniversity("Abc大学");
        }});
        book1Authors.add(new BookAuthor() {{
            setFirstName("四");
            setLastName("李");
            setUniversity("Bcd大学");
        }});
        List<String> book1Tags = new ArrayList<>();
        book1Tags.add("畅销书");
        book1Tags.add("教科书");
        book1Tags.add("推荐");
        Press book1Press = new Press() {{
            setPressId("111");
            setPressName("出版社1");
        }};
        ModifyBookReq book1 = ModifyBookReq.builder()
                .id(1L)
                .isbnNo("isbn-1")
                .bookName("编译原理")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.7)
                .introduction("这是一本值得推荐的好书")
                .categoryChain(new ArrayList<>())
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(75.68).build();

        bookReqs.add(book1);

        return bookReqs;
    }

}
