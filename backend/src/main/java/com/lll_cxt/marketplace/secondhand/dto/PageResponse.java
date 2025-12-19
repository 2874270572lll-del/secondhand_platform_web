package com.lll_cxt.marketplace.secondhand.dto;

import java.util.List;

/**
 * 分页响应DTO
 */
public class PageResponse<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;

    // Getters and Setters
    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
