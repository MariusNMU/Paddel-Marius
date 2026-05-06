package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "participation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_participation_match_membre", columnNames = {"match_id", "membre_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private PadelMatch match;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_participation", nullable = false, length = 20)
    private RoleParticipation roleParticipation;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_entree", nullable = false, length = 30)
    private ModeEntreeParticipation modeEntree;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_participation", nullable = false, length = 30)
    private StatutParticipation statutParticipation;

    @Column(name = "date_affectation", nullable = false)
    private LocalDateTime dateAffectation;

    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    @Column(name = "date_liberation")
    private LocalDateTime dateLiberation;
}