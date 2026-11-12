import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { Observable, of } from 'rxjs';
import { MetaResponse, TipoMeta, TipoMetaResponse } from '../../model/IMetas.models';
import { ContaFinanceiraService } from '../contas/service/contas.service';
import { MetasComponent } from './metas.component';
import { MetasService } from './services/metas.service';

describe('MetasComponent', () => {
  let fixture: ComponentFixture<MetasComponent>;
  let component: MetasComponent;
  let metasService: {
    deletions: number[];
    listarMetas: () => Observable<MetaResponse[]>;
    listarTiposMeta: () => Observable<TipoMetaResponse[]>;
    deletarMeta: (id: number) => Observable<MetaResponse>;
  };

  const meta: MetaResponse = {
    id: 42,
    nome: 'Reserva de emergência',
    valorTotal: 5000,
    valorAtual: 800,
    tipoMeta: TipoMeta.POUPANCA,
    usuarioId: 1
  };

  beforeEach(async () => {
    metasService = {
      deletions: [],
      listarMetas: () => of([] as MetaResponse[]),
      listarTiposMeta: () => of([] as TipoMetaResponse[]),
      deletarMeta: (id: number) => {
        metasService.deletions.push(id);
        return of(meta);
      }
    };

    await TestBed.configureTestingModule({
      imports: [MetasComponent],
      providers: [
        MessageService,
        { provide: MetasService, useValue: metasService },
        { provide: ContaFinanceiraService, useValue: { listarContasUsuario: () => of([]) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MetasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.metas.set([meta]);
  });

  it('asks for confirmation before deleting a goal', () => {
    component.deletarMeta(meta.id!);

    expect(component.exibirDialogExcluirMeta).toBe(true);
    expect(metasService.deletions).toEqual([]);
  });

  it('deletes the goal only after confirmation', () => {
    component.deletarMeta(meta.id!);
    component.confirmarExclusaoMeta();

    expect(metasService.deletions).toEqual([meta.id]);
    expect(component.exibirDialogExcluirMeta).toBe(false);
    expect(component.metas()).toEqual([]);
  });
});
