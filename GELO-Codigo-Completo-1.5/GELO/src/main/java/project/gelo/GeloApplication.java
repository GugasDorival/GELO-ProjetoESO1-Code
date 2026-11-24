package project.gelo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class GeloApplication {
    public static void main(String[] args) {
        criarPastaDoBanco();
        SpringApplication.run(GeloApplication.class, args);
    }

    /**
     * O SQLite cria o arquivo gelo.db, mas não cria a pasta onde ele fica.
     * Por isso a pasta "data" é criada aqui, antes de o Spring abrir o banco,
     * independentemente da pasta de onde o programa foi executado.
     */
    private static void criarPastaDoBanco() {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível criar a pasta 'data' do banco SQLite.", ex);
        }
    }
}
