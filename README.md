# 🙋 API de Dúvidas da Turma

Uma API na nuvem onde a turma registra **dúvidas** e o professor acompanha tudo em um só lugar.
Ela pode ser usada por qualquer projeto: um console em Java, um site em HTML/JS, um app de celular…

```
   Seu projeto  ──┐
   Console Java ──┼──►  🌍 api-duvidas.onrender.com  ──►  🐯 TiDB (compatível com MySQL)
   App do prof. ──┘         🛂 chave · ✅ validação
```

> **Endereço da API:** `https://api-duvidas.onrender.com`
>
> **Documentação interativa (Swagger):** <https://api-duvidas.onrender.com/swagger-ui.html>

---

## 📚 Sumário

1. [⚡ Antes de começar: a API está acordada?](#-antes-de-começar-a-api-está-acordada)
2. [🔑 A chave de acesso](#-a-chave-de-acesso)
3. [🍽️ O cardápio (endpoints)](#️-o-cardápio-endpoints)
4. [📦 Como é uma dúvida](#-como-é-uma-dúvida)
5. [🚦 Respostas e erros](#-respostas-e-erros)
6. [📖 Testando pelo navegador (Swagger)](#-testando-pelo-navegador-swagger)
7. [🧪 Testando no Insomnia](#-testando-no-insomnia)
8. [💻 Exemplos de código](#-exemplos-de-código)
9. [🌐 CORS: chamando pelo navegador](#-cors-chamando-pelo-navegador)
10. [🛟 Deu ruim? Problemas comuns](#-deu-ruim-problemas-comuns)
11. [🛠️ Para quem mantém a API](#️-para-quem-mantém-a-api)

---

## ⚡ Antes de começar: a API está acordada?

A API roda em um plano **gratuito**, que desliga o servidor depois de **15 minutos sem ninguém usar**.
Para isso não acontecer, um **despertador** ⏰ chama a API a cada 10 minutos. Então, normalmente,
ela responde **na hora**.

Em casos raros (logo depois de uma atualização da API, por exemplo), a **primeira chamada** pode levar
**cerca de 1 minuto**. As seguintes são rápidas. 😴➡️😃

**No seu código:** use um tempo de espera (timeout) longo e mostre algo como *"Carregando…"*.
Os [exemplos de código](#-exemplos-de-código) já fazem isso.

**Quer conferir se está tudo no ar?** Abra no navegador: <https://api-duvidas.onrender.com/saude>

```json
{"status": "ok", "duvidas": 12}
```

Se aparecer isso, a API **e** o banco de dados estão funcionando. ✅

---

## 🔑 A chave de acesso

Toda chamada precisa de um **crachá** 🪪: um cabeçalho (header) chamado **`X-API-Key`** com a sua chave.

```http
GET /duvidas
X-API-Key: sua-chave-aqui
```

Existem **dois tipos de chave**:

| Chave | Quem usa | Pode fazer |
|---|---|---|
| 🎓 **ALUNO** | a turma, nos projetos | ver e criar dúvidas |
| 👨‍🏫 **PROFESSOR** | só o professor | tudo, inclusive editar e apagar |

**Como conseguir a chave ALUNO?** Peça ao professor. Ela **não** fica neste repositório, de propósito.

### 🔐 Regras de ouro

- **Nunca** coloque a chave no GitHub. Guarde em um arquivo `.env` e coloque `.env` no seu `.gitignore`.
- **Use só a chave de que você precisa.** Seu projeto só lista e cria dúvidas? Então use a chave ALUNO.
  Isso se chama **princípio do menor privilégio**.
- Em sites (HTML/JS), qualquer pessoa consegue ver a chave abrindo o DevTools (F12).
  Por isso a chave ALUNO **não consegue apagar nada**: mesmo que alguém a encontre, os dados da turma ficam seguros.

---

## 🍽️ O cardápio (endpoints)

| Verbo | Endereço | O que faz | 🎓 ALUNO | 👨‍🏫 PROFESSOR |
|---|---|---|:---:|:---:|
| `GET` | `/saude` | diz se a API **e o banco** estão funcionando (não precisa de chave) | ✅ | ✅ |
| `GET` | `/alo` | diz só se a API está ligada (não precisa de chave) | ✅ | ✅ |
| `GET` | `/duvidas` | lista **todas** as dúvidas | ✅ | ✅ |
| `GET` | `/duvidas/{id}` | busca **uma** dúvida pelo número | ✅ | ✅ |
| `POST` | `/duvidas` | registra uma dúvida **nova** | ✅ | ✅ |
| `PUT` | `/duvidas/{id}` | **edita** a mensagem de uma dúvida | 🚫 | ✅ |
| `DELETE` | `/duvidas/{id}` | **apaga** uma dúvida | 🚫 | ✅ |

> Em `/duvidas/{id}`, troque `{id}` pelo número da dúvida. Exemplo: `/duvidas/3`.

---

## 📦 Como é uma dúvida

A API conversa em **JSON**. Uma dúvida salva é assim:

```json
{
  "id": 3,
  "mensagem": "Como funciona o @RestController?",
  "datahora": "2026-09-30T16:22:20"
}
```

| Campo | Tipo | Quem preenche | Regras |
|---|---|---|---|
| `id` | número | **a API** (automático) | não envie: a API ignora. É **único**, mas **não é sequencial** (veja abaixo) |
| `mensagem` | texto | **você** | **obrigatória**, até **2000** caracteres |
| `datahora` | texto (data e hora) | você **ou** a API | opcional. Se não enviar, a API usa o **horário de Brasília** do momento |

> **Por que os `id` pulam?** Você pode ver uma dúvida 12 e a próxima ser a 30001. O banco (TiDB) roda
> em vários servidores ao mesmo tempo, e cada um reserva um bloco de números para nunca repetir um `id`.
> Então: use o `id` para **identificar** uma dúvida, mas não conte com "a próxima é a 13".

### Criando uma dúvida (`POST /duvidas`)

O mínimo que você precisa mandar:

```json
{
  "mensagem": "Qual a diferença entre 401 e 403?"
}
```

Quer registrar uma dúvida com o horário em que ela **realmente** aconteceu (por exemplo, uma dúvida
anotada offline e enviada depois)? Mande a `datahora` junto, no formato `AAAA-MM-DDTHH:MM:SS`:

```json
{
  "mensagem": "Qual a diferença entre 401 e 403?",
  "datahora": "2026-10-02T09:30:00"
}
```

---

## 🚦 Respostas e erros

Todo pedido volta com um **código de status**. Ele diz o que aconteceu antes mesmo de você ler o corpo da resposta:

| Código | Nome | Quer dizer | Exemplo |
|---|---|---|---|
| **200** | OK | deu certo, e a resposta traz dados | listou ou criou uma dúvida |
| **204** | No Content | deu certo, sem nada para devolver | apagou uma dúvida |
| **400** | Bad Request | **o seu pedido** está errado | mensagem vazia ou grande demais |
| **401** | Unauthorized | "**identifique-se**": faltou a chave, ou ela está errada | esqueceu o header `X-API-Key` |
| **403** | Forbidden | "**sei quem você é, mas você não pode**" | chave ALUNO tentando apagar |
| **404** | Not Found | não existe | `/duvidas/999` |
| **500** | Internal Server Error | o problema foi **na API** | avise quem mantém a API! |

💡 **Dica para decorar:** os códigos **4xx** são culpa de **quem pediu**, os **5xx** são culpa **do servidor**.

### O formato dos erros

Quando uma regra da dúvida é quebrada (**400**), a API explica o motivo em português, na lista `erros`:

```json
{
  "status": 400,
  "erros": ["A mensagem da dúvida é obrigatória."]
}
```

Os outros erros (401, 403, 404…) vêm assim:

```json
{
  "timestamp": "2026-10-04T03:16:16.918Z",
  "status": 401,
  "error": "Unauthorized",
  "path": "/duvidas"
}
```

---

## 📖 Testando pelo navegador (Swagger)

O jeito mais fácil de conhecer e testar a API, sem instalar nada:
<https://api-duvidas.onrender.com/swagger-ui.html>

O Swagger mostra **todos** os endpoints, o que cada um faz, as respostas possíveis (200, 400, 401…)
e um exemplo de cada campo. E ele **gera a documentação a partir do código**: se a API mudar,
a página muda junto.

**Para testar um endpoint:**

1. Clique em **🔒 Authorize** (no topo), cole a sua chave **sem aspas** e clique em **Authorize**.
2. Abra um endpoint, por exemplo `GET /duvidas`, e clique em **Try it out**.
3. Preencha o que for pedido (um `id`, ou o JSON da dúvida) e clique em **Execute**.
4. Logo abaixo aparecem a resposta e o comando `curl` equivalente. 🎉

> ⚠️ O Swagger chama a API **de verdade**: uma dúvida criada por ele aparece para a turma inteira.

Quer a "planta" da API em JSON (formato **OpenAPI**), para gerar código ou importar no Insomnia?
Está em <https://api-duvidas.onrender.com/v3/api-docs>.

---

## 🧪 Testando no Insomnia

1. Crie um **New Request**.
2. Escolha o verbo (`GET`, `POST`…) e cole o endereço, por exemplo `https://api-duvidas.onrender.com/duvidas`.
3. Na aba **Headers**, clique em **Add**:
   - **Name:** `X-API-Key`
   - **Value:** a sua chave (**sem aspas**!)
4. Se for `POST` ou `PUT`: na aba **Body**, escolha **JSON** e escreva a dúvida.
5. **Send**. 🚀

---

## 💻 Exemplos de código

> Nos exemplos, troque `SUA_CHAVE_ALUNO` pela chave que o professor passou.
> Em projetos de verdade, **leia a chave de um arquivo `.env`** em vez de escrever no código.

### JavaScript (no navegador ou no Node.js)

```js
const API = "https://api-duvidas.onrender.com";
const CHAVE = "SUA_CHAVE_ALUNO";

// Lista todas as dúvidas
async function listarDuvidas() {
  const resposta = await fetch(`${API}/duvidas`, {
    headers: { "X-API-Key": CHAVE },
    signal: AbortSignal.timeout(90_000), // espera até 90s: a API pode estar acordando 😴
  });

  if (!resposta.ok) {
    throw new Error(`A API respondeu ${resposta.status}`);
  }
  return resposta.json(); // uma lista de dúvidas
}

// Registra uma dúvida nova
async function enviarDuvida(mensagem) {
  const resposta = await fetch(`${API}/duvidas`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-API-Key": CHAVE,
    },
    body: JSON.stringify({ mensagem }),
    signal: AbortSignal.timeout(90_000),
  });

  const dados = await resposta.json();

  if (resposta.status === 400) {
    // Regra quebrada: a API explica o que foi
    alert(dados.erros.join("\n"));
    return null;
  }
  if (!resposta.ok) {
    throw new Error(`A API respondeu ${resposta.status}`);
  }
  return dados; // a dúvida criada, já com id e datahora
}
```

### Um site completo, em um arquivo só

Salve como `index.html` e abra com o **Live Server** do VS Code:

```html
<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <title>Dúvidas da Turma</title>
</head>
<body>
  <h1>🙋 Dúvidas da Turma</h1>

  <form id="formulario">
    <input id="mensagem" placeholder="Qual é a sua dúvida?" size="50">
    <button>Enviar</button>
  </form>

  <p id="aviso"></p>
  <ul id="lista"></ul>

  <script>
    const API = "https://api-duvidas.onrender.com";
    const CHAVE = "SUA_CHAVE_ALUNO";
    const cabecalhos = { "Content-Type": "application/json", "X-API-Key": CHAVE };

    async function carregar() {
      document.getElementById("aviso").textContent = "Carregando… (se a API estiver dormindo, leva ~1 min 😴)";
      const resposta = await fetch(`${API}/duvidas`, { headers: cabecalhos });
      const duvidas = await resposta.json();

      const lista = document.getElementById("lista");
      lista.innerHTML = "";
      for (const duvida of duvidas) {
        const item = document.createElement("li");
        item.textContent = `#${duvida.id} — ${duvida.mensagem}`;
        lista.appendChild(item);
      }
      document.getElementById("aviso").textContent = "";
    }

    document.getElementById("formulario").addEventListener("submit", async (evento) => {
      evento.preventDefault();
      const campo = document.getElementById("mensagem");

      const resposta = await fetch(`${API}/duvidas`, {
        method: "POST",
        headers: cabecalhos,
        body: JSON.stringify({ mensagem: campo.value }),
      });

      if (resposta.status === 400) {
        const dados = await resposta.json();
        document.getElementById("aviso").textContent = dados.erros.join(" ");
        return;
      }

      campo.value = "";
      carregar();
    });

    carregar();
  </script>
</body>
</html>
```

> Repare que o item da lista usa `textContent`, e não `innerHTML`. Assim, se alguém escrever HTML
> numa dúvida, ele aparece como **texto** e não é executado no seu site. 🛡️

### Java (`HttpClient`, sem bibliotecas extras)

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ExemploApiDuvidas {

    static final String API = "https://api-duvidas.onrender.com";
    static final String CHAVE = "SUA_CHAVE_ALUNO";

    public static void main(String[] args) throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();

        // POST: registrar uma dúvida
        String json = "{\"mensagem\": \"Como usar o HttpClient do Java?\"}";
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(API + "/duvidas"))
                .header("Content-Type", "application/json")
                .header("X-API-Key", CHAVE)
                .timeout(Duration.ofSeconds(90)) // a API pode estar acordando 😴
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resposta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
        System.out.println(resposta.statusCode() + " → " + resposta.body());
    }
}
```

> Montar JSON "na mão" como acima quebra se a mensagem tiver aspas. Em projetos maiores, use uma
> biblioteca como o **Jackson** para converter objetos Java em JSON.

### Terminal (`curl`)

```bash
# Listar
curl -H "X-API-Key: SUA_CHAVE_ALUNO" https://api-duvidas.onrender.com/duvidas

# Criar
curl -X POST https://api-duvidas.onrender.com/duvidas \
  -H "X-API-Key: SUA_CHAVE_ALUNO" \
  -H "Content-Type: application/json" \
  -d '{"mensagem": "Como usar o curl?"}'
```

---

## 🌐 CORS: chamando pelo navegador

O navegador só deixa um site chamar a API se ela autorizar aquele endereço. Isso se chama **CORS**.
Hoje estão liberados:

| Endereço do seu site | Liberado? |
|---|:---:|
| `http://localhost:` qualquer porta (Live Server, Vite, React…) | ✅ |
| `http://127.0.0.1:` qualquer porta | ✅ |
| `https://<seu-usuario>.github.io` (GitHub Pages) | ✅ |
| `https://<seu-projeto>.vercel.app` (Vercel) | ✅ |
| outro endereço | 🚫 peça para incluir |

**O CORS só vale para navegadores.** Insomnia, Java, Python e apps de celular não passam por ele.

**CORS não é a segurança da API.** Ele só diz **de quais sites** o navegador pode chamar.
Quem decide se o pedido é aceito continua sendo a **chave**.

---

## 🛟 Deu ruim? Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| A primeira chamada demora ~1 minuto | a API estava **acordando** 😴 (raro: o despertador evita isso) | aguarde. Se acontecer sempre, confira o <https://api-duvidas.onrender.com/saude> e avise quem mantém a API |
| **401** mesmo com a chave | chave com **aspas**, com espaço, incompleta, ou no header errado | o header é `X-API-Key`, e o valor vai **sem aspas** |
| **403** | você usou a chave ALUNO para editar ou apagar | só a chave PROFESSOR faz isso |
| **400** | a mensagem está vazia ou passou de 2000 caracteres | leia a lista `erros` da resposta |
| `Failed to fetch` ou *blocked by CORS policy* no navegador | o endereço do seu site não está liberado, **ou** a API ainda estava acordando | confira a [tabela do CORS](#-cors-chamando-pelo-navegador). Se o seu endereço estiver lá, tente de novo em 1 minuto |
| **500** | erro **dentro da API** | não é culpa sua: avise quem mantém a API |

---

## 🛠️ Para quem mantém a API

### Tecnologias

- **Java 25** + **Spring Boot 4** (Web MVC, Data JPA, Validation)
- **TiDB Cloud Starter** (compatível com MySQL, plano gratuito): volta sozinho quando fica parado, sem precisar religar
- Hospedagem no **Render**, com **Docker**
- Documentação com **springdoc-openapi** (OpenAPI 3 + Swagger UI). As anotações `@Tag`, `@Operation`,
  `@ApiResponse` e `@Schema` no código descrevem os endpoints e os campos.

### Como o código está organizado

| Arquivo | Papel |
|---|---|
| `Duvida.java` | a entidade (vira a tabela `duvida`) e as regras de validação |
| `DuvidaRepository.java` | o acesso ao banco (Spring Data JPA) |
| `DuvidaController.java` | os endpoints `/duvidas`, `/alo` e `/saude` |
| `PorteiroFilter.java` | 🛂 confere a chave `X-API-Key` e as permissões (401/403) |
| `TratadorDeErros.java` | transforma erros de validação em respostas 400 claras |
| `CorsConfig.java` | 🌐 libera os sites da variável `CORS_ORIGENS` |
| `DocumentacaoConfig.java` | 📖 nome, descrição e a chave `X-API-Key` no Swagger |
| `Dockerfile` | 📦 a receita do container usado no Render |

### Variáveis de ambiente

| Variável | Para quê | Padrão (se não definir) |
|---|---|---|
| `DB_URL` | endereço JDBC do banco. No TiDB: `jdbc:mysql://HOST:4000/duvidasdb?sslMode=VERIFY_IDENTITY` | `jdbc:mysql://localhost:3306/duvidasdb` |
| `DB_USER` | usuário do banco. No TiDB ele tem um prefixo: `xxxx.root` | `root` |
| `DB_PASSWORD` | senha do banco | **obrigatória** |
| `DB_POOL_SIZE` | máximo de conexões com o banco | `5` |
| `API_KEY_ALUNO` | chave de quem só lista e cria | **obrigatória** |
| `API_KEY_PROFESSOR` | chave com acesso total | **obrigatória** |
| `CORS_ORIGENS` | sites liberados no navegador (aceita `*`) | `http://localhost:*,http://127.0.0.1:*` |
| `PORT` | porta do servidor (o Render define sozinho) | `8080` |

As variáveis **obrigatórias** não têm valor padrão **de propósito**: se faltar alguma, a API **não liga**.
É melhor ela ficar fora do ar do que subir com uma senha ou chave adivinhável. 🛡️

### Rodando no seu computador

1. Tenha **Java 25** (ou mais novo) e um banco `duvidasdb`: um **MySQL** local ou o **TiDB**.
   No TiDB, o seu IP precisa estar liberado (veja [Segurança do banco](#-segurança-do-banco)).
2. Copie o `.env.example` para `.env` e preencha. O `.env` **não vai para o GitHub**.
   Para gerar chaves aleatórias: `openssl rand -hex 24`.
3. Rode:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Teste: <http://localhost:8080/saude>

### Publicação

A cada `git push` na branch `main`, o **Render** monta o container a partir do `Dockerfile` e publica
a nova versão sozinho. As senhas e chaves ficam **só** no painel do Render (Environment), nunca no código.

O **Health Check Path** do Render é `/saude`: se o banco cair, o Render percebe.

### ⏰ O despertador

Um job no **cron-job.org** chama `GET /saude` **a cada 10 minutos**. Isso:

- impede o Render de desligar a API (ele desliga depois de 15 minutos sem uso);
- mantém o banco em uso;
- avisa por e-mail se a resposta não for 200.

### 🔐 Segurança do banco

O TiDB só aceita conexões dos endereços cadastrados em **Settings → Networking → Authorized Networks**:

| Regra | Para quê |
|---|---|
| as faixas de saída do Render (`render1`, `render2`) | a API no ar |
| o IP de quem mantém a API | testes locais |

A regra `Allow_all_public_connections` (liberar a internet inteira) **foi apagada de propósito**.

- **Seu IP mudou** (outra rede, outro dia)? A API local não vai conectar. Clique em **Add Current IP** no TiDB.
- **O Render mudou de região?** Os IPs de saída mudam junto. Copie os novos em **Render → Connect → Outbound**
  e cadastre no TiDB. Um `/24` vira o intervalo `A.B.C.0` a `A.B.C.255`.

---

Feito com ☕ pela turma de Desenvolvimento Java do **SENAI Taguatinga**.
