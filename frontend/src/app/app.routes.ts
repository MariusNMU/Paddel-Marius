import { Routes } from '@angular/router';
import { AccueilComponent } from './pages/accueil/accueil.component';
import { JoueurAuthComponent } from './pages/joueur-auth/joueur-auth.component';
import { DisponibilitesComponent } from './pages/disponibilites/disponibilites.component';
import { CreerMatchComponent } from './pages/creer-match/creer-match.component';
import { MatchesPublicsComponent } from './pages/matches-publics/matches-publics.component';
import { MesReservationsComponent } from './pages/mes-reservations/mes-reservations.component';
import { MesDettesComponent } from './pages/mes-dettes/mes-dettes.component';
import { AdminLoginComponent } from './pages/admin-login/admin-login.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { AdminTraitementVeilleComponent } from './pages/admin-traitement-veille/admin-traitement-veille.component';
import { AdminStatistiquesComponent } from './pages/admin-statistiques/admin-statistiques.component';
import { InscriptionJoueurComponent } from './pages/inscription-joueur/inscription-joueur.component';
import { AdminFermeturesComponent } from './pages/admin-fermetures/admin-fermetures.component';

export const routes: Routes = [
  { path: '', redirectTo: 'accueil', pathMatch: 'full' },
  { path: 'accueil', component: AccueilComponent },
  { path: 'joueur', component: JoueurAuthComponent },
  { path: 'joueur/disponibilites', component: DisponibilitesComponent },
  { path: 'joueur/creer-match', component: CreerMatchComponent },
  { path: 'joueur/matches-publics', component: MatchesPublicsComponent },
  { path: 'joueur/mes-reservations', component: MesReservationsComponent },
  { path: 'joueur/mes-dettes', component: MesDettesComponent },
  { path: 'inscription-joueur', component: InscriptionJoueurComponent },
  { path: 'admin/login', component: AdminLoginComponent },
  { path: 'admin/dashboard', component: AdminDashboardComponent },
  { path: 'admin/traitement-veille', component: AdminTraitementVeilleComponent },
  { path: 'admin/fermetures', component: AdminFermeturesComponent },
  { path: 'admin/statistiques', component: AdminStatistiquesComponent },
  { path: '**', redirectTo: 'accueil' }
];
