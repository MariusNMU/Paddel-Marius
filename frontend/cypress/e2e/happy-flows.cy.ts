export {};

function dateIsoDansJours(decalageJours: number): string {
  const date = new Date();
  date.setDate(date.getDate() + decalageJours);

  const annee = date.getFullYear();
  const mois = String(date.getMonth() + 1).padStart(2, '0');
  const jour = String(date.getDate()).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}

function formaterDateFr(dateIso: string): string {
  const [annee, mois, jour] = dateIso.split('-');
  return `${jour}/${mois}/${annee}`;
}

const dateMatchPublic = dateIsoDansJours(3);
const dateDebutStats = dateIsoDansJours(-14);
const dateFinStats = dateIsoDansJours(14);

const joueurG1001 = {
  membreId: 2001,
  matricule: 'G1001',
  nom: 'Dupont',
  prenom: 'Marie',
  categorieMembre: 'GLOBAL',
  siteRattachementId: null,
  nomSiteRattachement: null,
  actif: true,
  token: 'jwt-joueur-cypress',
  expirationToken: '2099-12-31T23:59:59'
};

const adminGlobal = {
  administrateurId: 2101,
  login: 'admin-global',
  nom: 'Admin',
  prenom: 'Global',
  roleAdministrateur: 'GLOBAL',
  siteId: null,
  nomSite: null,
  actif: true,
  token: 'jwt-admin-cypress',
  expirationToken: '2099-12-31T23:59:59'
};

const soldeG1001 = {
  membreId: 2001,
  matricule: 'G1001',
  soldeCredit: 100
};

const sitesActifs = [
  {
    siteId: 1001,
    code: 'BRU',
    nom: 'Padel Bruxelles',
    adresse: 'Rue du Padel 1, 1000 Bruxelles'
  },
  {
    siteId: 1002,
    code: 'NAM',
    nom: 'Padel Namur',
    adresse: 'Avenue des Sports 10, 5000 Namur'
  }
];

const parametresMetier = {
  dureeMatchMinutes: 90,
  pauseEntreMatchesMinutes: 15,
  nombreJoueursMaximum: 4,
  prixTotalMatch: 60,
  montantParticipationStandard: 15,
  soldeInitialJoueur: 100
};

const presentationDemo = {
  categoriesMembres: [
    {
      prefixe: 'G',
      categorie: 'GLOBAL',
      regle:
        "Peut réserver sur tous les sites, jusqu'à 21 jours avant."
    }
  ],
  sites: sitesActifs,
  joueurs: [
    {
      matricule: 'G1001',
      motDePasse: 'password',
      description: 'joueur GLOBAL actif'
    }
  ],
  administrateurs: [
    {
      login: 'admin-global',
      motDePasse: 'secret',
      description: 'administrateur GLOBAL'
    }
  ]
};

const disponibilitesBruxelles = {
  siteId: 1001,
  nomSite: 'Padel Bruxelles',
  date: dateMatchPublic,
  ferme: false,
  motifFermeture: null,
  creneaux: [
    {
      terrainId: 1103,
      numeroTerrain: 'T3',
      dateHeureDebut: `${dateMatchPublic}T13:15:00`,
      dateHeureFin: `${dateMatchPublic}T14:45:00`
    }
  ]
};

const matchPublicDisponible = {
  matchId: 3001,
  siteId: 1001,
  nomSite: 'Padel Bruxelles',
  terrainId: 1101,
  numeroTerrain: 'T1',
  dateHeureDebut: `${dateMatchPublic}T09:00:00`,
  dateHeureFin: `${dateMatchPublic}T10:30:00`,
  nombreParticipantsActifs: 2,
  placesDisponibles: 2,
  prixTotal: 60,
  montantParticipation: 15,
  peutRejoindre: true,
  motifNonEligibilite: null
};

const paiementMatchPublic = {
  matchId: 3001,
  participationId: 3103,
  paiementId: 6008,
  matriculeJoueur: 'G1001',
  montantPaye: 15,
  statutParticipation: 'CONFIRMEE',
  soldeRestant: 85
};

