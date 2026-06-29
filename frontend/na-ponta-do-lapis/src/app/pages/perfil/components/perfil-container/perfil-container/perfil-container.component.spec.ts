import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerfilContainerComponent } from './perfil-container.component';

describe('PerfilContainerComponent', () => {
  let component: PerfilContainerComponent;
  let fixture: ComponentFixture<PerfilContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerfilContainerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilContainerComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
