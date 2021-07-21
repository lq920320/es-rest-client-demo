package com.es;

import com.es.common.constants.EsConstant;
import com.es.model.person.Person;
import com.es.service.EsService;
import com.es.service.PersonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PersonEsTests {

    @Autowired
    private PersonService personService;
    @Autowired
    private EsService esService;

    /**
     * 检查索引是否存在
     */
    @Test
    public void checkIndexExistTest() {
        Assert.assertTrue(esService.checkIndexExists(EsConstant.INDEX_NAME));
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndexTest() {
        esService.createIndex(EsConstant.INDEX_NAME);
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndexTest() {
        esService.deleteIndex(EsConstant.INDEX_NAME);
    }

    /**
     * 批量插入数据
     */
    @Test
    public void batchInsertTest() {

    }

    /**
     * 批量更新数据
     */
    @Test
    public void batchUpdateTest() {

    }

    /**
     * 批量更新/插入数据
     */
    @Test
    public void batchUpsertTest() {

    }

    /**
     * 单个查询测试
     */
    @Test
    public void searchById() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person person = personService.getById(Long.parseLong("1"));
        System.out.println(objectMapper.writeValueAsString(person));
    }

    /**
     * 测试删除
     */
    @Test
    public void deleteTest() {
        personService.delete(1L);
    }
}
