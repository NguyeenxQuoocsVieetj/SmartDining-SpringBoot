package org.springboot.smartdining.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_pickup_counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyPickupCounter {

    @Id
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "last_code", nullable = false)
    private Integer lastCode;
}
