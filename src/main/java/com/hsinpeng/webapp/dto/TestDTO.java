package com.hsinpeng.webapp.dto;

import lombok.Data;

@Data
public class TestDTO {
    Integer value;

    public TestDTO(int value) {
        this.value = value;
    }
}