const statistiquesGlobales = {
  dateDebut: dateDebutStats,
  dateFin: dateFinStats,
  siteId: null,
  nomSite: null,
  nombreMatches: 3,
  nombreMatchesAVenir: 2,
  nombreMatchesTermines: 1,
  nombrePaiements: 7,
  chiffreAffaires: 105,
  nombreDettesOuvertes: 1,
  montantDettesOuvertes: 30,
  nombreParticipationsActives: 8,
  capaciteTheoriqueJoueurs: 12,
  tauxRemplissage: 66.67
};

function intercepterSitesActifs(): void {
  cy.intercept('GET', '/api/sites', {
    statusCode: 200,
    body: sitesActifs
  }).as('listeSitesActifs');
}

function intercepterParametresMetier(): void {
  cy.intercept('GET', '/api/parametres-metier', {
    statusCode: 200,
    body: parametresMetier
  }).as('parametresMetier');
}

function intercepterReferentielsPublics(): void {
  intercepterSitesActifs();
  intercepterParametresMetier();
}

function intercepterCompteurInvitationsRecues(): void {
  cy.intercept('GET', '/api/membres/G1001/invitations/recues/count', {
    statusCode: 200,
    body: 0
  }).as('compteurInvitationsRecues');
}

function intercepterConnexionJoueur(): void {
  cy.intercept('POST', '/api/auth/joueur', (request) => {
    expect(request.body).to.deep.equal({
      matricule: 'G1001',
      motDePasse: 'password'
    });

    request.reply({
      statusCode: 200,
      body: joueurG1001
    });
  }).as('connexionJoueur');
}

function intercepterConnexionAdmin(): void {
  cy.intercept('POST', '/api/auth/admin', (request) => {
    expect(request.body).to.deep.equal({
      login: 'admin-global',
      motDePasse: 'secret'
    });

    request.reply({
      statusCode: 200,
      body: adminGlobal
    });
  }).as('connexionAdmin');
}

function connecterJoueurG1001(): void {
  intercepterCompteurInvitationsRecues();
  intercepterConnexionJoueur();

  cy.visit('/joueur');

  cy.contains('h2', 'Connexion joueur').should('be.visible');

  cy.get('form').within(() => {
    cy.get('input[name="matricule"]').clear().type('G1001');
    cy.get('input[name="motDePasse"]').clear().type('password');
    cy.contains('button', 'Se connecter').click();
  });

  cy.wait('@connexionJoueur');

  cy.contains('Joueur connecté : Marie Dupont (G1001).').should('be.visible');
  cy.contains('a', 'Mon solde').should('exist');
}

function connecterAdminGlobal(): void {
  intercepterConnexionAdmin();

  cy.visit('/admin/login');

  cy.contains('h2', 'Connexion admin').should('be.visible');

  cy.get('form').within(() => {
    cy.get('input[name="login"]').clear().type('admin-global');
    cy.get('input[name="motDePasse"]').clear().type('secret');
    cy.contains('button', 'Se connecter').click();
  });

  cy.wait('@connexionAdmin');

  cy.contains('h2', 'Dashboard admin').should('be.visible');
  cy.contains('Login :').should('be.visible');
  cy.contains('admin-global').should('be.visible');
}

