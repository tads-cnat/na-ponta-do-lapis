import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormularioSecaoComponent } from './formulario-secao.component';

describe('FormularioSecaoComponent', () => {
  let component: FormularioSecaoComponent;
  let fixture: ComponentFixture<FormularioSecaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioSecaoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioSecaoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
