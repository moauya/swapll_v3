package com.swapll.gradu.model;

import com.swapll.gradu.Enum.OfferType;
import com.swapll.gradu.Enum.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class  Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OfferType type;

    @Column(name = "title")
    @NotBlank
    private String title;

    @Column(name = "description")
    @NotBlank
    private String description;

    @Column(name = "price")
    @NotNull
    private int price;

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(name = "delivery_time")


    private int deliveryTime;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentMethod paymentMethod;



    private String status = "active";

    @Column(name = "allow_swap")
    private boolean allowSwap = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();



    public void addReview(Review review) {
        reviews.add(review);
        review.setOffer(this);
    }
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
