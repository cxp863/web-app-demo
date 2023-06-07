package com.hsinpeng.webapp.controller;

import com.hsinpeng.webapp.dto.TestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class OOMTestController {
    public static List<List<TestDTO>> list = new ArrayList<>();

    @RequestMapping("/add")
    public Integer add() {
        Random random = new Random(System.currentTimeMillis());
        list.add(Stream.generate( () -> new TestDTO(random.nextInt())).limit(1024*1024).collect(Collectors.toList()));
        return list.size();
    }

    @RequestMapping("/remove")
    public Integer remove() {
        if(list.size() > 0) {
            list.remove(0);
        }
        return list.size();
    }
}
