package com.lll_cxt.marketplace.secondhand.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 订单创建请求DTO
 */
public class OrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private String buyerMessage;

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBuyerMessage() {
        return buyerMessage;
    }

    public void setBuyerMessage(String buyerMessage) {
        this.buyerMessage = buyerMessage;
    }
}
