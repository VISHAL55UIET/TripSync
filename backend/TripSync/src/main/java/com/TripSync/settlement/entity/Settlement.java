package com.TripSync.settlement.entity;

import com.TripSync.common.entity.BaseEntity;
import com.TripSync.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.TripSync.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="settlements")
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "trip_id",nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "payer_id",nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "payee_id",nullable = false)
    private User payee;

    @Column(nullable = false,precision = 12,scale = 2)
    private BigDecimal amount;

    @Column(name = "settled_at",nullable = false)
    private LocalDateTime settleAt;

    @Column(length = 500)
    private String note;
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;
}
