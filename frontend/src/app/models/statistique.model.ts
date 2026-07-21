export interface StatistiquesAdminResponse {
  dateDebut: string;
  dateFin: string;
  siteId: number | null;
  nomSite: string | null;
  nombreMatches: number;
  nombreMatchesAVenir: number;
  nombreMatchesTermines: number;
  nombrePaiements: number;
  chiffreAffaires: number;
  nombreDettesOuvertes: number;
  montantDettesOuvertes: number;
  nombreParticipationsActives: number;
  capaciteTheoriqueJoueurs: number;
  tauxRemplissage: number;
}
