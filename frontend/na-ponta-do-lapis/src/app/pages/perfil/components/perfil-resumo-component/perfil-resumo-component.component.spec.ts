import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerfilResumoComponentComponent } from './perfil-resumo-component.component';

describe('PerfilResumoComponentComponent', () => {
  let component: PerfilResumoComponentComponent;
  let fixture: ComponentFixture<PerfilResumoComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerfilResumoComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilResumoComponentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
