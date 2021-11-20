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
//        if (indexExists) {
//            System.out.println("删除book2的索引");
//            esService.deleteIndex(EsConstant.BOOK_INDEX_2_NAME);
//        }
//        Thread.sleep(10000);
//        System.out.println("book2的索引是否存在: " + indexExists);
//        Assert.assertFalse(indexExists);
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
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(75.68).build();
        ModifyBookReq book2 = ModifyBookReq.builder()
                .id(2L)
                .isbnNo("isbn-2")
                .bookName("CSS教程")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.5)
                .introduction("这是一本值得推荐的好书")
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(70.68).build();
        ModifyBookReq book3 = ModifyBookReq.builder()
                .id(3L)
                .isbnNo("isbn-3")
                .bookName("Java编程思想")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.6)
                .introduction("这是一本值得推荐的好书")
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(88.00).build();
        ModifyBookReq book4 = ModifyBookReq.builder()
                .id(4L)
                .isbnNo("isbn-4")
                .bookName("Linux教程")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.0)
                .introduction("这是一本值得推荐的好书")
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(80.00).build();
        ModifyBookReq book5 = ModifyBookReq.builder()
                .id(5L)
                .isbnNo("isbn-5")
                .bookName("设计模式（Java版）")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.9)
                .introduction("这是一本值得推荐的好书")
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(60.99).build();
        ModifyBookReq book6 = ModifyBookReq.builder()
                .id(6L)
                .isbnNo("isbn-6")
                .bookName("JVM虚拟机")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(3.8)
                .introduction("这是一本值得推荐的好书")
                .categoryId(1)
                .categoryName("教科书")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(55.68).build();
        ModifyBookReq book7 = ModifyBookReq.builder()
                .id(7L)
                .isbnNo("isbn-7")
                .bookName("解忧杂货铺")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(3.8)
                .introduction("这是一本值得推荐的好书")
                .categoryId(2)
                .categoryName("小说")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(20.00).build();
        ModifyBookReq book8 = ModifyBookReq.builder()
                .id(8L)
                .isbnNo("isbn-8")
                .bookName("神雕侠侣")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.7)
                .introduction("这是一本值得推荐的好书")
                .categoryId(2)
                .categoryName("小说")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(40.0).build();
        ModifyBookReq book9 = ModifyBookReq.builder()
                .id(9L)
                .isbnNo("isbn-9")
                .bookName("倚天屠龙记")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.2)
                .introduction("这是一本值得推荐的好书")
                .categoryId(2)
                .categoryName("小说")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(35.68).build();
        ModifyBookReq book10 = ModifyBookReq.builder()
                .id(10L)
                .isbnNo("isbn-10")
                .bookName("鲁迅传记")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.8)
                .introduction("这是一本值得推荐的好书")
                .categoryId(3)
                .categoryName("人物传记")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(75.68).build();
        ModifyBookReq book11 = ModifyBookReq.builder()
                .id(11L)
                .isbnNo("isbn-11")
                .bookName("乔布斯传")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.7)
                .introduction("这是一本值得推荐的好书")
                .categoryId(3)
                .categoryName("人物传记")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(75.68).build();
        ModifyBookReq book12 = ModifyBookReq.builder()
                .id(12L)
                .isbnNo("isbn-1")
                .bookName("周恩来传")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.8)
                .introduction("这是一本值得推荐的好书")
                .categoryId(3)
                .categoryName("人物传记")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(99.68).build();
        ModifyBookReq book13 = ModifyBookReq.builder()
                .id(13L)
                .isbnNo("isbn-13")
                .bookName("丘吉尔传")
                .authors(book1Authors)
                .tags(book1Tags)
                .score(4.7)
                .introduction("这是一本值得推荐的好书")
                .categoryId(3)
                .categoryName("人物传记")
                .publishTime(LocalDateTime.now())
                .press(book1Press)
                .price(75.68).build();

        bookReqs.add(book1);
        bookReqs.add(book2);
        bookReqs.add(book3);
        bookReqs.add(book4);
        bookReqs.add(book5);
        bookReqs.add(book6);
        bookReqs.add(book7);
        bookReqs.add(book8);
        bookReqs.add(book9);
        bookReqs.add(book10);
        bookReqs.add(book11);
        bookReqs.add(book12);
        bookReqs.add(book13);

        return bookReqs;
    }

}
