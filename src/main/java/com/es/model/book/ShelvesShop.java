package com.es.model.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zetu
 * @date 2021/5/10
 */
@Data
@Accessors(chain = true)
public class ShelvesShop implements Serializable {

    private static final long serialVersionUID = -7010907394039344391L;

    @ApiModelProperty("店铺ID")
    private String shopId;

    @ApiModelProperty("店铺名称")
    private String shopName;

}
