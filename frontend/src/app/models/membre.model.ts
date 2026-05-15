export type CategorieMembre = 'GLOBAL' | 'SITE' | 'LIBRE';

export interface InscriptionMembreRequest {
  nom: string;
  prenom: string;
  categorieMembre: CategorieMembre;
  siteRattachementId: number | null;
}

export interface MembreResponse {
  membreId: number;
  matricule: string;
  nom: string;
  prenom: string;
  categorieMembre: CategorieMembre;
  siteRattachementId: number | null;
  nomSiteRattachement: string | null;
  actif: boolean;
  soldeCredit: number;
}
