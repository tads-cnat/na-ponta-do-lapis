import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AreaPerfilComponentComponent } from './area-perfil-component.component';

describe('AreaPerfilComponentComponent', () => {
  let component: AreaPerfilComponentComponent;
  let fixture: ComponentFixture<AreaPerfilComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AreaPerfilComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AreaPerfilComponentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
