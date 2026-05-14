export interface PayerParticipationRequest {
  montant: number;
}

export interface PaiementResponse {
  paiementId: number;
  participationId?: number;
  membreId: number;
  matriculeMembre?: string;
  montant: number;
  montantDettesReglees?: number;
  montantTotalDebite?: number;
  naturePaiement: string;
  statutPaiement: string;
  statutParticipation?: string;
  dateHeurePaiement: string;
  dateConfirmationParticipation?: string;
}

export interface HistoriquePaiementResponse {
  paiementId: number;
  membreId: number;
  matriculeMembre: string;
  naturePaiement: string;
  montant: number;
  statutPaiement: string;
  dateHeurePaiement: string;
  participationId: number | null;
  detteId: number | null;
  matchId: number | null;
}
