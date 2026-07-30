import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AdminFermeturesFacadeService } from '../../services/admin-fermetures-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-fermetures',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [AdminFermeturesFacadeService],
  template: `
    <section class="page">
      <h2>Jours de fermeture</h2>

      <p>
        Cette page permet à un administrateur de créer une fermeture globale ou locale.
        Le backend bloque ensuite les disponibilités et annule les matches à venir concernés.
      </p>

      @if (facade.admin(); as admin) {
        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Administrateur connecté</h3>
          <p>
            <strong>{{ admin.prenom }} {{ admin.nom }}</strong>
            — rôle {{ enumLabel(admin.roleAdministrateur) }}
          </p>

          @if (admin.roleAdministrateur === 'SITE') {
            <p><strong>Site géré :</strong> {{ facade.nomSiteSelectionne() }}</p>
          }
        </mat-card>
      }

      <mat-card
        appearance="outlined"
        class="bloc-info"
      >
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
      </mat-card>

      <form (ngSubmit)="creerFermeture()">
        <mat-form-field appearance="outline">
          <mat-label>Date de fermeture</mat-label>
          <input
            matInput
            id="dateFermeture"
            name="dateFermeture"
            type="date"
            [ngModel]="facade.dateFermeture()"
            (ngModelChange)="
              facade.modifierDateFermeture(
                $event
              )
            "
            required
          >
        </mat-form-field>

        @if (facade.estAdminGlobal()) {
          <mat-form-field appearance="outline">
            <mat-label>Portée</mat-label>
            <select
              matNativeControl
              id="portee"
              name="portee"
              [ngModel]="facade.portee()"
              (ngModelChange)="
                facade.modifierPortee($event)
              "
            >
              <option value="">
                Choisir une portée
              </option>
              <option value="GLOBALE">
                Globale — tous les sites
              </option>
              <option value="LOCALE">
                Locale — un site précis
              </option>
            </select>
          </mat-form-field>
        } @else if (facade.admin()) {
          <mat-card
            appearance="outlined"
            class="bloc-info"
          >
            <p><strong>Portée :</strong> Locale</p>
            <p><strong>Site :</strong> {{ facade.nomSiteSelectionne() }}</p>
          </mat-card>
        }

        @if (facade.portee() === 'LOCALE' && facade.estAdminGlobal()) {
          <mat-form-field appearance="outline">
            <mat-label>Site concerné</mat-label>
            <select
              matNativeControl
              id="siteId"
              name="siteId"
              [ngModel]="facade.siteId()"
              (ngModelChange)="
                facade.modifierSiteId($event)
              "
              [disabled]="
                facade.chargementSites()
                || facade.sites().length === 0
              "
            >
              <option
                *ngFor="let site of facade.sites()"
                [ngValue]="site.siteId"
              >
                {{ site.nom }}
              </option>
            </select>
          </mat-form-field>
        }

        <mat-form-field appearance="outline">
          <mat-label>Motif</mat-label>
          <input
            matInput
            id="motif"
            name="motif"
            type="text"
            [ngModel]="facade.motif()"
            (ngModelChange)="
              facade.modifierMotif($event)
            "
            maxlength="255"
          >
        </mat-form-field>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Résumé avant validation</h3>

          <p>
            <strong>Date :</strong>
            @if (facade.dateFermeture()) {
              {{ facade.dateFermeture() | date:'dd/MM/yyyy' }}
            } @else {
              Non renseignée
            }
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
        </mat-card>

        <button
          mat-flat-button
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
        <mat-card
          appearance="outlined"
          class="resultat"
        >
          <h3>Fermeture créée avec succès</h3>

          <div class="resume-grid">
            <p>
              <strong>Date</strong><br>
              {{ fermetureCreee.dateFermeture | date:'dd/MM/yyyy' }}
            </p>
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
        </mat-card>
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

    form mat-form-field {
      width: 100%;
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
