package com.es.common.result;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * @author zetu
 * @date 2021/4/1
 */
@Data
public class PagedResult<T> implements Serializable {

    private static final long serialVersionUID = -6223866777090425870L;

    private Integer code;

    private String msg;

    private List<T> data;

    private Boolean success;
    /**
     * 页码
     */
    private Integer pageNum;
    /**
     * 分页大小
     */
    private Integer pageSize;
    /**
     * 查询出的总数
     */
    private Long total;

    public PagedResult() {
    }

    private PagedResult(ResultCode resultCode) {
        this(resultCode.code, resultCode.msg);
    }

    private PagedResult(ResultCode resultCode, List<T> data) {
        this(resultCode.code, resultCode.msg, data);
    }

    private PagedResult(int code, String msg) {
        this(code, msg, null);
    }

    private PagedResult(int code, String msg, List<T> data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    /**
     * 返回成功
     *
     * @param <T> 泛型标记
     * @return 响应信息 {@code Result}
     */
    public static <T> PagedResult<T> success() {
        return new PagedResult<>(ResultCode.SUCCESS);
    }


    /**
     * 返回成功-携带数据
     *
     * @param data 响应数据
     * @param <T>  泛型标记
     * @return 响应信息 {@code Result}
     */
    public static <T> PagedResult<T> success(@Nullable List<T> data) {
        return new PagedResult<>(ResultCode.SUCCESS, data);
    }

    public Boolean getSuccess() {
        return ResultCode.SUCCESS.code.equals(code);
    }
}
