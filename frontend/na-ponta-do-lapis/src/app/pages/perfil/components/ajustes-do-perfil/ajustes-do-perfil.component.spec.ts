import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjustesDoPerfilComponent } from './ajustes-do-perfil.component';

describe('AjustesDoPerfilComponent', () => {
  let component: AjustesDoPerfilComponent;
  let fixture: ComponentFixture<AjustesDoPerfilComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AjustesDoPerfilComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AjustesDoPerfilComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
