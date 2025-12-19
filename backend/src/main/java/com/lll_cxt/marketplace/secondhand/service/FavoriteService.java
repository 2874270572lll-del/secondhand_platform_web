package com.lll_cxt.marketplace.secondhand.service;

import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.dto.ProductDTO;
import com.lll_cxt.marketplace.secondhand.entity.Favorite;
import com.lll_cxt.marketplace.secondhand.entity.Product;
import com.lll_cxt.marketplace.secondhand.exception.BusinessException;
import com.lll_cxt.marketplace.secondhand.repository.FavoriteRepository;
import com.lll_cxt.marketplace.secondhand.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务类
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository,
                           ProductService productService) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }

        // 创建收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteRepository.save(favorite);

        // 更新商品收藏数
        product.setFavoriteCount(product.getFavoriteCount() + 1);
        productRepository.save(product);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        // 查找收藏记录
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("未收藏该商品"));

        // 删除收藏
        favoriteRepository.delete(favorite);

        // 更新商品收藏数
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null && product.getFavoriteCount() > 0) {
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
        }
    }

    /**
     * 获取我的收藏列表
     */
    public PageResponse<ProductDTO> getMyFavorites(Long userId, Integer current, Integer size) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);

        List<ProductDTO> records = favoritePage.getContent().stream()
                .map(favorite -> {
                    Product product = productRepository.findById(favorite.getProductId()).orElse(null);
                    if (product != null) {
                        return productService.getProductDetail(product.getId(), userId);
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        PageResponse<ProductDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(favoritePage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }
}
