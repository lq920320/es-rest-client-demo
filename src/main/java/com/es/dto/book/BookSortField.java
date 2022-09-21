package com.es.dto.book;

import com.es.enums.SortEnum;
import com.es.enums.SortFieldEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 搜索排序字段，包含排序字段
 *
 * @author zetu
 * @date 2022/9/21
 */
@Data
public class BookSortField {

    @ApiModelProperty("排序字段")
    private SortFieldEnum sortField;

    @ApiModelProperty("排序顺序")
    private SortEnum sort;
}
