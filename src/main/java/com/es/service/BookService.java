package com.es.service;

import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.model.book.Book;
import com.es.dto.book.ModifyBookReq;

import java.util.List;

/**
 * @author zetu
 * @date 2021/5/10
 */
public interface BookService {

    /**
     * 根据ID获取数据
     *
     * @param id 文档ID
     * @return 详情
     */
    Book getById(Long id);

    /**
     * 添加数据
     *
     * @param modifyReq 更新请求体
     * @return 更新结果
     */
    Boolean add(ModifyBookReq modifyReq);


    /**
     * 添加数据
     *
     * @param list 数据列表
     * @return 更新结果
     */
    Boolean addList(List<ModifyBookReq> list);

    /**
     * 更新数据
     *
     * @param modifyReq 更新请求体
     * @return
     */
    Boolean update(ModifyBookReq modifyReq);


    /**
     * 根据 ID 删除数据
     *
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 搜索图书信息
     *
     * @param searchReq 查询参数
     * @return {@link SearchBookRes} 响应结果
     */
    SearchBookRes searchBook(SearchBookReq searchReq);
}