describe('Happy flows MVP Padel Marius', () => {
  it(
    'affiche les données de démonstration fournies par le backend',
    () => {
      cy.intercept(
        'GET',
        '/api/demo/presentation',
        {
          statusCode: 200,
          body: presentationDemo
        }
      ).as('presentationDemo');

      cy.visit('/accueil');

      cy.wait('@presentationDemo');

      cy.contains('Padel Bruxelles')
        .should('be.visible');

      cy.contains('G1001')
        .should('be.visible');

      cy.contains('admin-global')
        .should('be.visible');
    }
  );

  it('connecte un joueur puis consulte son solde', () => {
    connecterJoueurG1001();
    intercepterParametresMetier();

    cy.intercept('GET', '/api/membres/G1001/solde', {
      statusCode: 200,
      body: soldeG1001
    }).as('consultationSolde');

    cy.visit('/joueur/mon-solde');

    cy.wait(['@parametresMetier', '@consultationSolde']);

    cy.contains('h2', 'Mon solde').should('be.visible');
    cy.contains('Solde disponible').should('be.visible');
    cy.contains('100.00 €').should('be.visible');
    cy.contains('G1001').should('be.visible');
  });

  it('connecte un joueur puis consulte les disponibilités', () => {
    connecterJoueurG1001();
    intercepterReferentielsPublics();

    cy.intercept('GET', '/api/disponibilites*', {
      statusCode: 200,
      body: disponibilitesBruxelles
    }).as('consultationDisponibilites');

    cy.contains('a', 'Organiser un match').click();

    cy.contains('h2', 'Organiser un match').should('be.visible');

    cy.wait('@listeSitesActifs');

    cy.get('input[name="date"]').clear().type(dateMatchPublic);

    cy.contains('button', 'Voir les créneaux disponibles').click();

    cy.wait('@consultationDisponibilites');

    cy.contains(
      `Padel Bruxelles — ${formaterDateFr(dateMatchPublic)}`
    ).should('be.visible');

    cy.contains('Terrain T3').should('be.visible');
    cy.contains('(1103)').should('not.exist');
    cy.contains('13:15').should('be.visible');
    cy.contains('14:45').should('be.visible');
    cy.contains('Utiliser ce créneau pour créer un match').should('be.visible');
  });

  it('connecte un joueur puis rejoint un match public avec paiement', () => {
    connecterJoueurG1001();
    intercepterReferentielsPublics();

    cy.intercept('GET', '/api/matches/publics*', {
      statusCode: 200,
      body: [matchPublicDisponible]
    }).as('listeMatchesPublics');

    cy.intercept('POST', '/api/matches/3001/participants/public/payer', (request) => {
      expect(request.body).to.deep.equal({
        matriculeJoueur: 'G1001'
      });

      request.reply({
        statusCode: 200,
        body: paiementMatchPublic
      });
    }).as('paiementMatchPublic');

    cy.contains('a', 'Rejoindre un match public').click();

    cy.contains('h2', 'Rejoindre un match public').should('be.visible');

    cy.wait(['@listeSitesActifs', '@parametresMetier']);

    cy.get('input[name="date"]').clear().type(dateMatchPublic);

    cy.contains('button', 'Rechercher les matches publics').click();

    cy.wait('@listeMatchesPublics');

    cy.contains('Padel Bruxelles — Terrain T1').should('be.visible');
    cy.contains('Places disponibles').should('be.visible');
    cy.contains('Rejoindre et payer 15.00 €').click();

    cy.wait('@paiementMatchPublic');

    cy.contains('Padel Bruxelles — Terrain T1').should('be.visible');
  });

  it('connecte un admin puis consulte les statistiques globales', () => {
    connecterAdminGlobal();
    intercepterSitesActifs();

    cy.intercept('GET', '/api/admin/statistiques*', {
      statusCode: 200,
      body: statistiquesGlobales
    }).as('consultationStatistiques');

    cy.contains('a', 'Ouvrir les statistiques').click();

    cy.contains('h2', 'Statistiques admin').should('be.visible');

    cy.wait('@listeSitesActifs');

    cy.contains('button', 'Période démo complète').click();
    cy.contains('button', 'Charger les statistiques').click();

    cy.wait('@consultationStatistiques');

    cy.contains('Vue :').should('be.visible');
    cy.contains('globale tous sites').should('be.visible');
    cy.contains('Matches').should('be.visible');
    cy.contains('Chiffre d\'affaires').should('be.visible');
    cy.contains('105 €').should('be.visible');
    cy.contains('Dettes ouvertes').should('be.visible');
    cy.contains('Taux remplissage').should('be.visible');
  });
});
