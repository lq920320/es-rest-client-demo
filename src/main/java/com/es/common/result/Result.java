package com.es.common.result;


import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @author zetu
 * @date 2021/4/1
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 800426755174767892L;

    private Integer code;

    private String msg;

    private T data;

    private Boolean success;

    private long total;

    public Result() {
    }

    private Result(ResultCode resultCode) {
        this(resultCode.code, resultCode.msg);
    }

    private Result(ResultCode resultCode, T data) {
        this(resultCode.code, resultCode.msg, data);
    }

    public static <T> Result<T> success(T data, long total) {
        return createResult(true, "success", data, total);
    }

    private static <T> Result<T> createResult(boolean success, String message, T data, Long total) {
        Result<T> r = createResult(success, message, data);
        r.setTotal(total);
        return r;
    }

    private static <T> Result<T> createResult(boolean success, String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setSuccess(success);
        r.setMsg(message);
        r.setData(data);
        return r;
    }

    private Result(int code, String msg) {
        this(code, msg, null);
    }

    private Result(int code, String msg, T data) {
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
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS);
    }


    /**
     * 返回成功-携带数据
     *
     * @param data 响应数据
     * @param <T>  泛型标记
     * @return 响应信息 {@code Result}
     */
    public static <T> Result<T> success(@Nullable T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public Boolean getSuccess() {
        return ResultCode.SUCCESS.code.equals(code);
    }

}
