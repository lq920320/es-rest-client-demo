package com.es.service;

/**
 * @author zetu
 * @date 2021/5/10
 */
public interface BookService {

    /**
     * create Index
     *
     * @param index elasticsearch index name
     */
    void createIndex(String index);

    /**
     * delete Index
     *
     * @param index elasticsearch index name
     */
    void deleteIndex(String index);


    /**
     * 根据 ID 删除数据
     *
     * @param id
     * @return
     */
    Boolean delete(Long id);
}
