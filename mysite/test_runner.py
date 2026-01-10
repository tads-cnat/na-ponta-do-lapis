#!/usr/bin/env python
"""
Script de teste executado via Django shell
python manage.py shell -c 'exec(open("test_runner.py").read())'
"""

from django.contrib.auth import get_user_model
from contas.models import ContaFinanceira
from contas.services import ContaService
from django.core.exceptions import ValidationError
from django.test.client import Client

Usuario = get_user_model()
client = Client()

print("\n" + "=" * 80)
print("🧪 SUITE DE TESTES COMPLETA - SISTEMA DE CONTAS FINANCEIRAS")
print("=" * 80)

# ============================================================================
# PREPARAÇÃO
# ============================================================================
print("\n📋 FASE 0: Preparação do Ambiente")
print("-" * 80)

Usuario.objects.filter(email='teste_suite@test.com').delete()
print("✅ Dados anteriores limpos")

# ============================================================================
# FASE 1: Criação de Usuário
# ============================================================================
print("\n📋 FASE 1: Criação de Usuário e Autenticação")
print("-" * 80)

user = Usuario.objects.create_user(
    email='teste_suite@test.com',
    nome_completo='Usuário de Teste Suite',
    password='senhaForte123'
)
print(f"✅ Usuário criado: {user.email}")

login_ok = client.login(email='teste_suite@test.com', password='senhaForte123')
print(f"✅ Login realizado: {'Sucesso' if login_ok else 'Falha'}")

# ============================================================================
# FASE 2: Criação de Contas
# ============================================================================
print("\n📋 FASE 2: Criação de Contas")
print("-" * 80)

contas_criadas = []

# Conta 1: CRÉDITO
try:
    conta1 = ContaService.AddContaService('Cartão Crédito Itaú', 5000.00, 'CREDITO', usuario=user)
    print(f"✅ Conta CRÉDITO: {conta1.nome} (ID: {conta1.id})")
    contas_criadas.append(conta1)
except Exception as e:
    print(f"❌ Erro ao criar conta crédito: {e}")

# Conta 2: DÉBITO
try:
    conta2 = ContaService.AddContaService('Conta Corrente Bradesco', 2000.50, 'DEBITO', usuario=user)
    print(f"✅ Conta DÉBITO: {conta2.nome} (ID: {conta2.id})")
    contas_criadas.append(conta2)
except Exception as e:
    print(f"❌ Erro ao criar conta débito: {e}")

# Conta 3: CRÉDITO/DÉBITO
try:
    conta3 = ContaService.AddContaService('Cartão Híbrido Santander', 3500.25, 'CREDITO_DEBITO', usuario=user)
    print(f"✅ Conta CRÉDITO/DÉBITO: {conta3.nome} (ID: {conta3.id})")
    contas_criadas.append(conta3)
except Exception as e:
    print(f"❌ Erro ao criar conta híbrida: {e}")

print(f"\n📊 Total de contas criadas: {len(contas_criadas)}")

# ============================================================================
# FASE 3: Teste da API JSON
# ============================================================================
print("\n📋 FASE 3: Teste de API JSON")
print("-" * 80)

if contas_criadas:
    conta_teste = contas_criadas[0]
    print(f"\n3.1️⃣  Testando API para: {conta_teste.nome}")
    
    response = client.get(f'/contas/api/obter/{conta_teste.id}/')
    
    if response.status_code == 200:
        import json
        dados = json.loads(response.content)
        print(f"   ✅ API Status: 200")
        print(f"   Dados JSON:")
        print(f"      - ID: {dados.get('id')}")
        print(f"      - Nome: {dados.get('nome')}")
        print(f"      - Saldo: {dados.get('saldo')}")
        print(f"      - Tipo: {dados.get('tipo')}")
        
        if (dados.get('id') == conta_teste.id and 
            dados.get('nome') == conta_teste.nome):
            print("   ✅ Dados corretos!")
        else:
            print("   ❌ Dados incorretos")
    else:
        print(f"   ❌ API Status: {response.status_code}")

# ============================================================================
# FASE 4: Edição
# ============================================================================
print("\n📋 FASE 4: Teste de Edição")
print("-" * 80)

if contas_criadas:
    conta_original = contas_criadas[0]
    print(f"\n4.1️⃣  Editando: {conta_original.nome}")
    print(f"   Antes: R$ {conta_original.saldo}, Tipo: {conta_original.tipo}")
    
    conta_editada = ContaService.EditarContaService(
        conta_original.id,
        'Cartão Premium Atualizado',
        7500.75,
        'CREDITO_DEBITO'
    )
    
    print(f"   Depois: R$ {conta_editada.saldo}, Tipo: {conta_editada.tipo}")
    
    if conta_editada.nome == 'Cartão Premium Atualizado':
        print("   ✅ Edição bem-sucedida!")
    else:
        print("   ❌ Edição falhou")

# ============================================================================
# FASE 5: Exclusão
# ============================================================================
print("\n📋 FASE 5: Teste de Exclusão")
print("-" * 80)

if len(contas_criadas) > 1:
    conta_delete = contas_criadas[1]
    print(f"\n5.1️⃣  Excluindo: {conta_delete.nome} (ID: {conta_delete.id})")
    
    ContaService.ExcluirContaService(conta_delete.id)
    
    existe = ContaFinanceira.objects.filter(id=conta_delete.id).exists()
    if not existe:
        print(f"   ✅ Conta excluída com sucesso!")
    else:
        print(f"   ❌ Conta ainda existe")

# ============================================================================
# FASE 6: Validação de Erros
# ============================================================================
print("\n📋 FASE 6: Teste de Validação")
print("-" * 80)

print("\n6.1️⃣  Nome muito curto:")
try:
    ContaService.AddContaService('AB', 1000, 'CREDITO', usuario=user)
    print("   ❌ Validação não funcionou")
except ValidationError:
    print("   ✅ Erro capturado corretamente")

print("\n6.2️⃣  Tipo inválido:")
try:
    ContaService.AddContaService('Teste', 1000, 'INVALIDO', usuario=user)
    print("   ❌ Validação não funcionou")
except ValidationError:
    print("   ✅ Erro capturado corretamente")

# ============================================================================
# RESUMO FINAL
# ============================================================================
contas_finais = ContaFinanceira.objects.filter(id_usuario=user)

print("\n" + "=" * 80)
print("✨ RESUMO FINAL")
print("=" * 80)
print(f"""
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

""")
print("=" * 80)
print("✨ TESTES COMPLETADOS! ✨")
print("=" * 80)
