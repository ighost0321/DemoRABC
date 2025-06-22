package com.ighost.demo.model;

/**
 * 改為使用 Java Record，自動獲得建構子、getter、equals()、hashCode() 和 toString()。
 * 這使 DTO（資料傳輸物件）的定義更加簡潔和不可變。
 */
public record FunctionDto(
    Integer id,
    String code,
    String name,
    String url,
    Integer groupId,
    String groupName
) {}