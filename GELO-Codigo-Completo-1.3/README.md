# GELO — código completo com mensagens de validação

Protótipo acadêmico com os casos de uso **Manter usuário** e **Login**.

## Tecnologias

- Java 17
- Maven
- Spring Boot 3.5.x
- Jakarta Faces (JSF) via JoinFaces
- Spring MVC
- Spring Data JPA / Hibernate
- H2 em arquivo para uso local
- PostgreSQL opcional
- BCrypt para armazenar hashes de senha

## Como executar no Windows

1. Instale JDK 17 ou superior e Maven.
2. Extraia este ZIP.
3. Abra o PowerShell na pasta `GELO_codigo_completo`, no mesmo nível do `pom.xml`.
4. Execute:

```powershell
mvn clean spring-boot:run
```

Ou dê duplo clique em `executar.bat`.

Mantenha o terminal aberto enquanto utiliza a aplicação. Acesse:

- Aplicação: http://localhost:8080/
- Login: http://localhost:8080/login.xhtml
- Criar conta: http://localhost:8080/cadastro.xhtml

O projeto utiliza servidor Tomcat embutido. Não copie o projeto para a pasta `webapps` do Tomcat 9.

## Administrador local inicial

Na primeira inicialização é criada uma conta de administrador, caso ela ainda não exista:

- E-mail: `admin@gelo.local`
- Senha: `Admin123!`

Essas credenciais são somente para desenvolvimento local. Altere as propriedades `gelo.bootstrap-admin.email` e `gelo.bootstrap-admin.senha` antes de qualquer implantação compartilhada ou pública.

## Mensagens de validação

Os formulários exibem mensagens claras em português para evitar mensagens técnicas do JSF:

- Nome vazio: **O nome deve ser preenchido.**
- E-mail vazio: **O e-mail deve ser preenchido.**
- E-mail inválido: **Informe um endereço de e-mail válido.**
- Senha vazia: **A senha deve ser preenchida.**
- Senha fora do limite: **A senha deve ter entre 6 e 20 caracteres.**
- Nome muito curto: **O nome deve ter entre 2 e 100 caracteres.**
- E-mail duplicado: **Já existe uma conta cadastrada com esse e-mail.**

As páginas `cadastro.xhtml`, `usuarios.xhtml` e `conta.xhtml` mostram mensagens junto aos campos e um resumo de validação. As regras também são verificadas na camada de serviço, para que não dependam apenas da interface.

## Funcionalidades implementadas

### Login
- Autenticação por e-mail e senha.
- Senha conferida por BCrypt.
- Mensagem genérica para credenciais inválidas.
- Sessão autenticada e diferenciação entre `ADMINISTRADOR` e `LEITOR`.
- Logout com invalidação da sessão.
- Proteção de páginas privadas.

### Manter usuário
- Cadastro público de leitores (não é possível selecionar Administrador pelo formulário).
- Validação de nome, e-mail e senha.
- Bloqueio de e-mail duplicado, sem distinção entre maiúsculas e minúsculas.
- Edição dos dados da própria conta.
- Administrador pode listar, cadastrar, editar e excluir outras contas.
- A senha nunca é incluída no DTO usado para exibir os dados.
- O administrador não pode excluir a última conta administrativa.

## Banco de dados

Por padrão, os dados são salvos no banco H2 em arquivo na pasta `data/gelodb` e permanecem entre inicializações.

Para reiniciar o banco local de demonstração:
1. Pare o servidor com `Ctrl+C`.
2. Apague `data/gelodb.mv.db` (e `data/gelodb.trace.db`, caso exista).
3. Inicie novamente. A conta administrativa local será recriada.

## PostgreSQL (opcional)

Crie o banco `gelodb`, defina as variáveis `GELO_DB_URL`, `GELO_DB_USER` e `GELO_DB_PASSWORD` e ative o perfil `postgres`, por exemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
mvn spring-boot:run
```

## Testes

```powershell
mvn test
```
