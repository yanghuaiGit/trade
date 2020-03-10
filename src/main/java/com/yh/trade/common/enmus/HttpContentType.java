package com.yh.trade.common.enmus;

/**
 * http 请求头 Content-Type 枚举
 *
 * @author yanghuai
 */

public enum HttpContentType implements BaseEnum {
    /**
     * application/json枚举
     */
    APPLICATION_JSON(1, "application/json");

    private Integer code;
    private String desc;

    HttpContentType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
