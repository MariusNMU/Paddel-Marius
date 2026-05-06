package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "membre",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_membre_matricule", columnNames = "matricule")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_membre", nullable = false, length = 20)
    private CategorieMembre categorieMembre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_rattachement_id")
    private Site siteRattachement;

    @Column(nullable = false)
    private boolean actif;
}

