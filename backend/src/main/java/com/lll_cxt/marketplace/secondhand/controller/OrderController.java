package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.OrderDTO;
import com.lll_cxt.marketplace.secondhand.dto.OrderRequest;
import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.dto.Result;
import com.lll_cxt.marketplace.secondhand.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderDTO> createOrder(@Valid @RequestBody OrderRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderDTO order = orderService.createOrder(userId, request);
        return Result.success("下单成功", order);
    }

    /**
     * 获取我购买的订单列表
     */
    @GetMapping("/my-buy")
    public Result<PageResponse<OrderDTO>> getMyBuyOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<OrderDTO> response = orderService.getMyBuyOrders(userId, current, size);
        return Result.success(response);
    }

    /**
     * 获取我卖出的订单列表
     */
    @GetMapping("/my-sell")
    public Result<PageResponse<OrderDTO>> getMySellOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<OrderDTO> response = orderService.getMySellOrders(userId, current, size);
        return Result.success(response);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/update-status/{id}")
    public Result<OrderDTO> updateOrderStatus(@PathVariable Long id,
                                               @RequestParam String status,
                                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderDTO order = orderService.updateOrderStatus(id, userId, status);
        return Result.success("操作成功", order);
    }
}
