import { enumLabel } from './enum-label.util';

describe('enumLabel', () => {
  it('doit traduire les états du cycle de match', () => {
    expect(enumLabel('A_VENIR')).toBe('À venir');
    expect(enumLabel('DEMARRE')).toBe('Démarré');
    expect(enumLabel('TERMINE')).toBe('Terminé');
    expect(enumLabel('ANNULE')).toBe('Annulé');
  });

  it('doit traduire les états opérationnels des terrains', () => {
    expect(enumLabel('DISPONIBLE')).toBe('Disponible');
    expect(enumLabel('RESERVE')).toBe('Réservé');
    expect(enumLabel('FERME')).toBe('Fermé');
    expect(enumLabel('INACTIF')).toBe('Inactif');
  });

  it('doit traduire les modes de création et de visibilité', () => {
    expect(enumLabel('PRIVE')).toBe('Privé');
    expect(enumLabel('PUBLIC')).toBe('Public');
  });

  it('doit traduire les statuts de participation', () => {
    expect(enumLabel('EN_ATTENTE_PAIEMENT')).toBe('En attente de paiement');
    expect(enumLabel('CONFIRMEE')).toBe('Confirmée');
    expect(enumLabel('LIBEREE')).toBe('Libérée');
  });

  it('doit traduire les rôles et les modes d entrée', () => {
    expect(enumLabel('ORGANISATEUR')).toBe('Organisateur');
    expect(enumLabel('JOUEUR')).toBe('Joueur');
    expect(enumLabel('CREATION')).toBe('Création');
    expect(enumLabel('INVITATION_PRIVEE')).toBe('Invitation privée');
    expect(enumLabel('INSCRIPTION_PUBLIQUE')).toBe('Inscription publique');
  });

  it('doit traduire les catégories et portées utilisées dans les écrans admin', () => {
    expect(enumLabel('GLOBAL')).toBe('Global');
    expect(enumLabel('SITE')).toBe('Site');
    expect(enumLabel('LIBRE')).toBe('Libre');
    expect(enumLabel('GLOBALE')).toBe('Globale');
    expect(enumLabel('LOCALE')).toBe('Locale');
  });

  it('doit traduire tous les statuts de paiement', () => {
    expect(enumLabel('EN_ATTENTE')).toBe('En attente');
    expect(enumLabel('PAYE')).toBe('Payé');
    expect(enumLabel('REFUSE')).toBe('Refusé');
  });

  it('doit garder la valeur technique si aucun libellé n existe encore', () => {
    expect(enumLabel('VALEUR_INCONNUE')).toBe('VALEUR_INCONNUE');
  });

  it('doit afficher un tiret pour une valeur vide', () => {
    expect(enumLabel(null)).toBe('-');
    expect(enumLabel(undefined)).toBe('-');
    expect(enumLabel('')).toBe('-');
  });
});
