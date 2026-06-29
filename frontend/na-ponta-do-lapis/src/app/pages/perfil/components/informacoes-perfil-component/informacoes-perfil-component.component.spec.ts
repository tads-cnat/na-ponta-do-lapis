import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformacoesPerfilComponentComponent } from './informacoes-perfil-component.component';

describe('InformacoesPerfilComponentComponent', () => {
  let component: InformacoesPerfilComponentComponent;
  let fixture: ComponentFixture<InformacoesPerfilComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformacoesPerfilComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InformacoesPerfilComponentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
