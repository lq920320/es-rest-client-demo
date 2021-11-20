package com.es.model.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Book implements Serializable {

    private static final long serialVersionUID = -1448455998070660980L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("ISBN编号")
    private String isbnNo;

    @ApiModelProperty("书名")
    private String bookName;

    @ApiModelProperty("封面")
    private String cover;

    @ApiModelProperty("作者")
    private List<BookAuthor> authors;

    @ApiModelProperty("标签")
    private List<String> tags;

    @ApiModelProperty("书籍评分")
    private Double score;

    @ApiModelProperty("图书介绍")
    private String introduction;

    @ApiModelProperty("图书分类")
    private String categoryId;

    @ApiModelProperty("图书分类名称")
    private String categoryName;

    @ApiModelProperty("发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty("出版社")
    private Press press;

    @ApiModelProperty("售价")
    private Double price;

}
