package com.es;

import com.es.common.constants.EsConstant;
import com.es.service.BookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zetu
 * @date 2021/5/10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BookEsTests {

    @Autowired
    private BookService bookService;

    /**
     * 创建索引
     */
    @Test
    public void createIndexTest() {
        bookService.createIndex(EsConstant.BOOK_INDEX_NAME);
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndexTest() {
        bookService.deleteIndex(EsConstant.BOOK_INDEX_NAME);
    }

    /**
     * 批量更新/插入数据
     */
    @Test
    public void batchUpsertTest() {

    }

}
