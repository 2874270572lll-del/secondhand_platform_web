package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.dto.ProductDTO;
import com.lll_cxt.marketplace.secondhand.dto.ProductRequest;
import com.lll_cxt.marketplace.secondhand.dto.Result;
import com.lll_cxt.marketplace.secondhand.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 发布商品
     */
    @PostMapping("/create")
    public Result<ProductDTO> createProduct(@Valid @RequestBody ProductRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ProductDTO product = productService.createProduct(userId, request);
        return Result.success("发布成功", product);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update/{id}")
    public Result<ProductDTO> updateProduct(@PathVariable Long id,
                                            @Valid @RequestBody ProductRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ProductDTO product = productService.updateProduct(id, userId, request);
        return Result.success("更新成功", product);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        productService.deleteProduct(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 下架商品
     */
    @PutMapping("/off-shelf/{id}")
    public Result<Void> offShelfProduct(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        productService.offShelfProduct(id, userId);
        return Result.success("下架成功", null);
    }

    /**
     * 获取商品列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResponse<ProductDTO>> getProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<ProductDTO> response = productService.getProductList(current, size, categoryId, userId);
        return Result.success(response);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<PageResponse<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<ProductDTO> response = productService.searchProducts(keyword, current, size, userId);
        return Result.success(response);
    }

    /**
     * 获取我的商品列表
     */
    @GetMapping("/my-products")
    public Result<PageResponse<ProductDTO>> getMyProducts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<ProductDTO> response = productService.getMyProducts(userId, current, size);
        return Result.success(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDTO> getProductDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ProductDTO product = productService.getProductDetail(id, userId);
        return Result.success(product);
    }
}
