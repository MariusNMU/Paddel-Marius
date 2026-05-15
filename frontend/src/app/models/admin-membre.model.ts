export type CategorieMembre = 'GLOBAL' | 'SITE' | 'LIBRE';

export interface MembreAdminResponse {
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
