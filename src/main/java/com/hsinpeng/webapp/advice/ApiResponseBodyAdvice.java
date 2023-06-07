package com.hsinpeng.webapp.advice;

import com.hsinpeng.webapp.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<ApiResponse> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public ApiResponse beforeBodyWrite(ApiResponse body, MethodParameter returnType, MediaType selectedContentType,
                                       Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                       ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null) {
            response.getHeaders().set("x-api-response-code", Integer.toString(body.getCode()));
        }
        return body;
    }
}
