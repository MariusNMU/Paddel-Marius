package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_source_id", nullable = false)
    private PadelMatch matchSource;

    @Column(name = "type_penalite", nullable = false, length = 50)
    private String typePenalite;

    @Column(nullable = false, length = 255)
    private String motif;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_penalite", nullable = false, length = 20)
    private StatutPenalite statutPenalite;
}