import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccueilComponent } from './accueil.component';

describe('AccueilComponent', () => {
  let fixture: ComponentFixture<AccueilComponent>;
  let component: AccueilComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccueilComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit afficher les sites de démonstration', () => {
    const contenu = fixture.nativeElement.textContent as string;

    expect(contenu).toContain('Padel Bruxelles');
    expect(contenu).toContain('Padel Namur');
    expect(contenu).toContain('BRU');
    expect(contenu).toContain('NAM');
  });

  it('doit afficher les identifiants joueurs de démonstration', () => {
    const contenu = fixture.nativeElement.textContent as string;

    expect(contenu).toContain('G1001');
    expect(contenu).toContain('S1001');
    expect(contenu).toContain('L1001');
    expect(contenu).toContain('password');
  });

  it('doit afficher les administrateurs de démonstration', () => {
    const contenu = fixture.nativeElement.textContent as string;

    expect(contenu).toContain('admin-global');
    expect(contenu).toContain('secret');
    expect(contenu).toContain('admin-bruxelles');
    expect(contenu).toContain('secret-site');
  });
});
