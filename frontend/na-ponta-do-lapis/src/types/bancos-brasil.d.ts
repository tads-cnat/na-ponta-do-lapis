declare module '@edusites/bancos-brasil/core' {
  export interface SvgBancoOptions {
    nome?: string;
    formato?: 'padrao' | 'circulo' | 'quadrado' | 'sem' | string;
    cor?: string;
    fundo?: string;
    tamanho?: number;
    classes?: string;
    [key: string]: any;
  }

  export interface PresetBanco {
    nome: string;
    chave: string;
    fundo: string;
    cor: string;
  }

  export function svgBanco(options?: SvgBancoOptions): Promise<string> | string;
  export function obterPreset(nome: string): PresetBanco | null;
  export function listaBancos(): string[];
}

