package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {


    @Query("SELECT ci from CartItem ci where ci.cart.cartId=?1 and ci.product.productId=?2")
    CartItem findCartItemByProductIdAndCartId(long cartId, Long productId);

    @Modifying
    @Query("DELETE from CartItem c where c.product.productId=?1 and c.cart.cartId=?2")
        void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);
}
