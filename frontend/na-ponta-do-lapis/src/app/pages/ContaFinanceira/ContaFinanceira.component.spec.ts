import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContaFinanceiraComponent } from './ContaFinanceira.component';

describe('ContaFinanceiraComponent', () => {
  let component: ContaFinanceiraComponent;
  let fixture: ComponentFixture<ContaFinanceiraComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContaFinanceiraComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaFinanceiraComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
