package project.gelo.dto;

public enum TipoBusca {
    // Procura o termo no título do livro (operador intitle)
    TITULO("Título"),
    // Procura o termo no nome do autor (operador inauthor)
    AUTOR("Autor"),
    // Busca livre: título, autor, assunto e demais campos indexados pelo Google
    GERAL("Geral");

    private final String rotulo;

    TipoBusca(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static TipoBusca de(String valor) {
        if (valor != null) {
            for (TipoBusca tipo : values()) {
                if (tipo.name().equalsIgnoreCase(valor.trim())) {
                    return tipo;
                }
            }
        }
        return GERAL;
    }
}
