package com.yh.trade.common.enmus;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * api请求基本信息
 */
public interface BaseRequestApi {

    /**
     * 获取request 请求方式
     */
    RequestMethod getRequestMethod();

    HttpContentType getContentType();


    default Boolean isGet(BaseRequestApi baseRequestApi) {
        return RequestMethod.GET.equals(baseRequestApi.getRequestMethod());
    }

    default Boolean isPost(BaseRequestApi baseRequestApi) {
        return RequestMethod.POST.equals(baseRequestApi.getRequestMethod());
    }

}
