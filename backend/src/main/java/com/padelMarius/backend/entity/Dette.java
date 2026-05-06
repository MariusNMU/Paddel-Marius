package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "dette",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dette_match", columnNames = "match_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private PadelMatch match;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membre_responsable_id", nullable = false)
    private Membre membreResponsable;

    @Column(name = "montant_initial", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantInitial;

    @Column(name = "montant_restant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantRestant;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_reglement")
    private LocalDateTime dateReglement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_dette", nullable = false, length = 20)
    private StatutDette statutDette;
}