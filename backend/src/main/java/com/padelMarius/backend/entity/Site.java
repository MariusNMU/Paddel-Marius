package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "site",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_site_code", columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(nullable = false)
    private boolean actif;
}