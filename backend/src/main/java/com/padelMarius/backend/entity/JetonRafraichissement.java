package com.padelMarius.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jeton_rafraichissement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JetonRafraichissement {

    @Id
    @Column(name = "identifiant", nullable = false, length = 36)
    private String identifiant;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(nullable = false, length = 150)
    private String sujet;

    @Column(name = "type_utilisateur", nullable = false, length = 20)
    private String typeUtilisateur;

    @Column(nullable = false)
    private boolean revoque;

    @Column(name = "date_revocation")
    private LocalDateTime dateRevocation;

    public JetonRafraichissement(
            String identifiant,
            LocalDateTime dateExpiration,
            String sujet,
            String typeUtilisateur
    ) {
        this.identifiant = identifiant;
        this.dateExpiration = dateExpiration;
        this.sujet = sujet;
        this.typeUtilisateur = typeUtilisateur;
        this.revoque = false;
        this.dateRevocation = null;
    }

    public boolean estActif(LocalDateTime maintenant) {
        return !revoque
                && dateExpiration != null
                && dateExpiration.isAfter(maintenant);
    }

    public boolean correspondA(
            String sujetAttendu,
            String typeUtilisateurAttendu
    ) {
        return sujet != null
                && sujet.equals(sujetAttendu)
                && typeUtilisateur != null
                && typeUtilisateur.equals(typeUtilisateurAttendu);
    }

    public void revoquer(LocalDateTime maintenant) {
        this.revoque = true;
        this.dateRevocation = maintenant;
    }
}
