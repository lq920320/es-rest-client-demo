package com.es.enums;

import lombok.Getter;

/**
 * 排序字段枚举
 *
 * @author zetu
 * @date 2021/7/21
 */
@Getter
public enum SortFieldEnum {
    /**
     * 按照价格排序
     */
    PRICE("price"),
    /**
     * 按照发布时间排序
     */
    PUBLISH_TIME("publishTime");

    private final String field;

    SortFieldEnum(String field) {
        this.field = field;
    }

}
