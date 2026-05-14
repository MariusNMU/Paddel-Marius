export interface JourRapide {
  libelle: string;
  date: string;
}

const NOMS_JOURS = [
  'Dimanche',
  'Lundi',
  'Mardi',
  'Mercredi',
  'Jeudi',
  'Vendredi',
  'Samedi'
];

export function dateDuJourPourInput(): string {
  return formaterDatePourInput(new Date());
}

export function dateHeureDuJourPourInput(heure = '13:00'): string {
  return `${dateDuJourPourInput()}T${heure}`;
}

export function genererJoursRapides(nombreDeJours = 7): JourRapide[] {
  const aujourdHui = new Date();

  return Array.from({ length: nombreDeJours }, (_, index) => {
    const date = ajouterJours(aujourdHui, index);

    const libelle =
      index === 0
        ? 'Aujourd’hui'
        : index === 1
          ? 'Demain'
          : NOMS_JOURS[date.getDay()];

    return {
      libelle,
      date: formaterDatePourInput(date)
    };
  });
}

function ajouterJours(date: Date, nombreJours: number): Date {
  const copie = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  copie.setDate(copie.getDate() + nombreJours);

  return copie;
}

function formaterDatePourInput(date: Date): string {
  const annee = date.getFullYear();
  const mois = String(date.getMonth() + 1).padStart(2, '0');
  const jour = String(date.getDate()).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}
