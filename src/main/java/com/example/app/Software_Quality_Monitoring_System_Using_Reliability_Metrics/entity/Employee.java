package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Double hourlyRate; // Погодинна оплата працівника BigDecimal

    @Enumerated(EnumType.STRING)  // Правильне використання Enum
    @Column(nullable = false)
    private Role role;

    @Column(name = "date_joined")
    private LocalDate dateJoined;

}
