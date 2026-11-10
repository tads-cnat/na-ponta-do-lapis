import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Usuario } from '../../model/IUsuario.models';

export type CodigoMoeda = 'BRL' | 'USD' | 'EUR';

export interface OpcaoMoeda {
  codigo: CodigoMoeda;
  nome: string;
  simbolo: string;
  bandeira: string;
}

@Injectable({
  providedIn: 'root'
})
export class PreferenciaMoedaService {
  private static readonly STORAGE_KEY = 'npl_moeda_padrao';

  readonly opcoesMoeda: OpcaoMoeda[] = [
    { codigo: 'BRL', nome: 'Real brasileiro',  simbolo: 'R$',  bandeira: '🇧🇷' },
    { codigo: 'USD', nome: 'Dólar americano', simbolo: 'US$', bandeira: '🇺🇸' },
    { codigo: 'EUR', nome: 'Euro',             simbolo: '€',   bandeira: '🇪🇺' },
  ];

  private readonly moedaSubject = new BehaviorSubject<string>(this.recuperarMoedaInicial());
  public readonly moeda$ = this.moedaSubject.asObservable();

  private recuperarMoedaInicial(): string {
    if (typeof window !== 'undefined' && window.localStorage) {
      const salva = localStorage.getItem(PreferenciaMoedaService.STORAGE_KEY);
      if (salva && (salva === 'BRL' || salva === 'USD' || salva === 'EUR')) {
        return salva;
      }
    }
    return 'BRL';
  }

  public obterMoedaAtiva(): string {
    return this.moedaSubject.value || 'BRL';
  }

  public obterSimboloMoeda(moeda?: string | null): string {
    const cod = (moeda || this.obterMoedaAtiva()).toUpperCase();
    switch (cod) {
      case 'USD': return 'US$';
      case 'EUR': return '€';
      case 'BRL':
      default:    return 'R$';
    }
  }

  public obterLocaleMoeda(moeda?: string | null): string {
    const cod = (moeda || this.obterMoedaAtiva()).toUpperCase();
    switch (cod) {
      case 'USD': return 'en-US';
      case 'EUR': return 'de-DE';
      case 'BRL':
      default:    return 'pt-BR';
    }
  }

  public obterSeparadorDecimal(moeda?: string | null): string {
    const cod = (moeda || this.obterMoedaAtiva()).toUpperCase();
    return cod === 'USD' ? '.' : ',';
  }

  public obterSeparadorMilhar(moeda?: string | null): string {
    const cod = (moeda || this.obterMoedaAtiva()).toUpperCase();
    return cod === 'USD' ? ',' : '.';
  }

  public definirMoedaPadrao(moeda: string): void {
    const limpa = (moeda || 'BRL').toUpperCase();
    this.moedaSubject.next(limpa);
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.setItem(PreferenciaMoedaService.STORAGE_KEY, limpa);
    }
  }

  public carregarMoedaDoUsuario(usuario: Usuario | null | undefined): void {
    if (usuario?.moedaPadrao) {
      this.definirMoedaPadrao(usuario.moedaPadrao);
    }
  }
}

