package com.ighost.demo.model;

import java.util.List;

public class PagedResult<T> {
    private List<T> content;
    private int total;

    public PagedResult(List<T> content, int total) {
        this.content = content;
        this.total = total;
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotal() {
        return total;
    }
}
