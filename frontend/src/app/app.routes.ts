import { Routes } from '@angular/router';
import { AccueilComponent } from './pages/accueil/accueil.component';
import { JoueurAuthComponent } from './pages/joueur-auth/joueur-auth.component';
import { DisponibilitesComponent } from './pages/disponibilites/disponibilites.component';
import { CreerMatchComponent } from './pages/creer-match/creer-match.component';
import { MatchesPublicsComponent } from './pages/matches-publics/matches-publics.component';
import { MesReservationsComponent } from './pages/mes-reservations/mes-reservations.component';
import { MesDettesComponent } from './pages/mes-dettes/mes-dettes.component';
import { HistoriqueTransactionsComponent } from './pages/historique-transactions/historique-transactions.component';
import { AdminLoginComponent } from './pages/admin-login/admin-login.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { AdminTraitementVeilleComponent } from './pages/admin-traitement-veille/admin-traitement-veille.component';
import { AdminStatistiquesComponent } from './pages/admin-statistiques/admin-statistiques.component';
import { InscriptionJoueurComponent } from './pages/inscription-joueur/inscription-joueur.component';
import { AdminFermeturesComponent } from './pages/admin-fermetures/admin-fermetures.component';
import { MonSoldeComponent } from './pages/mon-solde/mon-solde.component';
import { AdminMembresComponent } from './pages/admin-membres/admin-membres.component';
import { InvitationsRecuesComponent } from './pages/invitations-recues/invitations-recues.component';
import { joueurGuard } from './guards/joueur.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'accueil', pathMatch: 'full' },
  { path: 'accueil', component: AccueilComponent },

  { path: 'joueur', component: JoueurAuthComponent },
  { path: 'inscription-joueur', component: InscriptionJoueurComponent },

  {
    path: 'joueur/disponibilites',
    component: DisponibilitesComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/creer-match',
    component: CreerMatchComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/matches-publics',
    component: MatchesPublicsComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mes-reservations',
    component: MesReservationsComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mes-dettes',
    component: MesDettesComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/historique-transactions',
    component: HistoriqueTransactionsComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mon-solde',
    component: MonSoldeComponent,
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/invitations-recues',
    component: InvitationsRecuesComponent,
    canActivate: [joueurGuard]
  },

  { path: 'admin/login', component: AdminLoginComponent },

  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [adminGuard]
  },
  {
    path: 'admin/traitement-veille',
    component: AdminTraitementVeilleComponent,
    canActivate: [adminGuard]
  },
  {
    path: 'admin/fermetures',
    component: AdminFermeturesComponent,
    canActivate: [adminGuard]
  },
  {
    path: 'admin/statistiques',
    component: AdminStatistiquesComponent,
    canActivate: [adminGuard]
  },
  {
    path: 'admin/membres',
    component: AdminMembresComponent,
    canActivate: [adminGuard]
  },

  { path: '**', redirectTo: 'accueil' }
];
