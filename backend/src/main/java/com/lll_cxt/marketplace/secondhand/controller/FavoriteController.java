package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.dto.ProductDTO;
import com.lll_cxt.marketplace.secondhand.dto.Result;
import com.lll_cxt.marketplace.secondhand.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add/{productId}")
    public Result<Void> addFavorite(@PathVariable Long productId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        favoriteService.addFavorite(userId, productId);
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove/{productId}")
    public Result<Void> removeFavorite(@PathVariable Long productId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        favoriteService.removeFavorite(userId, productId);
        return Result.success("取消收藏成功", null);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my-favorites")
    public Result<PageResponse<ProductDTO>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResponse<ProductDTO> response = favoriteService.getMyFavorites(userId, current, size);
        return Result.success(response);
    }
}
