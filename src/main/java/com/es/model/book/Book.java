package com.es.model.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Data
@Accessors(chain = true)
public class Book implements Serializable {

    private static final long serialVersionUID = -1448455998070660980L;

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("ISBN编号")
    private String isbnNo;

    @ApiModelProperty("书名")
    private String bookName;

    @ApiModelProperty("作者")
    private List<String> authors;

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
