export interface PayerParticipationRequest {
  montant: number;
}

export interface PaiementResponse {
  paiementId: number;
  membreId: number;
  montant: number;
  naturePaiement: string;
  statutPaiement: string;
  dateHeurePaiement: string;
}
