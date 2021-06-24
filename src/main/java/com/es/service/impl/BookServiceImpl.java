package com.es.service.impl;

import com.es.common.constants.EsConstant;
import com.es.service.BookService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Service
@Slf4j
public class BookServiceImpl extends BaseEsService implements BookService {


    @Override
    public void createIndex(String index) {
        createIndexRequest(index);
    }

    @Override
    public void deleteIndex(String index) {
        deleteIndexRequest(index);
    }

    @Override
    public Boolean delete(Long id) {
        // 根据ID删除数据
        DeleteResponse response = deleteRequest(EsConstant.BOOK_INDEX_NAME, String.valueOf(id));
        return response.status().equals(RestStatus.OK);
    }
}
