import { svgBanco, obterPreset } from '@edusites/bancos-brasil/core';

export interface BancoResolvido {
  chave: string | null;
  nomeExibicao: string;
  corFundo?: string;
  corIcone?: string;
}

const ALIASES: Record<string, { nome: string; aliases: string[]; corPadrao?: string }> = {
  nubank: {
    nome: 'Nubank',
    aliases: ['nubank', 'nu pagamentos', 'nu financeiro', 'roxinho', 'nuconta', 'ultravioleta'],
    corPadrao: '#820AD1'
  },
  inter: {
    nome: 'Banco Inter',
    aliases: ['banco inter', 'inter', 'intermedium', 'bancointer', 'intercard'],
    corPadrao: '#FF7A00'
  },
  itau: {
    nome: 'Itaú',
    aliases: ['itaú', 'itau', 'itaucard', 'unibanco', 'itau unibanco', 'personnalite', 'personnalité'],
    corPadrao: '#EC7000'
  },
  bradesco: {
    nome: 'Bradesco',
    aliases: ['bradesco', 'bradescard', 'banco bradesco', 'bradesco cartoes'],
    corPadrao: '#CC092F'
  },
  santander: {
    nome: 'Santander',
    aliases: ['santander', 'banco santander', 'totta', 'santander sx', 'santander free'],
    corPadrao: '#EA1D2C'
  },
  bancodobrasil: {
    nome: 'Banco do Brasil',
    aliases: ['banco do brasil', 'brasil', 'bb', 'ourocard', 'banco brasil'],
    corPadrao: '#003882'
  },
  caixa: {
    nome: 'Caixa Econômica',
    aliases: ['caixa', 'caixa econômica', 'caixa economica', 'cef'],
    corPadrao: '#0066B3'
  },
  c6: {
    nome: 'C6 Bank',
    aliases: ['c6', 'c6 bank', 'c6bank'],
    corPadrao: '#242424'
  },
  picpay: {
    nome: 'PicPay',
    aliases: ['picpay', 'pic pay'],
    corPadrao: '#11C76F'
  },
  mercadopago: {
    nome: 'Mercado Pago',
    aliases: ['mercado pago', 'mercadopago'],
    corPadrao: '#009EE3'
  },
  btg: {
    nome: 'BTG Pactual',
    aliases: ['btg', 'btg pactual', 'pactual'],
    corPadrao: '#001E62'
  },
  sicoob: {
    nome: 'Sicoob',
    aliases: ['sicoob'],
    corPadrao: '#003641'
  },
  sicredi: {
    nome: 'Sicredi',
    aliases: ['sicredi'],
    corPadrao: '#004F34'
  },
  pan: {
    nome: 'Banco Pan',
    aliases: ['pan', 'banco pan'],
    corPadrao: '#007AFF'
  },
  neon: {
    nome: 'Neon',
    aliases: ['neon', 'banco neon'],
    corPadrao: '#00E5FF'
  },
  safra: {
    nome: 'Safra',
    aliases: ['safra', 'banco safra'],
    corPadrao: '#1E2330'
  },
  pagbank: {
    nome: 'PagBank',
    aliases: ['pagbank', 'pagseguro', 'pagseguro internet'],
    corPadrao: '#00A859'
  },
  xp: {
    nome: 'XP Investimentos',
    aliases: ['xp', 'xp investimentos'],
    corPadrao: '#000000'
  },
  cora: {
    nome: 'Cora',
    aliases: ['cora', 'corabank'],
    corPadrao: '#FE3E6D'
  },
  stone: {
    nome: 'Stone',
    aliases: ['stone', 'stone pagamentos'],
    corPadrao: '#00A868'
  },
  next: {
    nome: 'Next',
    aliases: ['next', 'banco next'],
    corPadrao: '#00FF5F'
  },
  bmg: {
    nome: 'BMG',
    aliases: ['bmg', 'banco bmg'],
    corPadrao: '#FF5A00'
  },
  nomad: {
    nome: 'Nomad',
    aliases: ['nomad', 'nomad global'],
    corPadrao: '#FFD700'
  },
  wise: {
    nome: 'Wise',
    aliases: ['wise', 'transferwise'],
    corPadrao: '#9FE870'
  }
};

/**
 * Normaliza o texto e encontra a chave do banco na biblioteca @edusites/bancos-brasil
 */
export function mapearNomeBanco(nome?: string | null): BancoResolvido {
  if (!nome || !nome.trim()) {
    return { chave: null, nomeExibicao: 'Instituição Bancária' };
  }

  const normalized = nome.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');

  for (const [chave, item] of Object.entries(ALIASES)) {
    for (const alias of item.aliases) {
      const aliasNormalized = alias.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
      const regex = new RegExp(`(^|\\W)${aliasNormalized}(\\W|$)`, 'i');
      if (regex.test(normalized) || normalized.includes(aliasNormalized)) {
        let preset: any = null;
        try {
          preset = obterPreset(chave);
        } catch {
          preset = null;
        }
        const corFundo = preset?.fundo || item.corPadrao;
        return {
          chave,
          nomeExibicao: item.nome,
          corFundo,
          corIcone: preset?.cor
        };
      }
    }
  }

  return { chave: null, nomeExibicao: nome };
}

/**
 * Retorna true se o nome informado corresponde a um banco reconhecido com logo oficial
 */
export function temBancoDetectado(nome?: string | null): boolean {
  return mapearNomeBanco(nome).chave !== null;
}

/**
 * Retorna a cor oficial da marca do banco a partir do nome ou apelido
 */
export function obterCorOficialBanco(nome?: string | null): string | null {
  const resolvido = mapearNomeBanco(nome);
  return resolvido.corFundo ?? null;
}

/**
 * Gera a string SVG usando a lib @edusites/bancos-brasil
 */
export async function obterSvgBancoString(options: {
  nome: string;
  formato?: 'quadrado' | 'circulo' | 'sem';
  tamanho?: number;
  cor?: string;
  fundo?: string;
}): Promise<string | null> {
  try {
    const res = svgBanco(options);
    if (res instanceof Promise) {
      return await res;
    }
    return res;
  } catch (err) {
    console.warn('[banco-icon.helper] Erro ao renderizar svgBanco:', err);
    return null;
  }
}
