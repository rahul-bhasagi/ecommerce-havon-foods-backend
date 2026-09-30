package com.havonfoods.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.havonfoods.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{
	
	@Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.variant v WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.variant v WHERE c.sessionToken = :sessionToken")
    Optional<Cart> findBySessionTokenWithItems(@Param("sessionToken") String sessionToken);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionToken(String sessionToken);
}
