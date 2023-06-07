package com.hsinpeng.webapp.dto;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 0xAC750L;
    private int code;
    private String message;
    private T data;

    private ApiResponse() {
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> result = new ApiResponse<>();
        result.data = data;
        result.code = 0;
        return result;
    }

    public static <T> ApiResponse<T> success() {
        ApiResponse<T> result = new ApiResponse<>();
        result.code = 0;
        return result;
    }

    public static <T> ApiResponse<T> failed(int code) {
        ApiResponse<T> result = new ApiResponse<>();
        result.code = code;
        return result;
    }

    public static <T> ApiResponse<T> failed(int code, String message) {
        ApiResponse<T> result = new ApiResponse<>();
        result.code = code;
        result.message = message;
        return result;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
