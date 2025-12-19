package com.lll_cxt.marketplace.secondhand.service;

import com.lll_cxt.marketplace.secondhand.entity.Category;
import com.lll_cxt.marketplace.secondhand.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务类
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }
}
