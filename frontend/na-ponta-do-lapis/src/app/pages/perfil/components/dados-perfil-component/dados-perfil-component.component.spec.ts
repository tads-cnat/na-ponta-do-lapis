import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DadosPerfilComponentComponent } from './dados-perfil-component.component';

describe('DadosPerfilComponentComponent', () => {
  let component: DadosPerfilComponentComponent;
  let fixture: ComponentFixture<DadosPerfilComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DadosPerfilComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DadosPerfilComponentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
