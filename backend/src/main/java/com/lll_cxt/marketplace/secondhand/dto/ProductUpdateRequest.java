package com.lll_cxt.marketplace.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品更新请求DTO - 只包含可修改的字段
 */
public class ProductUpdateRequest {

    private String description;

    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;

    private String contact;

    private Integer quantity = 1;

    private String images;

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}