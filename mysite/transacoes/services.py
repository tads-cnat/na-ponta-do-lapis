from django.core.exceptions import ValidationError
from django.db.models import Q
from datetime import datetime
from django.utils import timezone
from .models import Transacao
from contas.models import ContaFinanceira
from contas.services import ContaService
from categoria.models import Marcador

class TransacaoService:

    @staticmethod
    def salvar_transacao_db(descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        try:
            conta = ContaFinanceira.objects.get(id=conta_financeira_id)

            transacao = Transacao(
                descricao = descricao,
                valor= valor,
                categoria= categoria,
                estado = estado,
                tipo = tipo,
                data_hora = data_hora,
                conta_financeira = conta
            )
            transacao.full_clean()
            transacao.save()

            if marcadores_ids:
                marcadores = Marcador.objects.filter(id__in=marcadores_ids)

                if marcadores.count() > 3:
                    raise ValidationError({'marcadores':'So é permitido no maximo 3 marcadores por transação.'})
                
                if marcadores.count() != len(marcadores_ids):
                    raise ValidationError({'marcadores':'1 ou mais marcadores não foram estão invalidos'})
                transacao.marcadores.set(marcadores)

            return transacao
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':'Conta Financeira Inexistente.'})
  
    @staticmethod
    def salvar_transacao_sessao(request, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        transacoes = request.session.get("transacoes", [])
        contador = request.session.get("contador_id", "")

        if not contador:
            contador = 0
            request.session["contador_id"] = contador
        else:
            contador += 1 
            request.session["contador_id"] = contador 

        data_hora = datetime.strptime(data_hora,'%Y-%m-%dT%H:%M')
        data_hora = data_hora.strftime('%Y-%m-%d %H:%M:%S')

        t = {
            "id": contador,
            "descricao":descricao,
            "valor":valor,
            "categoria":categoria,
            "estado":estado,
            "tipo":tipo,
            "data_hora":data_hora,
            "conta_financeira_id":conta_financeira_id,
            "marcadores_ids":marcadores_ids
        }

        transacoes.append(t)

        request.session["transacoes"] = transacoes
        request.session.modified = True
        
        

    @staticmethod
    def editar_transacao(id_transacao, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids):

        try:
            transacao = TransacaoService.obter_transacoes_id(id_transacao)
            conta =  ContaFinanceira.objects.get(id=conta_financeira_id)

            transacao.descricao = descricao
            transacao.valor = valor
            transacao.categoria = categoria
            transacao.estado = estado
            transacao.tipo = tipo
            transacao.data_hora = data_hora
            transacao.conta_financeira = conta
            
            transacao.full_clean()
            transacao.save()

            if marcadores_ids is not None:
                marcadores = Marcador.objects.filter(id__in=marcadores_ids)

                if marcadores.count() > 3:
                    raise ValidationError({'marcadores':'So é permitido no máximo 3 marcadores por transação.'})
                
                if marcadores.count() != len(marcadores_ids):
                    raise ValidationError({'marcadores':'1 ou mais marcadores não foram estão invalidos'})
                transacao.marcadores.set(marcadores)
      
            return transacao
        
        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':"Conta Financeira não encontrada"})
    
    @staticmethod
    def excluir_transacao(id_transacao):
        try:
           TransacaoService.obter_transacoes_id(id_transacao).delete()
        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
    @staticmethod
    def obter_minhas_transacoes(usuario):
        contas = ContaService.obter_contas_usuario(usuario.id)
        return Transacao.objects.filter(conta_financeira__in=contas)
   
    @staticmethod
    def obter_transacoes_id(transacao_id):
        return Transacao.objects.get(id=transacao_id)
    
    @staticmethod
    def filtrar_transacao(busca, categoria, tipo, conta, data_inicio, data_fim , usuario):
        transacoes = TransacaoService.obter_minhas_transacoes(usuario)
        if busca:
            transacoes = transacoes.filter(descricao__icontains=busca)
        if categoria:
            transacoes = transacoes.filter(categoria=categoria)
        if tipo:
            transacoes = transacoes.filter(tipo=tipo)
        if conta:
            transacoes = transacoes.filter(conta_financeira=conta)
        if data_inicio:
            data_inicio = datetime.strptime(data_inicio, "%Y-%m-%d").date()
            transacoes = transacoes.filter(data_hora__gte = data_inicio)
        if data_fim:
            data_fim = datetime.strptime(data_fim, "%Y-%m-%d").date()
            transacoes = transacoes.filter(data_hora__lte = data_fim)

        return transacoes
        
    @staticmethod
    def obter_categorias():
        return Transacao.Categoria.choices
    
    @staticmethod
    def obter_estados():
        return Transacao.EstadoTransacao.choices
    
    @staticmethod
    def obter_tipos():
        return Transacao.TipoTransacao.choices
    
