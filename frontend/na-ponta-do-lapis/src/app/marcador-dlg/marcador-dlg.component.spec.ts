import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarcadorDlgComponent } from './marcador-dlg.component';

describe('MarcadorDlgComponent', () => {
  let component: MarcadorDlgComponent;
  let fixture: ComponentFixture<MarcadorDlgComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MarcadorDlgComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MarcadorDlgComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
