import { Routes } from '@angular/router';
import { adminGlobalGuard } from './guards/admin-global.guard';
import { adminGuard } from './guards/admin.guard';
import { joueurGuard } from './guards/joueur.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'accueil', pathMatch: 'full' },
  {
    path: 'accueil',
    loadComponent: () =>
      import('./pages/accueil/accueil.component')
        .then(component => component.AccueilComponent)
  },
  {
    path: 'joueur',
    loadComponent: () =>
      import('./pages/joueur-auth/joueur-auth.component')
        .then(component => component.JoueurAuthComponent)
  },
  {
    path: 'inscription-joueur',
    loadComponent: () =>
      import('./pages/inscription-joueur/inscription-joueur.component')
        .then(component => component.InscriptionJoueurComponent)
  },
  {
    path: 'joueur/disponibilites',
    loadComponent: () =>
      import('./pages/disponibilites/disponibilites.component')
        .then(component => component.DisponibilitesComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/creer-match',
    loadComponent: () =>
      import('./pages/creer-match/creer-match.component')
        .then(component => component.CreerMatchComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/matches-publics',
    loadComponent: () =>
      import('./pages/matches-publics/matches-publics.component')
        .then(component => component.MatchesPublicsComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mes-reservations',
    loadComponent: () =>
      import('./pages/mes-reservations/mes-reservations.component')
        .then(component => component.MesReservationsComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mes-dettes',
    loadComponent: () =>
      import('./pages/mes-dettes/mes-dettes.component')
        .then(component => component.MesDettesComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/historique-transactions',
    loadComponent: () =>
      import(
        './pages/historique-transactions/historique-transactions.component'
        )
        .then(component => component.HistoriqueTransactionsComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/mon-solde',
    loadComponent: () =>
      import('./pages/mon-solde/mon-solde.component')
        .then(component => component.MonSoldeComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'joueur/invitations-recues',
    loadComponent: () =>
      import('./pages/invitations-recues/invitations-recues.component')
        .then(component => component.InvitationsRecuesComponent),
    canActivate: [joueurGuard]
  },
  {
    path: 'admin/login',
    loadComponent: () =>
      import('./pages/admin-login/admin-login.component')
        .then(component => component.AdminLoginComponent)
  },
  {
    path: 'admin/dashboard',
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard.component')
        .then(component => component.AdminDashboardComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'admin/traitement-veille',
    loadComponent: () =>
      import(
        './pages/admin-traitement-veille/admin-traitement-veille.component'
        )
        .then(component => component.AdminTraitementVeilleComponent),
    canActivate: [adminGlobalGuard]
  },
  {
    path: 'admin/fermetures',
    loadComponent: () =>
      import('./pages/admin-fermetures/admin-fermetures.component')
        .then(component => component.AdminFermeturesComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'admin/statistiques',
    loadComponent: () =>
      import('./pages/admin-statistiques/admin-statistiques.component')
        .then(component => component.AdminStatistiquesComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'admin/etat-operationnel',
    loadComponent: () =>
      import(
        './pages/admin-etat-operationnel/admin-etat-operationnel.component'
        )
        .then(
          component =>
            component.AdminEtatOperationnelComponent
        ),
    canActivate: [adminGuard]
  },
  {
    path: 'admin/membres',
    loadComponent: () =>
      import('./pages/admin-membres/admin-membres.component')
        .then(component => component.AdminMembresComponent),
    canActivate: [adminGuard]
  },
  { path: '**', redirectTo: 'accueil' }
];
