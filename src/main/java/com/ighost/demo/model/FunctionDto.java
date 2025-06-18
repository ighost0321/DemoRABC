package com.ighost.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionDto {
    private Integer id;
    private String code;
    private String name;
    private String url;
    private Integer groupId;
    private String groupName;   // 這是 function_group.name
}
