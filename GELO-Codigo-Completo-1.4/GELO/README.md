# GELO — Gerenciamento Eletrônico de Livros e Opiniões

Aplicação web GELO com os casos de uso **Manter usuário** e **Login**, alinhada ao modelo Astah do projeto.

## Tecnologias

- Java 17
- Maven
- Spring Boot 3.5.x
- Jakarta Faces (JSF) integrado com Spring Boot por meio do JoinFaces
- Spring Data JPA / Hibernate
- H2 para execução local sem instalar um servidor de banco de dados
- PostgreSQL opcional
- BCrypt para armazenar senhas com hash

## Abrir e executar pelo NetBeans

1. No NetBeans, selecione **File > Open Project** e abra a pasta `GELO` que contém este `pom.xml`.
2. Aguarde o NetBeans concluir a resolução das dependências do Maven.
3. Clique com o botão direito no projeto **GELO** e selecione **Clean and Build**.
4. Depois de `BUILD SUCCESS`, clique com o botão direito no projeto e selecione **Run**. O arquivo `nbactions.xml` vincula a ação Run Project às metas `process-classes` e `spring-boot:run`, evitando a configuração genérica `exec:exec`.
5. Na primeira execução, o Maven pode baixar dependências; aguarde a inicialização do servidor na janela **Output**.

O projeto usa Spring Boot com Tomcat embutido. Não o copie para a pasta `webapps` de uma instalação externa do Tomcat 9. Depois de iniciar, abra <http://localhost:8080/>. A raiz mostra a página pública inicial; use **Entrar** para ir ao login ou **Criar conta** para abrir o cadastro. O mapeamento JSF também é configurado para atender a `/login.xhtml` e `/faces/login.xhtml`.

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

## Persistência padrão (H2)

Por padrão, o aplicativo usa um banco H2 em arquivo na pasta `data`, então os usuários permanecem entre reinicializações. O esquema é atualizado com Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Para reiniciar o banco de demonstração do zero, pare a aplicação e apague os arquivos `data/gelodb.mv.db` e `data/gelodb.trace.db`, se existirem. Ao iniciar novamente, o administrador inicial será criado outra vez.

## Usar PostgreSQL opcionalmente

1. Crie um banco chamado `gelodb` no PostgreSQL.
2. Configure as variáveis de ambiente `GELO_DB_URL`, `GELO_DB_USER` e `GELO_DB_PASSWORD`.
3. Inicie com o perfil `postgres`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Alternativamente, configure o perfil ativo pela variável `SPRING_PROFILES_ACTIVE=postgres`. O arquivo `application-postgres.properties` contém os padrões de conexão.

## Como o código se relaciona com o Astah

- `Usuario`: contém identificador, nome, e-mail, senha e tipo de usuário.
- `TipoUsuario`: enum com `ADMINISTRADOR` e `LEITOR`.
- `UsuarioDTO`: apresenta os dados de conta para as telas sem expor a senha.
- `UsuarioRepository`: operações de persistência JPA.
- `UsuarioService`: regras de cadastro, autenticação, edição e exclusão.
- `UsuarioBean` e `LoginBean`: comportamentos acionados pelas páginas JSF.

O código anterior combinava Jakarta EE 10 com bibliotecas antigas de JSF, CDI e Hibernate e usava o `id` de forma inconsistente. A versão atual unifica a aplicação em Spring Boot + JoinFaces e corrige o mapeamento do identificador e da enumeração de perfil. As contas criadas no formulário normal são sempre `LEITOR`; o administrador inicial é criado pela configuração para que seja possível acessar as funções administrativas no primeiro uso.

## Testes

Execute:

```powershell
mvn test
```

Os testes de serviço verificam o cadastro, o hash de senha, a autenticação e a prevenção de e-mails duplicados.
