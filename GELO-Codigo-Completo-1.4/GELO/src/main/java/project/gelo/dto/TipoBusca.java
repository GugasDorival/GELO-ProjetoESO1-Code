package project.gelo.dto;

/** Campo em que o termo digitado será procurado na API Google Books. */
public enum TipoBusca {
    /** Procura o termo no título do livro (operador intitle). */
    TITULO("Título"),
    /** Procura o termo no nome do autor (operador inauthor). */
    AUTOR("Autor"),
    /** Busca livre: título, autor, assunto e demais campos indexados pelo Google. */
    GERAL("Geral");

    private final String rotulo;

    TipoBusca(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Converte o texto recebido da página; valores vazios ou desconhecidos viram GERAL. */
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
