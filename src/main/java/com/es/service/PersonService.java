package com.es.service;

import com.es.dto.person.ModifyPersonReq;
import com.es.model.person.Person;

import java.util.List;

/**
 * @author zetu
 * @date 2021/5/10
 */
public interface PersonService {

    /**
     * 获取所有数据
     *
     * @return 数据列表
     */
    List<Person> searchList();

    /**
     * 根据ID获取数据
     *
     * @param id 文档ID
     * @return 详情
     */
    Person getById(Long id);

    /**
     * 添加数据
     *
     * @param modifyReq 更新请求体
     * @return 更新结果
     */
    Boolean add(ModifyPersonReq modifyReq);

    /**
     * 更新数据
     *
     * @param modifyReq 更新请求体
     * @return
     */
    Boolean update(ModifyPersonReq modifyReq);

    /**
     * 删除数据
     *
     * @param id 文档ID
     * @return
     */
    Boolean delete(Long id);
}
