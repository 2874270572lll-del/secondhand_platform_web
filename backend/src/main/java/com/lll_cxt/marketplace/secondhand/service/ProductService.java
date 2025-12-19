package com.lll_cxt.marketplace.secondhand.service;

import com.lll_cxt.marketplace.secondhand.dto.PageResponse;
import com.lll_cxt.marketplace.secondhand.dto.ProductDTO;
import com.lll_cxt.marketplace.secondhand.dto.ProductRequest;
import com.lll_cxt.marketplace.secondhand.entity.Category;
import com.lll_cxt.marketplace.secondhand.entity.Product;
import com.lll_cxt.marketplace.secondhand.entity.User;
import com.lll_cxt.marketplace.secondhand.exception.BusinessException;
import com.lll_cxt.marketplace.secondhand.repository.CategoryRepository;
import com.lll_cxt.marketplace.secondhand.repository.FavoriteRepository;
import com.lll_cxt.marketplace.secondhand.repository.ProductRepository;
import com.lll_cxt.marketplace.secondhand.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务类
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FavoriteRepository favoriteRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository,
                          CategoryRepository categoryRepository, FavoriteRepository favoriteRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 发布商品
     */
    @Transactional
    public ProductDTO createProduct(Long sellerId, ProductRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setSellerId(sellerId);
        product.setStatus("ON_SALE");
        product.setViewCount(0);
        product.setFavoriteCount(0);

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct, sellerId);
    }

    /**
     * 更新商品
     */
    @Transactional
    public ProductDTO updateProduct(Long productId, Long userId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查权限
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权限修改此商品");
        }

        BeanUtils.copyProperties(request, product);
        product.setId(productId);
        product.setSellerId(userId);

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct, userId);
    }

    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查权限
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权限删除此商品");
        }

        productRepository.deleteById(productId);
    }

    /**
     * 下架商品
     */
    @Transactional
    public void offShelfProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查权限
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此商品");
        }

        product.setStatus("OFF_SHELF");
        productRepository.save(product);
    }

    /**
     * 获取商品列表（分页）
     */
    public PageResponse<ProductDTO> getProductList(Integer current, Integer size, Long categoryId, Long userId) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> productPage;
        if (categoryId != null && categoryId > 0) {
            productPage = productRepository.findByStatusAndCategoryId("ON_SALE", categoryId, pageable);
        } else {
            productPage = productRepository.findByStatus("ON_SALE", pageable);
        }

        List<ProductDTO> records = productPage.getContent().stream()
                .map(product -> convertToDTO(product, userId))
                .collect(Collectors.toList());

        PageResponse<ProductDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(productPage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }

    /**
     * 搜索商品
     */
    public PageResponse<ProductDTO> searchProducts(String keyword, Integer current, Integer size, Long userId) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> productPage = productRepository.searchByKeyword("ON_SALE", keyword, pageable);

        List<ProductDTO> records = productPage.getContent().stream()
                .map(product -> convertToDTO(product, userId))
                .collect(Collectors.toList());

        PageResponse<ProductDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(productPage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }

    /**
     * 获取我的商品列表
     */
    public PageResponse<ProductDTO> getMyProducts(Long userId, Integer current, Integer size) {
        Pageable pageable = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> productPage = productRepository.findBySellerId(userId, pageable);

        List<ProductDTO> records = productPage.getContent().stream()
                .map(product -> convertToDTO(product, userId))
                .collect(Collectors.toList());

        PageResponse<ProductDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(productPage.getTotalElements());
        response.setCurrent(current);
        response.setSize(size);

        return response;
    }

    /**
     * 获取商品详情
     */
    @Transactional
    public ProductDTO getProductDetail(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 增加浏览次数
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return convertToDTO(product, userId);
    }

    /**
     * 转换为DTO
     */
    private ProductDTO convertToDTO(Product product, Long currentUserId) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);

        // 设置卖家信息
        User seller = userRepository.findById(product.getSellerId()).orElse(null);
        if (seller != null) {
            dto.setSellerName(seller.getNickname());
            dto.setSellerAvatar(seller.getAvatar());
        }

        // 设置分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }

        // 设置是否已收藏
        if (currentUserId != null) {
            boolean isFavorite = favoriteRepository.existsByUserIdAndProductId(currentUserId, product.getId());
            dto.setIsFavorite(isFavorite);
        } else {
            dto.setIsFavorite(false);
        }

        return dto;
    }
}
