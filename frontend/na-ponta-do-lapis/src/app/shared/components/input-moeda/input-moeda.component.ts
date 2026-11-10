import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  forwardRef,
  OnInit,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { PreferenciaMoedaService } from '../../service/preferencia-moeda.service';

@Component({
  selector: 'app-input-moeda',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputMoedaComponent),
      multi: true
    }
  ],
  template: `
    <div
      (click)="focar()"
      [class]="obterClassesContainer()"
      [style.borderRadius]="'var(--p-form-field-border-radius, 6px)'"
      [class.opacity-60]="disabled"
      [class.cursor-not-allowed]="disabled"
      [class.cursor-text]="!disabled">

      <!-- PREFIXO DA MOEDA DISCRETO E INTEGRADO -->
      <span class="font-medium text-slate-400 select-none shrink-0"
            [class.text-xs]="tamanho === 'sm'"
            [class.text-base]="tamanho === 'md'"
            [class.text-lg]="tamanho === 'lg'">
        {{ simbolo }}
      </span>

      <!-- INPUT NATIVO CONTROLADO -->
      <input
        #inputElement
        type="text"
        inputmode="numeric"
        [id]="inputId || null"
        [disabled]="disabled"
        [value]="valorFormatado"
        (input)="aoDigitar($event)"
        (focus)="aoFoco($event)"
        (blur)="aoDesfoco($event)"
        [placeholder]="placeholderAtivo"
        [class]="obterClassesInput()" />
    </div>
  `
})
export class InputMoedaComponent implements ControlValueAccessor, OnInit, OnDestroy {
  @ViewChild('inputElement') inputRef!: ElementRef<HTMLInputElement>;

  @Input() moeda?: string | null;
  @Input() placeholder: string = '0,00';
  @Input() tamanho: 'sm' | 'md' | 'lg' = 'md';
  @Input() inputId?: string;
  @Input() disabled: boolean | undefined = false;
  @Input() permitirVazio: boolean = false;
  @Input() classesContainer?: string;
  @Input() classesInput?: string;

  @Output() aoMudar = new EventEmitter<number | null>();
  @Output() aoFocar = new EventEmitter<FocusEvent>();
  @Output() aoDesfocar = new EventEmitter<FocusEvent>();

  public valorFormatado: string = '';
  private valorNumericoAtual: number | null = null;
  private subMoeda?: Subscription;

  private onChange: (valor: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(
    private preferenciaMoedaService: PreferenciaMoedaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subMoeda = this.preferenciaMoedaService.moeda$.subscribe(() => {
      if (this.valorNumericoAtual !== null && this.valorNumericoAtual !== undefined) {
        this.valorFormatado = this.formatarNumero(this.valorNumericoAtual);
      } else if (!this.permitirVazio) {
        this.valorFormatado = this.separadorDecimal === '.' ? '0.00' : '0,00';
      }
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.subMoeda?.unsubscribe();
  }

  get simbolo(): string {
    return this.preferenciaMoedaService.obterSimboloMoeda(this.moeda);
  }

  get localeAtivo(): string {
    return this.preferenciaMoedaService.obterLocaleMoeda(this.moeda);
  }

  get separadorDecimal(): string {
    return this.preferenciaMoedaService.obterSeparadorDecimal(this.moeda);
  }

  get placeholderAtivo(): string {
    if (this.placeholder && this.placeholder !== '0,00' && this.placeholder !== '0.00') {
      return this.placeholder;
    }
    return this.separadorDecimal === '.' ? '0.00' : '0,00';
  }

  private formatarNumero(num: number): string {
    return num.toLocaleString(this.localeAtivo, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  public focar(): void {
    if (!this.disabled && this.inputRef?.nativeElement) {
      this.inputRef.nativeElement.focus();
    }
  }

  // =========================================
  // CONTROL VALUE ACCESSOR
  // =========================================

  writeValue(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.valorNumericoAtual = this.permitirVazio ? null : 0;
      this.valorFormatado = this.permitirVazio ? '' : (this.separadorDecimal === '.' ? '0.00' : '0,00');
    } else {
      const num = Number(valor);
      if (isNaN(num)) {
        this.valorNumericoAtual = this.permitirVazio ? null : 0;
        this.valorFormatado = this.permitirVazio ? '' : (this.separadorDecimal === '.' ? '0.00' : '0,00');
      } else {
        this.valorNumericoAtual = num;
        this.valorFormatado = this.formatarNumero(num);
      }
    }
    this.cdr.markForCheck();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.cdr.markForCheck();
  }

  // =========================================
  // DIGITAÇÃO FLUIDA ESTILO FINTECH
  // =========================================

  public aoDigitar(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digitos = input.value.replace(/\D/g, '');

    if (!digitos) {
      if (this.permitirVazio) {
        this.valorNumericoAtual = null;
        this.valorFormatado = '';
        input.value = '';
      } else {
        this.valorNumericoAtual = 0;
        this.valorFormatado = this.separadorDecimal === '.' ? '0.00' : '0,00';
        input.value = this.valorFormatado;
      }
      this.onChange(this.valorNumericoAtual);
      this.aoMudar.emit(this.valorNumericoAtual);
      return;
    }

    const valor = Number(digitos) / 100;
    this.valorNumericoAtual = valor;
    const formatado = this.formatarNumero(valor);

    this.valorFormatado = formatado;
    input.value = formatado;

    this.onChange(valor);
    this.aoMudar.emit(valor);
  }

  public aoFoco(event: FocusEvent): void {
    this.aoFocar.emit(event);
  }

  public aoDesfoco(event: FocusEvent): void {
    this.onTouched();
    this.aoDesfocar.emit(event);
  }

  // =========================================
  // ESTILOS PADRONIZADOS COM OS INPUTS PRIMENG
  // =========================================

  public obterClassesContainer(): string {
    if (this.classesContainer) {
      return this.classesContainer;
    }

    const base = 'flex items-center gap-1.5 w-full bg-white border border-slate-300 transition-colors focus-within:border-[#7843cd] focus-within:ring-1 focus-within:ring-[#7843cd]';

    switch (this.tamanho) {
      case 'sm':
        return `${base} h-[36px] min-h-[36px] px-2.5`;
      case 'lg':
        return `${base} h-[48px] min-h-[48px] px-3.5`;
      case 'md':
      default:
        return `${base} h-[42px] min-h-[42px] px-3`;
    }
  }

  public obterClassesInput(): string {
    if (this.classesInput) {
      return this.classesInput;
    }

    const base = 'w-full bg-transparent border-0 p-0 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-0 disabled:cursor-not-allowed leading-normal';

    switch (this.tamanho) {
      case 'sm':
        return `${base} text-xs font-normal`;
      case 'lg':
        return `${base} text-lg font-normal`;
      case 'md':
      default:
        return `${base} text-base font-normal`;
    }
  }
}

