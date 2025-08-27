package com.example.webstore.dto;

import java.util.List;

public class PageResult<T> {
    public final List<T> items;
    public final int page;
    public final int size;
    public final long total;
    public final int totalPages;

    public PageResult(List<T> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.max(1, Math.ceil(total / (double) size));
    }

    public boolean hasPrev() { return page > 1; }
    public boolean hasNext() { return page < totalPages; }
}
