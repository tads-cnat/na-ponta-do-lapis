import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BotoesAcaoComponent } from './botoes-acao.component';

describe('BotoesAcaoComponent', () => {
  let component: BotoesAcaoComponent;
  let fixture: ComponentFixture<BotoesAcaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BotoesAcaoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BotoesAcaoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
