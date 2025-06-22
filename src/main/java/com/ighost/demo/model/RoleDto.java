package com.ighost.demo.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Role 的資料傳輸物件，已改為 Record 型別。
 * Record 提供了一種簡潔的方式來建立不可變的資料載體。
 */
public record RoleDto(String id, String name, List<FunctionDto> functions) {
    
    /**
     * 緊湊建構子 (Compact Constructor)，用於在主建構子執行前進行參數驗證或轉換。
     * 這裡我們確保即使傳入的 functions 列表是 null，也會被初始化為一個空的 ArrayList，
     * 避免了後續操作中可能出現的 NullPointerException。
     */
    public RoleDto {
        if (functions == null) {
            functions = new ArrayList<>();
        }
    }
}