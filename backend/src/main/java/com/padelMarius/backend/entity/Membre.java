package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    public static final BigDecimal SOLDE_INITIAL = new BigDecimal("100.00");

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

    @Builder.Default
    @Column(name = "solde_credit", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeCredit = SOLDE_INITIAL;
}