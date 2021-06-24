package com.es.model.person;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zetu
 * @date 2021/4/1
 */
@Data
@Accessors(chain = true)
public class Person implements Serializable {

    private static final long serialVersionUID = 1389115485845100464L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 名字
     */
    @ApiModelProperty("名字")
    private String name;

    /**
     * 国家
     */
    @ApiModelProperty("国家")
    private String country;

    /**
     * 年龄
     */
    @ApiModelProperty("年龄")
    private Integer age;

    /**
     * 生日
     */
    @ApiModelProperty("生日")
    private LocalDateTime birthday;

    /**
     * 介绍
     */
    @ApiModelProperty("介绍")
    private String remark;
}
