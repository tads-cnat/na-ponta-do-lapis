import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemaComponentComponent } from './tema-component.component';

describe('TemaComponentComponent', () => {
  let component: TemaComponentComponent;
  let fixture: ComponentFixture<TemaComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemaComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TemaComponentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
