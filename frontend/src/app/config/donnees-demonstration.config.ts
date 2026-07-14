export const DONNEES_DEMONSTRATION = {
  categoriesMembres: [
    {
      prefixe: 'G',
      categorie: 'GLOBAL',
      regle: "Peut réserver sur tous les sites, jusqu'à 21 jours avant."
    },
    {
      prefixe: 'S',
      categorie: 'SITE',
      regle: "Peut réserver uniquement sur son site de rattachement, jusqu'à 14 jours avant."
    },
    {
      prefixe: 'L',
      categorie: 'LIBRE',
      regle: "Peut réserver sur tous les sites, jusqu'à 5 jours avant."
    }
  ],
  sites: [
    {
      id: 1001,
      code: 'BRU',
      nom: 'Padel Bruxelles'
    },
    {
      id: 1002,
      code: 'NAM',
      nom: 'Padel Namur'
    }
  ],
  joueurs: [
    {
      matricule: 'G1001',
      motDePasse: 'password',
      description: 'joueur GLOBAL actif'
    },
    {
      matricule: 'G1002',
      motDePasse: 'password',
      description: 'joueur GLOBAL actif avec dette ouverte'
    },
    {
      matricule: 'S1001',
      motDePasse: 'password',
      description: 'joueur SITE rattaché à Padel Bruxelles (1001)'
    },
    {
      matricule: 'S1002',
      motDePasse: 'password',
      description: 'joueur SITE rattaché à Padel Namur (1002)'
    },
    {
      matricule: 'L1001',
      motDePasse: 'password',
      description: 'joueur LIBRE actif'
    },
    {
      matricule: 'L1002',
      motDePasse: 'password',
      description: 'joueur LIBRE avec pénalité active'
    },
    {
      matricule: 'G9999',
      motDePasse: 'password',
      description: 'joueur inactif pour tester le refus'
    }
  ],
  administrateurs: [
    {
      login: 'admin-global',
      motDePasse: 'secret',
      description: 'administrateur GLOBAL'
    },
    {
      login: 'admin-bruxelles',
      motDePasse: 'secret-site',
      description: 'administrateur SITE Bruxelles (1001)'
    }
  ]
} as const;
