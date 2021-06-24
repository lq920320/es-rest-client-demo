package com.es.model.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 图书出版社
 *
 * @author zetu
 * @date 2021/5/10
 */
@Data
@Accessors(chain = true)
public class Press implements Serializable {

    private static final long serialVersionUID = 5783987274236922050L;

    @ApiModelProperty("出版社ID")
    private String pressId;

    @ApiModelProperty("出版社名称")
    private String pressName;
}
