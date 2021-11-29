package com.es.service;

import com.es.dto.book.ModifyBookReq;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.dto.book.UpdatePricesReq;
import com.es.model.book.Book;
import com.es.model.book.CategoryGroup;

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
     * @param bookId    图书ID
     * @param modifyReq 更新请求体
     * @return 是否更新成功
     */
    Boolean update(Long bookId, ModifyBookReq modifyReq);

    /**
     * 按照查询条件更新数据，script 更新
     *
     * @param bookId    图书ID
     * @param modifyReq 更新请求体
     * @return 是否更新成功
     */
    Boolean updateByQuery(Long bookId, ModifyBookReq modifyReq);


    /**
     * 根据 ID 删除数据
     *
     * @param id 图书ID
     * @return 删除结果，是否成功
     */
    Boolean delete(Long id);

    /**
     * 搜索图书信息
     *
     * @param searchReq 查询参数
     * @return {@link SearchBookRes} 查询结果
     */
    SearchBookRes searchBook(SearchBookReq searchReq);

    /**
     * 按照图书类目分组查询
     *
     * @return {@link List<CategoryGroup>}
     */
    List<CategoryGroup> categoryGroup();

    /**
     * 批量更新图书价格
     *
     * @param updateReq 更新价格请求体
     * @return {@link Boolean} 更新结果
     */
    Boolean updatePrices(UpdatePricesReq updateReq);
}
