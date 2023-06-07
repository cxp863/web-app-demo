package com.hsinpeng.webapp.controller;

import com.hsinpeng.webapp.dto.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/test1")
    public String test1() {
        return "Test OKay!";
    }

    @RequestMapping("/test2")
    public ApiResponse<String> test2() {
        return ApiResponse.success("Test Okay");
    }
}
