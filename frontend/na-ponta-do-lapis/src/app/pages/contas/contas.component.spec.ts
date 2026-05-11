import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContasComponent } from './contas.component';

describe('ContasComponent', () => {
  let component: ContasComponent;
  let fixture: ComponentFixture<ContasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContasComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ContasComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
