from .models import Marcador

class MarcadorService:
    @staticmethod
    def criar_marcador(nome: str, cor: str):
        if not nome:
            raise ValueError("Nome é obrigatório.")
        if not cor:
            raise ValueError("Cor é obrigatória.")

        marcador = Marcador.objects.create(nome=nome, cor=cor)
        return marcador

    @staticmethod
    def listar_marcadores():
        return list(Marcador.objects.values("id", "nome", "cor"))