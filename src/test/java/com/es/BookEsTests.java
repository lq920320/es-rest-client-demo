package com.es;

import com.es.common.constants.EsConstant;
import com.es.service.BookService;
import com.es.service.EsService;
import org.junit.Assert;
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
    public void batchUpsertTest() {

    }

}
