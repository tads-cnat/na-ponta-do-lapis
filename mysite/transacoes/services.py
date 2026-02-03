from django.core.exceptions import ValidationError
from django.db.models.functions import Lower
from django.core.paginator import Paginator
from django.db.models import Q
from django.db import transaction
from datetime import datetime
from .models import Transacao
from contas.models import ContaFinanceira
from contas.services import ContaService
from categoria.models import Marcador

class TransacaoService:

    @staticmethod
    def salvar_transacao_db(descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):

        try:
            with transaction.atomic():
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

                    if marcadores.count() > 1:
                        raise ValidationError({'marcadores':'So é permitido no maximo 1 marcadores por transação.'})
                    
                    if marcadores.count() != len(marcadores_ids):
                        raise ValidationError({'marcadores':'1 ou mais marcadores não foram estão invalidos'})
                    transacao.marcadores.set(marcadores)

                if tipo == "receita" and estado == "realizada":
                    ContaService.aumentar_saldo(conta_id=conta_financeira_id, valor_transacao= float(valor))
                elif tipo == "despesa" and estado == "realizada":
                    ContaService.diminuir_saldo(conta_id=conta_financeira_id, valor_transacao= float(valor))
                elif estado == "pendente":
                    pass
                else:
                    raise ValidationError({'tipo': "Tipo de transação não registrada."})

                return transacao
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':'Conta Financeira Inexistente.'})
  
    @staticmethod
    def salvar_transacao_sessao(request, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        transacoes = request.session.get("transacoes", [])
        contador = request.session.get("contador_id", "")

        if not contador:
            contador = 1
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
    def editar_transacao_db(id_transacao, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids):

        try:
            with transaction.atomic():
                transacao = TransacaoService.obter_transacoes_id(id_transacao)
                conta =  ContaFinanceira.objects.get(id=conta_financeira_id)

                if not transacao.estado == "pendente":
                    if transacao.tipo == "despesa":
                        ContaService.aumentar_saldo(conta_financeira_id, float(transacao.valor))
                    elif transacao.tipo == "receita":
                        ContaService.diminuir_saldo(conta_financeira_id, float(transacao.valor))
                 
        
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

                if tipo == "receita" and estado == "realizada":
                    ContaService.aumentar_saldo(conta_id=conta_financeira_id, valor_transacao= float(valor))
                elif tipo == "despesa" and estado == "realizada":
                    ContaService.diminuir_saldo(conta_id=conta_financeira_id, valor_transacao= float(valor))
             
                return transacao
        
        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
        except ContaFinanceira.DoesNotExist:
            raise ValidationError({'conta_financeira':"Conta Financeira não encontrada"})
        
    @staticmethod
    def editar_transacao_sessao(request, id_transacao, descricao, valor, categoria, estado, tipo, data_hora, conta_financeira_id, marcadores_ids = None ):
        transacoes = request.session.get("transacoes", [])

        data_hora = datetime.strptime(data_hora,'%Y-%m-%dT%H:%M')
        data_hora = data_hora.strftime('%Y-%m-%d %H:%M:%S')

        t = {
            "id": id_transacao,
            "descricao":descricao,
            "valor":valor,
            "categoria":categoria,
            "estado":estado,
            "tipo":tipo,
            "data_hora":data_hora,
            "conta_financeira_id":conta_financeira_id,
            "marcadores_ids":marcadores_ids
        }

        for i, transacao in enumerate(transacoes):
            if transacao["id"] == id_transacao:
                transacoes[i] = t
                break

        request.session["transacoes"] = transacoes
        request.session.modified = True
    
    @staticmethod
    def excluir_transacao_db(id_transacao):
        try:
           with transaction.atomic():
                t = TransacaoService.obter_transacoes_id(id_transacao)

                if t.estado == "realizada":
                    if t.tipo == "receita":
                        ContaService.diminuir_saldo(t.conta_financeira.id, float(t.valor))
                    elif t.tipo == "despesa":
                        ContaService.aumentar_saldo(t.conta_financeira.id, float(t.valor))
                    else:
                        raise ValidationError()
                    
                t.delete()

        except Transacao.DoesNotExist:
            raise ValidationError({'transacao':'Transação não encontrada'})
        
    @staticmethod
    def excluir_transacao_sessao(request, id_transacao):
        transacoes = request.session.get("transacoes")
        
        for t in transacoes:
            if t['id'] == id_transacao:
                transacoes.remove(t)
                request.session["transacoes"] = transacoes
                request.session.modified = True
                

        
    @staticmethod
    def obter_minhas_transacoes(usuario, order=None, direcao="asc", numero_pag=1):
        contas = ContaService.obter_contas_usuario(usuario.id)
        transacoes = Transacao.objects.filter(conta_financeira__in=contas).order_by('-id')

        if order:
            if direcao == "desc":
                transacoes = transacoes.order_by(Lower(order).desc())
            else:
                transacoes = transacoes.order_by(Lower(order))

        paginator = Paginator(transacoes, 8) 
        pag_transacao = paginator.get_page(numero_pag)

        return pag_transacao
    
   
    @staticmethod
    def obter_transacoes_id(transacao_id):
        return Transacao.objects.get(id=transacao_id)
    
    
    @staticmethod
    def filtrar_transacao(request, usuario, busca=None, categoria=None, tipo=None, conta=None, data_inicio=None, data_fim=None):
        if request.user.is_authenticated:
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

        else:
            transacoes = request.session.get("transacoes", None)
            if busca:
                transacoes = [t for t in transacoes if busca.lower() in t['descricao'].lower()]

            if categoria:
                transacoes = [ t for t in transacoes if categoria == t['categoria']]

            if tipo:
                transacoes = [ t for t in transacoes if tipo == t['tipo']]

            if conta:
                transacoes = [ t for t in transacoes if conta == t['conta']]

            if data_inicio:
                data_inicio = datetime.strptime(data_inicio, "%Y-%m-%d").date()
                transacoes = [
                    t for t in transacoes
                    if datetime.strptime(t['data_hora'], "%Y-%m-%d %H:%M:%S").date() >= data_inicio
                ]

            if data_fim:
                data_fim = datetime.strptime(data_fim, "%Y-%m-%d").date()
                transacoes = [
                    t for t in transacoes
                    if datetime.strptime(t['data_hora'], "%Y-%m-%d %H:%M:%S").date() <= data_fim
                ]
                
            for t in transacoes:
                t["data_hora"] = datetime.strptime(
                    t["data_hora"],
                    "%Y-%m-%d %H:%M:%S"
                )
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
    
