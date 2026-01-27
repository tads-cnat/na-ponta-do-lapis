from .models import Marcador

class MarcadorService:
    @staticmethod
    def criar_marcador(nome: str, cor: str, usuario):
        if not nome:
            raise ValueError("Nome é obrigatório.")
        if not cor:
            raise ValueError("Cor é obrigatória.")
        if not usuario or not usuario.is_authenticated:
            raise ValueError("Usuário inválido.")

        marcador = Marcador.objects.create(
            nome=nome,
            cor=cor,
            usuario=usuario
        )
        return marcador


    @staticmethod
    def listar_marcadores(usuario):
        return list(Marcador.objects
                .filter(usuario=usuario)
                .values("id", "nome", "cor"))
    
    @staticmethod
    def excluir_marcador(marcador_id: int):
        try:
            marcador = Marcador.objects.get(id=marcador_id)
        except Marcador.DoesNotExist:
            raise ValueError("Marcador não encontrado.")

        marcador.delete()

    @staticmethod
    def editar_marcador(marcador_id: int, nome: str):
        if not nome:
            raise ValueError("Nome é obrigatório.")

        try:
            marcador = Marcador.objects.get(id=marcador_id)
        except Marcador.DoesNotExist:
            raise ValueError("Marcador não encontrado.")

        marcador.nome = nome
        marcador.save()
        return marcador