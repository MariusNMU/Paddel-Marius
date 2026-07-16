import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminFermeturesFacadeService } from '../../services/admin-fermetures-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-fermetures',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [AdminFermeturesFacadeService],
  template: `
    <section class="page">
      <h2>Jours de fermeture</h2>

      <p>
        Cette page permet à un administrateur de créer une fermeture globale ou locale.
        Le backend bloque ensuite les disponibilités et annule les matches à venir concernés.
      </p>

      @if (facade.admin(); as admin) {
        <div class="bloc-info">
          <h3>Administrateur connecté</h3>
          <p>
            <strong>{{ admin.prenom }} {{ admin.nom }}</strong>
            — rôle {{ enumLabel(admin.roleAdministrateur) }}
          </p>

          @if (admin.roleAdministrateur === 'SITE') {
            <p><strong>Site géré :</strong> {{ facade.nomSiteSelectionne() }}</p>
          }
        </div>
      }

      <div class="bloc-info">
        <h3>Règles métier</h3>

        <ul>
          <li>
            <strong>Administrateur global :</strong>
            fermeture de tous les sites ou d’un site sélectionné.
          </li>
          <li>
            <strong>Administrateur de site :</strong>
            fermeture locale de son propre site uniquement.
          </li>
          <li>Les matches à venir sur les terrains concernés sont annulés.</li>
          <li>Les disponibilités du jour fermé deviennent indisponibles.</li>
        </ul>
      </div>

      <form (ngSubmit)="creerFermeture()">
        <label for="dateFermeture">Date de fermeture</label>
        <input
          id="dateFermeture"
          name="dateFermeture"
          type="date"
          [ngModel]="facade.dateFermeture()"
          (ngModelChange)="facade.modifierDateFermeture($event)"
          required
        />

        @if (facade.estAdminGlobal()) {
          <label for="portee">Portée</label>
          <select
            id="portee"
            name="portee"
            [ngModel]="facade.portee()"
            (ngModelChange)="facade.modifierPortee($event)"
          >
            <option value="">Choisir une portée</option>
            <option value="GLOBALE">Globale — tous les sites</option>
            <option value="LOCALE">Locale — un site précis</option>
          </select>
        } @else if (facade.admin()) {
          <div class="bloc-info">
            <p><strong>Portée :</strong> Locale</p>
            <p><strong>Site :</strong> {{ facade.nomSiteSelectionne() }}</p>
          </div>
        }

        @if (facade.portee() === 'LOCALE' && facade.estAdminGlobal()) {
          <label for="siteId">Site concerné</label>
          <select
            id="siteId"
            name="siteId"
            [ngModel]="facade.siteId()"
            (ngModelChange)="facade.modifierSiteId($event)"
            [disabled]="facade.chargementSites() || facade.sites().length === 0"
          >
            <option *ngFor="let site of facade.sites()" [ngValue]="site.siteId">
              {{ site.nom }} ({{ site.siteId }})
            </option>
          </select>
        }

        <label for="motif">Motif</label>
        <input
          id="motif"
          name="motif"
          type="text"
          [ngModel]="facade.motif()"
          (ngModelChange)="facade.modifierMotif($event)"
          maxlength="255"
        />

        <div class="bloc-info">
          <h3>Résumé avant validation</h3>

          <p>
            <strong>Date :</strong>
            {{ facade.dateFermeture() || 'Non renseignée' }}
          </p>
          <p>
            <strong>Portée :</strong>
            {{ enumLabel(facade.portee()) }}
          </p>

          @if (facade.portee() === 'LOCALE') {
            <p><strong>Site :</strong> {{ facade.nomSiteSelectionne() }}</p>
          }

          <p>
            <strong>Motif :</strong>
            {{ facade.motif() || 'Aucun motif renseigné' }}
          </p>
        </div>

        <button
          type="submit"
          [disabled]="
            facade.chargementCreation()
            || facade.chargementSites()
            || !facade.admin()
          "
        >
          {{ facade.chargementCreation() ? 'Création...' : 'Créer la fermeture' }}
        </button>
      </form>

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (facade.fermetureCreee(); as fermetureCreee) {
        <div class="resultat">
          <h3>Fermeture créée avec succès</h3>

          <div class="resume-grid">
            <p><strong>ID fermeture</strong><br>{{ fermetureCreee.fermetureId }}</p>
            <p><strong>Date</strong><br>{{ fermetureCreee.dateFermeture }}</p>
            <p><strong>Portée</strong><br>{{ enumLabel(fermetureCreee.portee) }}</p>
            <p><strong>Site</strong><br>{{ fermetureCreee.nomSite || 'Tous les sites' }}</p>
            <p><strong>Matches annulés</strong><br>{{ fermetureCreee.nombreMatchesAnnules }}</p>
            <p>
              <strong>Remboursements crédités</strong><br>
              {{ fermetureCreee.nombreRemboursementsCredites }}
            </p>
            <p>
              <strong>Montant total remboursé</strong><br>
              {{ fermetureCreee.montantTotalRembourse | number:'1.2-2' }} €
            </p>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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
export class AdminFermeturesComponent implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade: AdminFermeturesFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  creerFermeture(): void {
    this.facade.creerFermeture();
  }
}
