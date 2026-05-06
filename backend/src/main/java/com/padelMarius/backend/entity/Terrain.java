package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "terrain",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_terrain_site_numero", columnNames = {"site_id", "numero"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terrain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false)
    private boolean actif;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
}