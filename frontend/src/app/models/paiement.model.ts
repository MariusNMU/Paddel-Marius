export type NaturePaiement =
  'PARTICIPATION'
  | 'REGLEMENT_DETTE';

export type StatutPaiement =
  'EN_ATTENTE'
  | 'PAYE'
  | 'ANNULE';

export type StatutParticipation =
  'EN_ATTENTE_PAIEMENT'
  | 'CONFIRMEE'
  | 'LIBEREE';

export interface PayerParticipationRequest {
  montant: number;
}

export interface PaiementResponse {
  paiementId: number;
  participationId: number;
  membreId: number;
  matriculeMembre: string;
  montant: number;
  montantDettesReglees: number;
  montantTotalDebite: number;
  naturePaiement: NaturePaiement;
  statutPaiement: StatutPaiement;
  statutParticipation: StatutParticipation;
  dateHeurePaiement: string;
  dateConfirmationParticipation: string;
}

export interface HistoriquePaiementResponse {
  paiementId: number;
  membreId: number;
  matriculeMembre: string;
  naturePaiement: NaturePaiement;
  montant: number;
  statutPaiement: StatutPaiement;
  dateHeurePaiement: string;
  participationId: number | null;
  detteId: number | null;
  matchId: number | null;
}
