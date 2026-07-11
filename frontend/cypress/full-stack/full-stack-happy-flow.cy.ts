function dateIsoDansJours(decalageJours: number): string {
  const date = new Date();
  date.setDate(date.getDate() + decalageJours);

  const annee = date.getFullYear();
  const mois = String(date.getMonth() + 1).padStart(2, '0');
  const jour = String(date.getDate()).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}

describe('Happy flow full stack Padel Marius', () => {
  const matricule = 'G1001';
  const motDePasse = 'password';
  const dateMatch = dateIsoDansJours(6);

  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('connecte un joueur, consulte les disponibilités, crée un match puis voit la réservation', () => {
    cy.visit('/joueur');

    cy.contains('h2', 'Connexion joueur').should('be.visible');

    cy.get('input[name="matricule"]').clear().type(matricule);
    cy.get('input[name="motDePasse"]').clear().type(motDePasse);

    cy.contains('button', 'Se connecter').click();

    cy.contains('Joueur connecté : Marie Dupont (G1001).', {
      timeout: 10000
    }).should('be.visible');

    cy.visit('/joueur/disponibilites');

    cy.contains('h2', 'Organiser un match').should('be.visible');

    cy.get('input[name="date"]').clear().type(dateMatch);

    cy.contains('button', 'Voir les créneaux disponibles').click();

    cy.contains(`Padel Bruxelles (1001) — ${dateMatch}`, {
      timeout: 10000
    }).should('be.visible');

    cy.contains('.creneau-card', 'Terrain T3 (1103)', {
      timeout: 10000
    })
      .should('be.visible')
      .within(() => {
        cy.contains('button', 'Utiliser ce créneau pour créer un match').click();
      });

    cy.location('pathname', { timeout: 10000 })
      .should('eq', '/joueur/creer-match');

    cy.contains('h2', 'Créer un match').should('be.visible');

    cy.get('select[name="terrainId"]').should('contain', 'Terrain T3 (1103)');
    cy.get('input[name="matriculeOrganisateur"]').should('have.value', matricule);

    cy.get('input[name="dateHeureDebut"]')
      .invoke('val')
      .then((valeur) => {
        expect(String(valeur)).to.match(new RegExp(`^${dateMatch}T`));
      });

    cy.get('select[name="modeCreation"]').select('PUBLIC');

    cy.contains('button', 'Créer le match').click();

    cy.contains('Match créé avec succès', {
      timeout: 10000
    }).should('be.visible');

    cy.get('.resultat.match-card').within(() => {
      cy.contains('T3 (1103)').should('be.visible');
      cy.contains('Public').should('be.visible');
      cy.contains('À venir').should('be.visible');
    });

    cy.visit('/joueur/mes-reservations');

    cy.contains('h2', 'Mes réservations').should('be.visible');

    cy.contains('.reservation-card', 'T3 (1103)', {
      timeout: 10000
    })
      .should('be.visible')
      .within(() => {
        cy.contains('Padel Bruxelles').should('be.visible');
        cy.contains(dateMatch).should('be.visible');
        cy.contains('Organisateur').should('be.visible');
        cy.contains('En attente de paiement').should('be.visible');
        cy.contains('À venir').should('be.visible');
        cy.contains('Public').should('be.visible');
      });
  });
});
