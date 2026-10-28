package com.npl.na_ponta_do_lapis;

import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class NaPontaDoLapisApplication {

	public static void main(String[] args) {
		Dotenv dotenv = carregarDotenv();
		if (dotenv != null) {
			dotenv.entries().forEach(entry -> {
				// Só define no System properties se não tiver sido passado externamente
				if (System.getProperty(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		}

		SpringApplication.run(NaPontaDoLapisApplication.class, args);
	}

	private static Dotenv carregarDotenv() {
		// 1. Diretório de trabalho atual (ex: backend/na-ponta-do-lapis)
		if (new File(".env").exists()) {
			return Dotenv.configure().ignoreIfMissing().load();
		}
		// 2. Raiz do projeto a partir do backend (../../.env)
		if (new File("../../.env").exists()) {
			return Dotenv.configure().directory("../../").ignoreIfMissing().load();
		}
		// 3. Diretório pai imediato (../.env)
		if (new File("../.env").exists()) {
			return Dotenv.configure().directory("../").ignoreIfMissing().load();
		}
		// 4. Fallback silencioso (usa variáveis de ambiente do SO ou container)
		return Dotenv.configure().ignoreIfMissing().load();
	}

}
