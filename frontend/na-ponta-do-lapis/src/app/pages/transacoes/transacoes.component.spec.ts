import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransacoesComponent } from './transacoes.component';

describe('TransacoesComponent', () => {
  let component: TransacoesComponent;
  let fixture: ComponentFixture<TransacoesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransacoesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TransacoesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
