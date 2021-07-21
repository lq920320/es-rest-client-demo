package com.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.es.common.constants.EsConstant;
import com.es.dto.person.ModifyPersonReq;
import com.es.model.person.Person;
import com.es.service.PersonService;
import com.es.service.base.BaseEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Service
@Slf4j
public class PersonServiceImpl extends BaseEsService implements PersonService {

    @Override
    public List<Person> searchList() {
        SearchResponse searchResponse = search(EsConstant.INDEX_NAME);
        return convertPersonList(searchResponse);
    }

    @Override
    public Person getById(Long id) {
        GetRequest getRequest = new GetRequest(EsConstant.INDEX_NAME).id(String.valueOf(id));
        try {
            GetResponse response = client.get(getRequest, COMMON_OPTIONS);
            Map<String, Object> sourceAsMap = response.getSourceAsMap();
            return BeanUtil.mapToBean(sourceAsMap, Person.class, false, new CopyOptions());
        } catch (IOException e) {
            log.error("Failed to get doc of id: {}", id, e);
            return null;
        }
    }

    private List<Person> convertPersonList(SearchResponse searchResponse) {
        List<Person> personList = new ArrayList<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        // 将搜索结果转换为响应结果对象
        Arrays.stream(hits).forEach(hit -> {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Person person = BeanUtil.mapToBean(sourceAsMap, Person.class, false, new CopyOptions());
            if (Objects.nonNull(person.getId())) {
                personList.add(person);
            }
        });
        return personList;
    }

    @Override
    public Boolean add(ModifyPersonReq modifyReq) {
        Person person = new Person();
        BeanUtils.copyProperties(modifyReq, person);
        // 插入到 ES
        IndexRequest request = buildIndexRequest(EsConstant.INDEX_NAME, String.valueOf(person.getId()), person);
        IndexResponse indexResponse;
        try {
            indexResponse = client.index(request, COMMON_OPTIONS);
        } catch (IOException e) {
            log.error("Failed to insert.", e);
            return false;
        }
        // 返回插入结果
        return indexResponse != null && indexResponse.getResult().equals(DocWriteResponse.Result.CREATED);
    }

    @Override
    public Boolean update(ModifyPersonReq modifyReq) {
        // 更新数据到 ES
        Person person = new Person();
        BeanUtils.copyProperties(modifyReq, person);
        UpdateResponse response = updateRequest(EsConstant.INDEX_NAME, String.valueOf(person.getId()), person);
        return response.status().equals(RestStatus.OK);
    }

    @Override
    public Boolean delete(Long id) {
        // 根据ID删除数据
        DeleteResponse response = deleteRequest(EsConstant.INDEX_NAME, String.valueOf(id));
        return response.status().equals(RestStatus.OK);
    }


}
