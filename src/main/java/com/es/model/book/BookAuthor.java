package com.es.model.book;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 图书作者
 *
 * @author zetu
 * @date 2021/7/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BookAuthor {
    @ApiModelProperty("作者名字")
    private String firstName;

    @ApiModelProperty("作者姓氏")
    private String lastName;

    @ApiModelProperty("作者所在大学")
    private String university;
}
