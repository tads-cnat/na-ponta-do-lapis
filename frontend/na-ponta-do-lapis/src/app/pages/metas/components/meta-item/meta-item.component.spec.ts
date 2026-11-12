import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MetaItemComponent } from './meta-item.component';
import { MetaResponse } from '@models/IMetas.models';

describe('MetaItemComponent', () => {
  let component: MetaItemComponent;
  let fixture: ComponentFixture<MetaItemComponent>;

  const mockMeta: MetaResponse = {
    id: 1,
    nome: 'Reserva de Emergência',
    descricao: 'Guardar dinheiro para imprevistos',
    valorTotal: 5000,
    valorAtual: 2500,
    fotoUrl: 'reserva',
    dataLimite: '2026-12-31',
    tipoMeta: 'POUPANCA',
    familiar: false,
    porcentagem: 50,
    atingida: false,
    expirada: false,
    tempoExpirada: '',
    contaId: 1,
    categoriaId: null,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MetaItemComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MetaItemComponent);
    component = fixture.componentInstance;
    component.meta = mockMeta;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve calcular valorFalta corretamente', () => {
    expect(component.valorFalta).toBe(2500);
  });

  it('deve emitir eventos ao clicar nas acoes', () => {
    let deletadoId: number | undefined;
    let editado: MetaResponse | undefined;
    let aportado: MetaResponse | undefined;
    let historico: MetaResponse | undefined;

    component.deletarMeta.subscribe((id) => (deletadoId = id));
    component.editarMeta.subscribe((m) => (editado = m));
    component.aportarMeta.subscribe((m) => (aportado = m));
    component.verHistorico.subscribe((m) => (historico = m));

    component.onDeletar();
    expect(deletadoId).toBe(1);

    component.onEditar();
    expect(editado).toEqual(mockMeta);

    component.onAportar();
    expect(aportado).toEqual(mockMeta);

    component.onVerHistorico();
    expect(historico).toEqual(mockMeta);
  });
});
