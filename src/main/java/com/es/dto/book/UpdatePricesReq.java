package com.es.dto.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 批量更改图书价格
 *
 * @author zetu
 * @date 2021/11/29
 */
@Data
public class UpdatePricesReq {
    @ApiModelProperty("图书ID列表")
    private List<Long> bookIds;

    @ApiModelProperty("要更新的图书价格")
    private Double price;
}
