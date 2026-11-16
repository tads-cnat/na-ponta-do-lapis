import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemaComponentComponent } from './tema-component.component';
import { ThemeService } from '../../../../service/theme.service';

describe('TemaComponentComponent', () => {
  let component: TemaComponentComponent;
  let fixture: ComponentFixture<TemaComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemaComponentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TemaComponentComponent);
    component = fixture.componentInstance;
    TestBed.inject(ThemeService).definir('claro');
    await fixture.whenStable();
  }); 

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('applies the selected dark theme preference', async () => {
    fixture.nativeElement.querySelectorAll('button')[1].click();
    await fixture.whenStable();

    expect(TestBed.inject(ThemeService).tema()).toBe('escuro');
    expect(localStorage.getItem('npl-tema')).toBe('escuro');
  });
});
