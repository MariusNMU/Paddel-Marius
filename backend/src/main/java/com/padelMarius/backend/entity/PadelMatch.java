package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "padel_match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PadelMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "terrain_id", nullable = false)
    private Terrain terrain;

    @Column(name = "date_heure_debut", nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(name = "date_heure_fin", nullable = false)
    private LocalDateTime dateHeureFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_creation", nullable = false, length = 20)
    private ModeCreation modeCreation;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilite_courante", nullable = false, length = 20)
    private VisibiliteMatch visibiliteCourante;

    @Column(name = "prix_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_passage_public")
    private LocalDateTime datePassagePublic;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_cycle", nullable = false, length = 20)
    private EtatCycleMatch etatCycle;
}