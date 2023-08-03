package com.atguigu.common.result;

import lombok.Data;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Data
public class R {

    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public R() {

    }

    public static R success() {
        R r = new R();
        r.setCode(HttpStatus.SC_OK);
        r.setMessage("success");
        return r;
    }

    public static R success(String msg) {
        R r = new R();
        r.setCode(HttpStatus.SC_OK);
        r.setMessage(msg);
        return r;
    }


    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    public R put(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public R put(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

}
