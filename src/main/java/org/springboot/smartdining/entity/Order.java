package org.springboot.smartdining.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springboot.smartdining.enums.OrderStatus;
import org.springboot.smartdining.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "cashier_id")
    private Long cashierId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 15)
    private String customerPhone;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "pickup_code")
    private Integer pickupCode;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
