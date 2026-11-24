# GELO — Gerenciamento Eletrônico de Livros e Opiniões

Aplicação web GELO com os casos de uso **Manter usuário** e **Login**, alinhada ao modelo Astah do projeto.

## Tecnologias

- Java 17
- Maven
- Spring Boot 3.5.x
- Jakarta Faces (JSF) integrado com Spring Boot por meio do JoinFaces
- Spring Data JPA / Hibernate
- SQLite (arquivo local `data/gelo.db`) para execução sem instalar um servidor de banco de dados
- BCrypt para armazenar senhas com hash

## Abrir e executar pelo NetBeans

1. No NetBeans, selecione **File > Open Project** e abra a pasta `GELO` que contém este `pom.xml`.
2. Aguarde o NetBeans concluir a resolução das dependências do Maven.
3. Clique com o botão direito no projeto **GELO** e selecione **Clean and Build**.
4. Depois de `BUILD SUCCESS`, clique com o botão direito no projeto e selecione **Run**. O arquivo `nbactions.xml` vincula a ação Run Project às metas `process-classes` e `spring-boot:run`, evitando a configuração genérica `exec:exec`.
5. Na primeira execução, o Maven pode baixar dependências; aguarde a inicialização do servidor na janela **Output**.

O projeto usa Spring Boot com Tomcat embutido. Não o copie para a pasta `webapps` de uma instalação externa do Tomcat 9. Depois de iniciar, abra <http://localhost:8081/>. A raiz mostra a página pública inicial; use **Entrar** para ir ao login ou **Criar conta** para abrir o cadastro. O mapeamento JSF também é configurado para atender a `/view/html/usuario/entrar.xhtml` e `/faces/view/html/usuario/entrar.xhtml`.

Se o NetBeans já tiver uma ação personalizada de Run armazenada localmente, confira em **Project Properties > Actions > Run project** se o campo **Execute Goals** está como `process-classes spring-boot:run` e deixe **Set Properties** vazio.

## Acesso inicial para testes locais

Na primeira inicialização, o sistema cria um administrador caso a conta configurada ainda não exista:

- **E-mail:** `admin@gelo.local`
- **Senha:** `Admin123!`

Essas credenciais são somente para desenvolvimento local. Altere `gelo.bootstrap-admin.email` e `gelo.bootstrap-admin.senha` em `src/main/resources/application.properties` antes de qualquer implantação compartilhada ou pública. A senha é gravada no banco como hash BCrypt, e não em texto puro.

Depois de entrar como administrador, abra **Gerenciar usuários** para listar, criar contas de leitor, editar dados ou excluir contas. Para testar o perfil normal, saia e cadastre um novo leitor na página **Criar uma conta**.

## Funcionalidades implementadas

### Login

- Login por e-mail e senha.
- Mensagem genérica para credenciais inválidas.
- Senha validada usando BCrypt.
- Sessão autenticada com diferenciação entre `ADMINISTRADOR` e `LEITOR`.
- Botão para sair e invalidação da sessão.
- Proteção das páginas privadas por verificação de sessão; a página de administração confere o perfil.

### Manter usuário

- Cadastro público de leitor. O formulário não permite escolher o perfil de administrador, conforme a anotação do modelo Astah.
- Validação de nome, formato de e-mail e senha entre 6 e 20 caracteres.
- Bloqueio de e-mail duplicado, inclusive diferenças entre maiúsculas e minúsculas.
- Consulta de contas na página administrativa.
- Edição de nome, e-mail e senha; deixar a nova senha em branco mantém a senha existente.
- Exclusão da própria conta pelo usuário.
- Administrador pode cadastrar leitores, editar contas e excluir outras contas.
- Não é possível excluir o último administrador.
- O DTO usado pelas páginas não inclui senha ou hash.

## Persistência (SQLite)

O aplicativo usa um banco SQLite em um único arquivo, `data/gelo.db`, então os usuários permanecem entre reinicializações. Não é preciso instalar nenhum servidor de banco de dados: o driver `sqlite-jdbc` já traz o SQLite embutido e o dialeto `hibernate-community-dialects` permite usá-lo com JPA/Hibernate. O arquivo é criado automaticamente na primeira execução, e o esquema é atualizado com Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

O caminho `./data/gelo.db` é relativo à pasta de onde o programa é executado (no VS Code, normalmente a pasta aberta no editor). A pasta `data` é criada automaticamente na inicialização, porque o SQLite cria o arquivo do banco, mas não a pasta onde ele fica.

Para reiniciar o banco de demonstração do zero, pare a aplicação e apague os arquivos `data/gelo.db` (e `data/gelo.db-journal`, se existir). Ao iniciar novamente, o administrador inicial será criado outra vez.

Observações sobre o SQLite:

- Ele aceita uma escrita por vez, por isso o pool de conexões está limitado a 1 (`spring.datasource.hikari.maximum-pool-size=1`).
- As chaves estrangeiras vêm desligadas por padrão; a URL de conexão usa `foreign_keys=on` para ativá-las.
- Ele altera tabelas de forma limitada. Se o modelo (as entidades) mudar durante o desenvolvimento e o `update` não conseguir ajustar a tabela, apague o arquivo `data/gelo.db` e inicie de novo.

## Como o código se relaciona com o Astah

- `Usuario`: contém identificador, nome, e-mail, senha e tipo de usuário.
- `TipoUsuario`: enum com `ADMINISTRADOR` e `LEITOR`.
- `UsuarioDTO`: apresenta os dados de conta para as telas sem expor a senha.
- `UsuarioRepository`: operações de persistência JPA.
- `UsuarioService`: regras de cadastro, autenticação, edição e exclusão.
- `UsuarioBean` e `EntrarBean`: comportamentos acionados pelas páginas JSF.

O código anterior combinava Jakarta EE 10 com bibliotecas antigas de JSF, CDI e Hibernate e usava o `id` de forma inconsistente. A versão atual unifica a aplicação em Spring Boot + JoinFaces e corrige o mapeamento do identificador e da enumeração de perfil. As contas criadas no formulário normal são sempre `LEITOR`; o administrador inicial é criado pela configuração para que seja possível acessar as funções administrativas no primeiro uso.

## Testes

Execute:

```powershell
mvn test
```

Os testes de serviço verificam o cadastro, o hash de senha, a autenticação e a prevenção de e-mails duplicados.
