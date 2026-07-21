import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatchPublicResponse } from '../../models/match-public.model';
import { MatchesPublicsFacadeService } from '../../services/matches-publics-facade.service';

@Component({
  selector: 'app-matches-publics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [MatchesPublicsFacadeService],
  template: `
    <section class="page">
      <h2>Rejoindre un match public</h2>

      <p>
        Retrouve les matches publics disponibles et rejoins une place
        en payant la participation calculée par le backend.
        Premier payé = premier servi.
      </p>

      <div class="bloc-info">
        <h3>Règles métier</h3>

        <ul>
          <li>
            Un match public peut être rejoint par les joueurs
            disponibles.
          </li>
          <li>
            Un match contient maximum
            {{ facade.parametresMetier()?.nombreJoueursMaximum }}
            joueurs.
          </li>
          <li>
            La place est validée uniquement après paiement.
          </li>
          <li>
            Le paiement débite ton solde crédit du montant de
            participation retourné par le backend.
          </li>
        </ul>
      </div>

      @if (!facade.joueurConnecte()) {
        <p class="erreur">
          Connecte-toi d'abord comme joueur pour rejoindre un match
          public.
        </p>
      }

      @if (facade.joueurConnecte(); as joueur) {
        <div class="bloc-info">
          <h3>Joueur connecté</h3>
          <p>
            <strong>Matricule :</strong>
            {{ joueur.matricule }}
          </p>
          <p>
            <strong>Nom :</strong>
            {{ joueur.nom }} {{ joueur.prenom }}
          </p>
        </div>
      }

      <div class="bloc-info">
        <h3>Choix rapide de la date</h3>

        <div class="jours-rapides">
          <button
            *ngFor="let jour of facade.joursRapides()"
            type="button"
            (click)="selectionnerJour(jour.date)"
            [class.selectionne]="facade.date() === jour.date"
          >
            <span>{{ jour.libelle }}</span>
            <strong>{{ jour.date }}</strong>
          </button>
        </div>
      </div>

      <form (ngSubmit)="rechercherMatchesPublics()">
        <label for="siteId">Site</label>
        <select
          id="siteId"
          name="siteId"
          [ngModel]="facade.siteId()"
          (ngModelChange)="facade.modifierSiteId($event)"
          [disabled]="
            facade.chargementSites()
            || facade.sites().length === 0
          "
        >
          <option
            *ngFor="let site of facade.sites()"
            [ngValue]="site.siteId"
          >
            {{ site.nom }} ({{ site.siteId }})
          </option>
        </select>

        <label for="date">Date</label>
        <input
          id="date"
          name="date"
          type="date"
          [ngModel]="facade.date()"
          (ngModelChange)="facade.modifierDate($event)"
          required
        />

        <button
          type="submit"
          [disabled]="
            facade.chargementRecherche()
            || facade.chargementSites()
            || facade.sites().length === 0
          "
        >
          {{
            facade.chargementRecherche()
              ? 'Recherche...'
              : 'Rechercher les matches publics'
          }}
        </button>
      </form>

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (facade.messageSucces()) {
        <div class="resultat">
          <h3>Inscription confirmée</h3>
          <p>{{ facade.messageSucces() }}</p>

          @if (facade.dernierPaiement(); as dernierPaiement) {
            <div class="resume-grid">
              <p>
                <strong>Match</strong><br>
                {{ dernierPaiement.matchId }}
              </p>
              <p>
                <strong>Participation</strong><br>
                {{ dernierPaiement.participationId }}
              </p>
              <p>
                <strong>Paiement</strong><br>
                {{ dernierPaiement.paiementId }}
              </p>
              <p>
                <strong>Montant payé</strong><br>
                {{
                  dernierPaiement.montantPaye
                    | number:'1.2-2'
                }} €
              </p>
              <p>
                <strong>Solde restant</strong><br>
                {{
                  dernierPaiement.soldeRestant
                    | number:'1.2-2'
                }} €
              </p>
            </div>
          }
        </div>
      }

      @if (
        facade.matches().length === 0
        && facade.rechercheEffectuee()
        && !facade.chargementRecherche()
      ) {
        <p>
          Aucun match public disponible pour cette date et ce site.
        </p>
      }

      @if (facade.matches().length > 0) {
        <div class="matches-grid">
          <article
            *ngFor="let match of facade.matches()"
            class="match-card"
          >
            <h3>
              {{ match.nomSite }} — Terrain
              {{ match.numeroTerrain }}
            </h3>

            <p>
              <strong>Début :</strong>
              {{ match.dateHeureDebut }}
            </p>
            <p>
              <strong>Fin :</strong>
              {{ match.dateHeureFin }}
            </p>
            <p>
              <strong>Participants :</strong>
              {{ match.nombreParticipantsActifs }} /
              {{
                facade.parametresMetier()
                  ?.nombreJoueursMaximum
              }}
            </p>
            <p>
              <strong>Places disponibles :</strong>
              {{ match.placesDisponibles }}
            </p>
            <p>
              <strong>Prix total :</strong>
              {{ match.prixTotal | number:'1.2-2' }} €
            </p>
            <p>
              <strong>Ta place :</strong>
              {{
                match.montantParticipation
                  | number:'1.2-2'
              }} €
            </p>

            @if (match.peutRejoindre) {
              <button
                type="button"
                [disabled]="
                  facade.chargementPaiement()
                  || !facade.joueurConnecte()
                "
                (click)="rejoindreEtPayer(match)"
              >
                {{
                  facade.chargementPaiement()
                    ? 'Paiement...'
                    : (
                        'Rejoindre et payer '
                        + (
                          match.montantParticipation
                            | number:'1.2-2'
                        )
                        + ' €'
                      )
                }}
              </button>
            } @else {
              <p class="action-indisponible">
                {{
                  match.motifNonEligibilite
                    || 'Ce match ne peut pas être rejoint.'
                }}
              </p>
            }
          </article>
        </div>
      }
    </section>
  `,
  styles: [`
    .jours-rapides {
      display: grid;
      grid-template-columns: repeat(7, minmax(0, 1fr));
      gap: 8px;
    }

    .jours-rapides button {
      width: 100%;
      padding: 8px 6px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      font-size: 13px;
      line-height: 1.15;
    }

    .jours-rapides button.selectionne {
      background: #dbeafe;
      color: #001f5c;
      outline: 2px solid #003b95;
    }

    @media (max-width: 1100px) {
      .jours-rapides {
        grid-template-columns:
          repeat(auto-fit, minmax(100px, 1fr));
      }
    }

    .matches-grid {
      display: grid;
      grid-template-columns:
        repeat(auto-fit, minmax(260px, 1fr));
      gap: 16px;
      margin-top: 18px;
    }

    .match-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .match-card h3 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .match-card p {
      margin: 8px 0;
    }

    .match-card button {
      margin-top: 12px;
    }

    .action-indisponible {
      margin-top: 12px;
      padding: 10px;
      border-radius: 8px;
      background: #f1f5f9;
      color: #475569;
      font-weight: 600;
    }

    .resume-grid {
      display: grid;
      grid-template-columns:
        repeat(auto-fit, minmax(160px, 1fr));
      gap: 12px;
      margin-top: 16px;
    }

    .resume-grid p {
      margin: 0;
      padding: 12px;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
    }
  `]
})
export class MatchesPublicsComponent implements OnInit {
  constructor(
    readonly facade: MatchesPublicsFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  selectionnerJour(date: string): void {
    this.facade.selectionnerJour(date);
  }

  rechercherMatchesPublics(): void {
    this.facade.rechercherMatchesPublics();
  }

  rejoindreEtPayer(match: MatchPublicResponse): void {
    this.facade.rejoindreEtPayer(match);
  }
}
