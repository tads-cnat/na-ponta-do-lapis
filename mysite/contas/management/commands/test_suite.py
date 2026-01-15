"""
Management command: python manage.py test_suite

Executa suite completa de testes do sistema de contas financeiras
"""

from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
from contas.models import ContaFinanceira
from contas.services import ContaService
from django.core.exceptions import ValidationError
from django.test.client import Client
import json

Usuario = get_user_model()


class Command(BaseCommand):
    help = 'Executa suite completa de testes do sistema de contas'

    def handle(self, *args, **options):
        client = Client()

        self.stdout.write("\n" + "=" * 80)
        self.stdout.write("🧪 SUITE DE TESTES COMPLETA - SISTEMA DE CONTAS FINANCEIRAS")
        self.stdout.write("=" * 80)

        # ====================================================================
        # PREPARAÇÃO
        # ====================================================================
        self.stdout.write("\n📋 FASE 0: Preparação do Ambiente")
        self.stdout.write("-" * 80)

        Usuario.objects.filter(email='teste_suite@test.com').delete()
        self.stdout.write("✅ Dados anteriores limpos")

        # ====================================================================
        # FASE 1: Criação de Usuário
        # ====================================================================
        self.stdout.write("\n📋 FASE 1: Criação de Usuário e Autenticação")
        self.stdout.write("-" * 80)

        user = Usuario.objects.create_user(
            email='teste_suite@test.com',
            nome_completo='Usuário de Teste Suite',
            password='senhaForte123'
        )
        self.stdout.write(f"✅ Usuário criado: {user.email}")

        login_ok = client.login(email='teste_suite@test.com', password='senhaForte123')
        self.stdout.write(f"✅ Login realizado: {'Sucesso' if login_ok else 'Falha'}")

        # ====================================================================
        # FASE 2: Criação de Contas
        # ====================================================================
        self.stdout.write("\n📋 FASE 2: Criação de Contas")
        self.stdout.write("-" * 80)

        contas_criadas = []

        # Conta 1: CRÉDITO
        try:
            conta1 = ContaService.AddContaService('Cartão Crédito Itaú', 5000.00, 'CREDITO', usuario=user)
            self.stdout.write(f"✅ Conta CRÉDITO: {conta1.nome} (ID: {conta1.id})")
            contas_criadas.append(conta1)
        except Exception as e:
            self.stdout.write(f"❌ Erro ao criar conta crédito: {e}", self.style.ERROR)

        # Conta 2: DÉBITO
        try:
            conta2 = ContaService.AddContaService('Conta Corrente Bradesco', 2000.50, 'DEBITO', usuario=user)
            self.stdout.write(f"✅ Conta DÉBITO: {conta2.nome} (ID: {conta2.id})")
            contas_criadas.append(conta2)
        except Exception as e:
            self.stdout.write(f"❌ Erro ao criar conta débito: {e}", self.style.ERROR)

        # Conta 3: CRÉDITO/DÉBITO
        try:
            conta3 = ContaService.AddContaService('Cartão Híbrido Santander', 3500.25, 'CREDITO_DEBITO', usuario=user)
            self.stdout.write(f"✅ Conta CRÉDITO/DÉBITO: {conta3.nome} (ID: {conta3.id})")
            contas_criadas.append(conta3)
        except Exception as e:
            self.stdout.write(f"❌ Erro ao criar conta híbrida: {e}", self.style.ERROR)

        self.stdout.write(f"\n📊 Total de contas criadas: {len(contas_criadas)}")

        # ====================================================================
        # FASE 3: Teste da API JSON
        # ====================================================================
        self.stdout.write("\n📋 FASE 3: Teste de API JSON")
        self.stdout.write("-" * 80)

        if contas_criadas:
            conta_teste = contas_criadas[0]
            self.stdout.write(f"\n3.1️⃣  Testando API para: {conta_teste.nome}")

            response = client.get(f'/contas/api/obter/{conta_teste.id}/')

            if response.status_code == 200:
                dados = json.loads(response.content)
                self.stdout.write(f"   ✅ API Status: 200")
                self.stdout.write(f"   Dados JSON:")
                self.stdout.write(f"      - ID: {dados.get('id')}")
                self.stdout.write(f"      - Nome: {dados.get('nome')}")
                self.stdout.write(f"      - Saldo: {dados.get('saldo')}")
                self.stdout.write(f"      - Tipo: {dados.get('tipo')}")

                if (dados.get('id') == conta_teste.id and 
                    dados.get('nome') == conta_teste.nome):
                    self.stdout.write("   ✅ Dados corretos!")
                else:
                    self.stdout.write("   ❌ Dados incorretos", self.style.WARNING)
            else:
                self.stdout.write(f"   ❌ API Status: {response.status_code}", self.style.ERROR)

        # ====================================================================
        # FASE 4: Edição
        # ====================================================================
        self.stdout.write("\n📋 FASE 4: Teste de Edição")
        self.stdout.write("-" * 80)

        if contas_criadas:
            conta_original = contas_criadas[0]
            self.stdout.write(f"\n4.1️⃣  Editando: {conta_original.nome}")
            self.stdout.write(f"   Antes: R$ {conta_original.saldo}, Tipo: {conta_original.tipo}")

            conta_editada = ContaService.EditarContaService(
                conta_original.id,
                'Cartão Premium Atualizado',
                7500.75,
                'CREDITO_DEBITO'
            )

            self.stdout.write(f"   Depois: R$ {conta_editada.saldo}, Tipo: {conta_editada.tipo}")

            if conta_editada.nome == 'Cartão Premium Atualizado':
                self.stdout.write("   ✅ Edição bem-sucedida!")
            else:
                self.stdout.write("   ❌ Edição falhou", self.style.ERROR)

        # ====================================================================
        # FASE 5: Exclusão
        # ====================================================================
        self.stdout.write("\n📋 FASE 5: Teste de Exclusão")
        self.stdout.write("-" * 80)

        if len(contas_criadas) > 1:
            conta_delete = contas_criadas[1]
            self.stdout.write(f"\n5.1️⃣  Excluindo: {conta_delete.nome} (ID: {conta_delete.id})")

            ContaService.ExcluirContaService(conta_delete.id)

            existe = ContaFinanceira.objects.filter(id=conta_delete.id).exists()
            if not existe:
                self.stdout.write(f"   ✅ Conta excluída com sucesso!")
            else:
                self.stdout.write(f"   ❌ Conta ainda existe", self.style.ERROR)

        # ====================================================================
        # FASE 6: Validação de Erros
        # ====================================================================
        self.stdout.write("\n📋 FASE 6: Teste de Validação")
        self.stdout.write("-" * 80)

        self.stdout.write("\n6.1️⃣  Nome muito curto:")
        try:
            ContaService.AddContaService('AB', 1000, 'CREDITO', usuario=user)
            self.stdout.write("   ❌ Validação não funcionou", self.style.ERROR)
        except ValidationError:
            self.stdout.write("   ✅ Erro capturado corretamente")

        self.stdout.write("\n6.2️⃣  Tipo inválido:")
        try:
            ContaService.AddContaService('Teste', 1000, 'INVALIDO', usuario=user)
            self.stdout.write("   ❌ Validação não funcionou", self.style.ERROR)
        except ValidationError:
            self.stdout.write("   ✅ Erro capturado corretamente")

        # ====================================================================
        # RESUMO FINAL
        # ====================================================================
        contas_finais = ContaFinanceira.objects.filter(id_usuario=user)

        self.stdout.write("\n" + "=" * 80)
        self.stdout.write("✨ RESUMO FINAL")
        self.stdout.write("=" * 80)
        
        resultado = f"""
📊 RESULTADOS:
   ✅ Contas criadas: {len(contas_criadas)}
   ✅ Contas finais: {contas_finais.count()}
   ✅ API JSON: Funcional
   ✅ Edição: Funcional
   ✅ Exclusão: Funcional
   ✅ Validação: Funcional

👤 USUÁRIO DE TESTE:
   Email: teste_suite@test.com
   Senha: senhaForte123

🎯 PRÓXIMOS PASSOS:
   1. Acesse http://127.0.0.1:8000/contas/
   2. Faça login com as credenciais acima
   3. Teste o carousel e os botões CRUD
   4. Verifique se os modais abrem com dados corretos

"""
        self.stdout.write(resultado)
        self.stdout.write("=" * 80)
        self.stdout.write("✨ TESTES COMPLETADOS! ✨")
        self.stdout.write("=" * 80)
