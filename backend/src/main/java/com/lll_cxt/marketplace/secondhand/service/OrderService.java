package com.lll_cxt.marketplace.secondhand.service;

import com.lll_cxt.marketplace.secondhand.dto.OrderDTO;
import com.lll_cxt.marketplace.secondhand.dto.OrderRequest;
import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.entity.Order;
import com.lll_cxt.marketplace.secondhand.entity.Product;
import com.lll_cxt.marketplace.secondhand.entity.User;
import com.lll_cxt.marketplace.secondhand.exception.BusinessException;
import com.lll_cxt.marketplace.secondhand.repository.OrderRepository;
import com.lll_cxt.marketplace.secondhand.repository.ProductRepository;
import com.lll_cxt.marketplace.secondhand.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务类
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建订单
     */
    @Transactional
    public OrderDTO createOrder(Long buyerId, OrderRequest request) {
        // 查询商品
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查商品状态
        if (!"ON_SALE".equals(product.getStatus())) {
            throw new BusinessException("商品已下架或已售出");
        }

        // 检查库存
        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            throw new BusinessException("商品库存不足");
        }

        // 检查是否是自己的商品
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setPrice(product.getPrice());
        order.setStatus("PENDING");
        order.setBuyerMessage(request.getBuyerMessage());

        Order savedOrder = orderRepository.save(order);

        // 减少商品库存
        product.setQuantity(product.getQuantity() - 1);

        // 如果库存为0，更新商品状态为已售出
        if (product.getQuantity() <= 0) {
            product.setStatus("SOLD");
        }
        productRepository.save(product);

        return convertToDTO(savedOrder);
    }

    /**
     * 获取我购买的订单列表
     */
    public PageResponse<OrderDTO> getMyBuyOrders(Long buyerId, Integer current, Integer size) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage = orderRepository.findByBuyerId(buyerId, pageable);

        List<OrderDTO> records = orderPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResponse<OrderDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(orderPage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }

    /**
     * 获取我卖出的订单列表
     */
    public PageResponse<OrderDTO> getMySellOrders(Long sellerId, Integer current, Integer size) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage = orderRepository.findBySellerId(sellerId, pageable);

        List<OrderDTO> records = orderPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageResponse<OrderDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(orderPage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Long userId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 检查权限
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        return convertToDTO(savedOrder);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        String orderNo = "ORD" + timestamp + random;

        // 检查订单号是否已存在
        if (orderRepository.existsByOrderNo(orderNo)) {
            return generateOrderNo();
        }

        return orderNo;
    }

    /**
     * 转换为DTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);

        // 设置商品信息
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null) {
            dto.setProductTitle(product.getTitle());
            // 获取第一张图片
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String images = product.getImages();
                if (images.startsWith("[") && images.contains("\"")) {
                    int start = images.indexOf("\"") + 1;
                    int end = images.indexOf("\"", start);
                    if (end > start) {
                        dto.setProductImage(images.substring(start, end));
                    }
                } else {
                    dto.setProductImage(images);
                }
            }
        }

        // 设置买家信息
        User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
        if (buyer != null) {
            dto.setBuyerName(buyer.getNickname());
        }

        // 设置卖家信息
        User seller = userRepository.findById(order.getSellerId()).orElse(null);
        if (seller != null) {
            dto.setSellerName(seller.getNickname());
        }

        return dto;
    }
}
