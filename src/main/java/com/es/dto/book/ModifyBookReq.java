package com.es.dto.book;

import com.es.model.book.BookAuthor;
import com.es.model.book.Category;
import com.es.model.book.Press;
import com.es.model.book.ShelvesShop;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新图书信息
 *
 * @author zetu
 * @date 2021/7/21
 */
@Data
public class ModifyBookReq {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("ISBN编号")
    private String isbnNo;

    @ApiModelProperty("书名")
    private String bookName;

    @ApiModelProperty("作者")
    private List<BookAuthor> authors;

    @ApiModelProperty("标签")
    private List<String> tags;

    @ApiModelProperty("书籍评分")
    private Double score;

    @ApiModelProperty("图书介绍")
    private String introduction;

    @ApiModelProperty("书籍分类链路")
    private List<Category> categoryChain;

    @ApiModelProperty("已上架的店铺")
    private List<ShelvesShop> shelvesShops;

    @ApiModelProperty("发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty("出版社")
    private Press press;

    @ApiModelProperty("售价")
    private Double price;
}
