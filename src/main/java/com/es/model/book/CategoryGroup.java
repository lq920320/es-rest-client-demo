package com.es.model.book;

import lombok.Data;

import java.util.List;

/**
 * 图书类目分组
 *
 * @author zetu
 * @date 2021/11/20
 */
@Data
public class CategoryGroup {
    /**
     * 类目ID
     */
    private Integer categoryId;

    /**
     * 该类目下的最贵的图书
     */
    private List<Book> books;

}
