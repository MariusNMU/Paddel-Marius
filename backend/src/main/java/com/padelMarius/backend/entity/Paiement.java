package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "paiement",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_paiement_participation", columnNames = "participation_id"),
                @UniqueConstraint(name = "uk_paiement_dette", columnNames = "dette_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_paiement", nullable = false, length = 30)
    private NaturePaiement naturePaiement;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_heure_paiement", nullable = false)
    private LocalDateTime dateHeurePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false, length = 20)
    private StatutPaiement statutPaiement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", unique = true)
    private Participation participation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dette_id", unique = true)
    private Dette dette;
}