package com.lll_cxt.marketplace.secondhand.repository;

import com.lll_cxt.marketplace.secondhand.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(p.title LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("status") String status,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
}
