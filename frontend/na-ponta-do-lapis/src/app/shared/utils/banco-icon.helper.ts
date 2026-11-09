import { svgBanco, obterPreset } from '@edusites/bancos-brasil/core';

export interface BancoResolvido {
  chave: string | null;
  nomeExibicao: string;
  corFundo?: string;
  corIcone?: string;
}

const ALIASES: Record<string, { nome: string; aliases: string[] }> = {
  nubank: {
    nome: 'Nubank',
    aliases: ['nubank', 'nu pagamentos', 'nu financeiro', 'roxinho']
  },
  inter: {
    nome: 'Banco Inter',
    aliases: ['banco inter', 'inter', 'intermedium', 'bancointer']
  },
  itau: {
    nome: 'Itaú',
    aliases: ['itaú', 'itau', 'itaucard', 'unibanco', 'itau unibanco']
  },
  bradesco: {
    nome: 'Bradesco',
    aliases: ['bradesco', 'bradescard', 'banco bradesco']
  },
  santander: {
    nome: 'Santander',
    aliases: ['santander', 'banco santander', 'totta']
  },
  bancodobrasil: {
    nome: 'Banco do Brasil',
    aliases: ['banco do brasil', 'brasil', 'bb', 'ourocard']
  },
  caixa: {
    nome: 'Caixa Econômica',
    aliases: ['caixa', 'caixa econômica', 'caixa economica', 'cef']
  },
  c6: {
    nome: 'C6 Bank',
    aliases: ['c6', 'c6 bank', 'c6bank']
  },
  picpay: {
    nome: 'PicPay',
    aliases: ['picpay', 'pic pay']
  },
  mercadopago: {
    nome: 'Mercado Pago',
    aliases: ['mercado pago', 'mercadopago']
  },
  btg: {
    nome: 'BTG Pactual',
    aliases: ['btg', 'btg pactual', 'pactual']
  },
  sicoob: {
    nome: 'Sicoob',
    aliases: ['sicoob']
  },
  sicredi: {
    nome: 'Sicredi',
    aliases: ['sicredi']
  },
  pan: {
    nome: 'Banco Pan',
    aliases: ['pan', 'banco pan']
  },
  neon: {
    nome: 'Neon',
    aliases: ['neon', 'banco neon']
  },
  safra: {
    nome: 'Safra',
    aliases: ['safra', 'banco safra']
  },
  pagbank: {
    nome: 'PagBank',
    aliases: ['pagbank', 'pagseguro', 'pagseguro internet']
  },
  xp: {
    nome: 'XP Investimentos',
    aliases: ['xp', 'xp investimentos']
  },
  cora: {
    nome: 'Cora',
    aliases: ['cora', 'corabank']
  },
  stone: {
    nome: 'Stone',
    aliases: ['stone', 'stone pagamentos']
  },
  next: {
    nome: 'Next',
    aliases: ['next', 'banco next']
  },
  bmg: {
    nome: 'BMG',
    aliases: ['bmg', 'banco bmg']
  },
  nomad: {
    nome: 'Nomad',
    aliases: ['nomad', 'nomad global']
  },
  wise: {
    nome: 'Wise',
    aliases: ['wise', 'transferwise']
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
        const preset = obterPreset(chave);
        return {
          chave,
          nomeExibicao: item.nome,
          corFundo: preset?.fundo,
          corIcone: preset?.cor
        };
      }
    }
  }

  return { chave: null, nomeExibicao: nome };
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
