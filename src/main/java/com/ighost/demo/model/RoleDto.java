package com.ighost.demo.model;

import java.util.ArrayList; // 1. 匯入 ArrayList
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    private String id;
    private String name;
    
    // 2. 關鍵修正：直接在這裡初始化列表
    // 這樣可以保證無論何時建立 RoleDto，functions 欄位都不是 null
    private List<FunctionDto> functions = new ArrayList<>();

}