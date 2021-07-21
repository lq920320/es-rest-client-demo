package com.es.dto.person;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * 变更数据（新增或者更新）请求参数
 *
 * @author zetu
 * @date 2021/4/1
 */
@Data
public class ModifyPersonReq {

    @ApiModelProperty("数据ID")
    private Long id;

    @ApiModelProperty("名字")
    private String name;

    @ApiModelProperty("国家")
    private String country;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("生日")
    private LocalDateTime birthday;

    @ApiModelProperty("介绍")
    private String remark;
}
