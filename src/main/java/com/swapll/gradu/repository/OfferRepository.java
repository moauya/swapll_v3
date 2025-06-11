package com.swapll.gradu.repository;

import com.swapll.gradu.Enum.PaymentMethod;
import com.swapll.gradu.model.Offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {
    List<Offer> findByCategoryId(int categoryId);
    List<Offer> findByOwnerId(int ownerId);

    boolean existsByOwner_IdAndId(int userId, int offerId);


    @Query("""
    SELECT o FROM Offer o
    WHERE (:keyword IS NULL OR
           LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(o.type) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryId IS NULL OR o.category.id = :categoryId)
      AND (:minPrice IS NULL OR o.price >= :minPrice)
      AND (:maxPrice IS NULL OR o.price <= :maxPrice)
      AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod)
""")
    List<Offer> search(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice, @Param("paymentMethod") PaymentMethod paymentMethod);




    @Query("SELECT o FROM Offer o LEFT JOIN o.reviews r GROUP BY o.id ORDER BY AVG(r.rating) DESC")
    List<Offer> findTopRatedOffers();


    List<Offer> findTop10ByOrderByCreatedAtDesc();

}

