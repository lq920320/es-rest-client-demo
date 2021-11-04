package com.es.service;

/**
 * ES相关操作
 *
 * @author zetu
 * @date 2021/7/21
 */
public interface EsService {

    /**
     * check index if exists
     *
     * @param index elasticsearch index name
     * @return exists
     */
    boolean checkIndexExists(String index);

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
     * reindex
     * copy data of sourceIndex to targetIndex
     *
     * @param sourceIndex source index name
     * @param targetIndex target index name
     */
    void reindex(String sourceIndex, String targetIndex);
}
