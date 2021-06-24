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
public class Category implements Serializable {

    private static final long serialVersionUID = 3113800086847926640L;

    @ApiModelProperty("分类ID")
    private Integer id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("父级ID")
    private Integer pid;
}
