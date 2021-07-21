package com.es.service.impl;

import com.es.service.EsService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zetu
 * @date 2021/7/21
 */
@Service
@Slf4j
public class EsServiceImpl extends BaseEsService implements EsService {

    @Override
    public boolean checkIndexExists(String index) {
        return checkIndexExistsRequest(index);
    }

    @Override
    public void createIndex(String index) {
        createIndexRequest(index);
    }

    @Override
    public void deleteIndex(String index) {
        deleteIndexRequest(index);
    }
}
