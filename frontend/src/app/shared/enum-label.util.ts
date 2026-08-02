const ENUM_LABELS: Record<string, string> = {
  A_VENIR: 'À venir',
  DEMARRE: 'Démarré',
  TERMINE: 'Terminé',
  ANNULE: 'Annulé',

  DISPONIBLE: 'Disponible',
  RESERVE: 'Réservé',
  FERME: 'Fermé',
  INACTIF: 'Inactif',

  PRIVE: 'Privé',
  PUBLIC: 'Public',

  EN_ATTENTE_PAIEMENT: 'En attente de paiement',
  CONFIRMEE: 'Confirmée',
  LIBEREE: 'Libérée',

  ORGANISATEUR: 'Organisateur',
  JOUEUR: 'Joueur',

  CREATION: 'Création',
  INVITATION_PRIVEE: 'Invitation privée',
  INSCRIPTION_PUBLIQUE: 'Inscription publique',

  GLOBAL: 'Global',
  SITE: 'Site',
  LIBRE: 'Libre',

  GLOBALE: 'Globale',
  LOCALE: 'Locale',

  OUVERTE: 'Ouverte',
  REGLEE: 'Réglée',

  EN_ATTENTE: 'En attente',
  PAYE: 'Payé',

  PARTICIPATION: 'Participation',
  REGLEMENT_DETTE: 'Règlement de dette'
};

export function enumLabel(value: string | null | undefined): string {
  if (!value) {
    return '-';
  }

  return ENUM_LABELS[value] ?? value;
}
