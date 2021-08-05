package com.es.dto.book;

import com.es.enums.SortEnum;
import com.es.enums.SortFieldEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 搜索图书信息请求参数
 *
 * @author zetu
 * @date 2021/7/21
 */
@Data
public class SearchBookReq {
    @ApiModelProperty("ISBN编号")
    private String isbnNo;

    @ApiModelProperty("书名")
    private String bookName;

    @ApiModelProperty("图书作者名字")
    private String bookAuthorFirstName;

    @ApiModelProperty("图书作者姓氏")
    private String bookAuthorLastName;

    @ApiModelProperty("标签")
    private List<String> tags;

    @ApiModelProperty("图书介绍")
    private String introduction;

    @ApiModelProperty("书籍分类Id")
    private Integer categoryId;

    @ApiModelProperty("发版时间区间起始，时间格式：yyyy-MM-dd")
    private String publishTimeStart;

    @ApiModelProperty("发版时间区间末端，时间格式：yyyy-MM-dd")
    private String publishTimeEnd;

    @ApiModelProperty("出版社Id")
    private String pressId;

    @ApiModelProperty("售价范围起始")
    private Double priceStart;

    @ApiModelProperty("售价范围末端")
    private Double priceEnd;

    @ApiModelProperty("排序字段")
    private SortFieldEnum sortField;

    @ApiModelProperty("排序顺序")
    private SortEnum sort;

    @ApiModelProperty("分页页码")
    private Integer page;

    @ApiModelProperty("分页大小")
    private Integer pageSize;
}
