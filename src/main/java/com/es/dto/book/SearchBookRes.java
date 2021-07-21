package com.es.dto.book;

import com.es.model.book.Book;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 搜索图书信息响应参数
 *
 * @author zetu
 * @date 2021/7/21
 */
@Data
public class SearchBookRes {
    @ApiModelProperty("图书信息列表")
    private List<Book> bookList;

    @ApiModelProperty("符合条件的数据总数")
    private long total;

}
