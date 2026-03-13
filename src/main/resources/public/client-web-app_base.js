require("dotenv").config({ path: "base.env" });
const express = require("express");
const cookieParser = require("cookie-parser");
const axios = require("axios");
const FormData = require("form-data"); // more info at:
const jwt = require("jsonwebtoken");
const PORT = 3001;

// system variables where Client credentials are stored
const CLIENT_ID = process.env.CLIENT_ID;
const CLIENT_SECRET = process.env.CLIENT_SECRET;

// callback URL configured during Client registration in OIDC provider
const CALLBACK = "callback";

const app = express();
const secretKey = process.env.SECRET_KEY_COOKIE;

console.log("Secret key:", secretKey);
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser(secretKey));

/* =========================
   MIDDLEWARES CUSTOM
========================= */
function checkAllowedUser(req, resp, next) {
  const userEmail = req.signedCookies.user_email;

  if (!userEmail) {
    return resp.status(401).send("Não autenticado");
  }

  // Ler variável do .env
  const allowedUsersEnv = process.env.ALLOWED_USERS;

  if (!allowedUsersEnv) {
    console.error("ALLOWED_USERS não definida no .env");
    return resp.status(500).send("Configuração inválida do servidor");
  }

  // Converter string em array
  const allowedUsers = allowedUsersEnv
    .split(",")
    .map(email => email.trim());

  if (!allowedUsers.includes(userEmail)) {
    return resp.status(403).send("Sem permissão");
  }

  next();
}

/*
 * AUTENTICAÇÃO - Tipo Google.
*/
app.get("/", (req, resp) => {
  resp.send(`
  <html>
  <head>
    <title>Google Tasks Manager</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <style>
      body {
        margin: 0;
        height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        font-family: 'Segoe UI', Roboto, Arial, sans-serif;
        background: linear-gradient(135deg, #4285f4, #34a853);
      }

      .card {
        background: white;
        padding: 60px;
        border-radius: 16px;
        box-shadow: 0 15px 40px rgba(0,0,0,0.2);
        text-align: center;
        animation: fadeIn 0.6s ease;
        width: 360px;
      }

      h1 {
        margin-bottom: 40px;
        font-weight: 600;
        color: #202124;
      }

      .google-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        padding: 14px;
        border-radius: 8px;
        border: 1px solid #dadce0;
        background: white;
        cursor: pointer;
        font-size: 15px;
        font-weight: 500;
        text-decoration: none;
        color: #3c4043;
        transition: all 0.2s ease;
      }

      .google-btn:hover {
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        transform: translateY(-2px);
      }

      .google-icon {
        width: 18px;
        height: 18px;
      }

      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(20px); }
        to { opacity: 1; transform: translateY(0); }
      }
    </style>
  </head>
  <body>

    <div class="card">
      <h1>Google Tasks Manager</h1>

      <a href="/login" class="google-btn">
        <svg class="google-icon" viewBox="0 0 48 48">
          <path fill="#EA4335" d="M24 9.5c3.1 0 5.9 1.1 8.1 3.3l6-6C34.4 3.2 29.6 1 24 1 14.6 1 6.5 6.6 2.7 14.8l7.2 5.6C11.5 14 17.2 9.5 24 9.5z"/>
          <path fill="#4285F4" d="M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v8.6h12.7c-.6 3-2.4 5.5-5.1 7.1l7.8 6c4.6-4.2 7.1-10.4 7.1-17.2z"/>
          <path fill="#FBBC05" d="M9.9 28.4c-.7-2-1.1-4.1-1.1-6.4s.4-4.4 1.1-6.4l-7.2-5.6C1 14.5 0 19.1 0 24s1 9.5 2.7 13.4l7.2-5z"/>
          <path fill="#34A853" d="M24 47c5.6 0 10.4-1.9 13.9-5.2l-7.8-6c-2.2 1.5-5 2.4-8.1 2.4-6.8 0-12.5-4.5-14.1-10.7l-7.2 5C6.5 41.4 14.6 47 24 47z"/>
        </svg>

        Sign in with Google
      </a>
    </div>

  </body>
  </html>
  `);
});

// More information at:
// https://developers.google.com/identity/protocols/OpenIDConnect
const crypto = require("crypto");

app.get("/login", (req, resp) => {
  const state = crypto.randomBytes(16).toString("hex");

  // Guardar state num cookie assinado
  resp.cookie("oauth_state", state, {
    httpOnly: true,
    signed: true
  });

  resp.redirect(
    302,
    "https://accounts.google.com/o/oauth2/v2/auth?" +
    "client_id=" + CLIENT_ID + "&" +
    "scope=openid%20email%20https://www.googleapis.com/auth/tasks&" +
    "state=" + state + "&" +
    "response_type=code&" +
    "redirect_uri=http://localhost:" +
    PORT +
    "/" +
    CALLBACK
  );
});

app.get("/" + CALLBACK, (req, resp) => {
  console.log("making request to token endpoint");

  const returnedState = req.query.state;
  const savedState = req.signedCookies.oauth_state;

  if (!savedState || returnedState !== savedState) {
    return resp.status(400).send("Invalid state parameter");
  }

  // content-type: application/x-www-form-urlencoded (URL-Encoded Forms)
  const form = new FormData();
  form.append("code", req.query.code);
  form.append("client_id", CLIENT_ID);
  form.append("client_secret", CLIENT_SECRET);
  form.append("redirect_uri", "http://localhost:3001/" + CALLBACK);
  form.append("grant_type", "authorization_code");

  axios
    .post(
      // token endpoint
      "https://www.googleapis.com/oauth2/v3/token",
      // body parameters in form url encoded
      form,
      { headers: form.getHeaders() },
    )
    .then(function (response) {
      // AXIOS assumes by default that response type is JSON: https://github.com/axios/axios#request-config
      // Property response.data should have the JSON response according to schema described here: https://openid.net/specs/openid-connect-core-1_0.html#TokenResponse

      console.log(response.data);
      // decode id_token from base64 encoding
      // note: method decode does not verify signature
      var jwt_payload = jwt.decode(response.data.id_token);
      console.log(jwt_payload);

      // a simple cookie example
      const cookieOptions = {
        httpOnly: true,
        signed: true
      };

      resp.cookie("access_token", response.data.access_token, cookieOptions);
      resp.cookie("user_email", jwt_payload.email, cookieOptions);
      resp.redirect("/dashboard");

    })
    .catch(function (error) {
      console.log(error);
      resp.send();
    });
});

/*
 * TODO: 4-g)
*/
/*
 * DASHBOARD:
 * - Buscar Listas de Tarefas;
 * - Selecionar lista específica;
 * - Ver tarefas da Lista selecionada;
 * - Botão Logout.
*/
app.get("/dashboard", async (req, resp) => {
  const accessToken = req.signedCookies.access_token;

  console.log("Cookies normais:", req.cookies);
  console.log("Cookies assinados:", req.signedCookies);

  if (!accessToken) {
    return resp.redirect("/");
  }

  try {

    const response = await axios.get(
      "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
      {
        headers: {
          Authorization: "Bearer " + accessToken
        }
      }
    );

    const lists = response.data.items || [];

    resp.send(`
      <html>
      <head>
        <title>Dashboard</title>
        <style>
          body {
            margin: 0;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #4285f4, #34a853);
          }

          .card {
            background: white;
            padding: 50px;
            border-radius: 16px;
            box-shadow: 0 15px 40px rgba(0,0,0,0.2);
            width: 420px;
            text-align: center;
          }

          h2 {
            margin-bottom: 30px;
            color: #202124;
            font-weight: 600;
          }

          .form-group {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
          }

          select {
            flex: 1;
            padding: 10px;
            border-radius: 6px;
            border: 1px solid #ccc;
            font-size: 14px;
          }

          button {
            padding: 10px 16px;
            border-radius: 6px;
            border: none;
            font-weight: 500;
            cursor: pointer;
            transition: 0.2s;
          }

          .primary-btn {
            background: #4285f4;
            color: white;
          }

          .primary-btn:hover {
            background: #2f6fdb;
          }

          .secondary-btn {
            display: block;
            margin-top: 15px;
            background: #34a853;
            color: white;
            text-decoration: none;
            padding: 12px;
            border-radius: 6px;
            transition: 0.2s;
          }

          .secondary-btn:hover {
            background: #2c8e46;
          }

          .logout {
            margin-top: 25px;
            display: inline-block;
            font-size: 13px;
            color: #666;
            text-decoration: none;
          }

          .logout:hover {
            color: #000;
          }
        </style>
      </head>
      <body>

        <div class="card">
          <h2>Escolher Lista de Tarefas</h2>

          <form action="/tasks" method="GET">
            <div class="form-group">
              <select name="listId" required>
                <option value="">Selecione uma Lista</option>
                ${lists.map(list =>
      `<option value="${list.id}">${list.title}</option>`
    ).join("")}
              </select>

              <button type="submit" class="primary-btn">
                Ver
              </button>
            </div>
          </form>

          <a href="/tasklist" class="secondary-btn">
            Ver Todas as Listas
          </a>

          <a href="/logout" class="logout">
            Sair
          </a>
        </div>

      </body>
      </html>
    `);

  } catch (error) {
    console.log(error.response?.data || error);
    resp.status(500).send("Erro ao carregar o dashboard");
  }
});

/*
 * LISTA TODAS AS LISTAS DE TAREFAS
 * - Possibilita clicar numa Lista para ver as respetivas tarefas.
*/
app.get("/tasklist", async (req, resp) => {
  const accessToken = req.signedCookies.access_token;

  if (!accessToken) {
    return resp.redirect("/");
  }

  try {

    const response = await axios.get(
      "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
      {
        headers: {
          Authorization: "Bearer " + accessToken
        }
      }
    );

    const lists = response.data.items || [];

    resp.send(`
      <html>
      <head>
        <title>Listas de Tarefas</title>
        <style>
          body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #4285f4, #34a853);
          }

          .card {
            background: white;
            padding: 50px;
            border-radius: 16px;
            box-shadow: 0 15px 40px rgba(0,0,0,0.2);
            width: 500px;
          }

          h1 {
            margin-bottom: 30px;
            color: #202124;
            font-weight: 600;
          }

          .list-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 20px;
            margin-bottom: 15px;
            border-radius: 10px;
            background: #f4f6f9;
            cursor: pointer;
            transition: 0.2s ease;
            text-decoration: none;
            color: #333;
            font-weight: 500;
          }

          .list-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            background: #eef2f7;
          }

          .list-left {
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .list-icon {
            width: 18px;
            height: 18px;
            fill: #4285f4;
          }

          .arrow {
            font-size: 18px;
            color: #888;
          }

          .back-btn {
            display: inline-block;
            margin-top: 25px;
            padding: 12px 18px;
            border-radius: 8px;
            background: #4285f4;
            color: white;
            text-decoration: none;
            font-weight: 500;
            transition: 0.2s;
          }

          .back-btn:hover {
            background: #2f6fdb;
          }
        </style>
      </head>
      <body>

        <div class="card">
          <h1>Listas de Tarefas</h1>

          ${lists.map(list => `
            <a class="list-card" href="/tasks?listId=${list.id}">
              <div class="list-left">
                <svg class="list-icon" viewBox="0 0 24 24">
                  <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 
                           4h14v-2H7v2zm0 4h14v-2H7v2zm0-10v2h14V7H7z"/>
                </svg>
                ${list.title}
              </div>
              <div class="arrow">›</div>
            </a>
          `).join("")}

          <a href="/dashboard" class="back-btn">
            Voltar ao Dashboard
          </a>

        </div>

      </body>
      </html>
    `);

  } catch (error) {
    console.log(error.response?.data || error);
    resp.status(500).send("Erro ao obter listas");
  }
});

/*
 * LISTA AS TAREFAS DE UMA LISTA ESPECÍFICA:
 * - Marcar como concluída;
 * - Eliminar.
 */
app.get("/tasks", async (req, resp) => {
  const accessToken = req.signedCookies.access_token;
  const userEmail = req.signedCookies.user_email;

  if (!accessToken) {
    return resp.redirect("/");
  }

  const listId = req.query.listId;

  if (!listId) {
    return resp.redirect("/tasklist");
  }

  try {

    const response = await axios.get(
      `https://tasks.googleapis.com/tasks/v1/lists/${listId}/tasks`,
      {
        headers: {
          Authorization: "Bearer " + accessToken
        }
      }
    );

    const tasks = response.data.items || [];

    const allowedUsers = process.env.ALLOWED_USERS
      .split(",")
      .map(e => e.trim());

    const isAllowed = allowedUsers.includes(userEmail);

    resp.send(`
      <html>
        <head>
          <title>Tarefas</title>
          <style>
            body {
              margin: 0;
              min-height: 100vh;
              display: flex;
              justify-content: center;
              align-items: center;
              font-family: Arial, sans-serif;
              background: linear-gradient(135deg, #4285f4, #34a853);
            }
  
            .card {
              background: white;
              padding: 50px;
              border-radius: 16px;
              box-shadow: 0 15px 40px rgba(0,0,0,0.2);
              width: 550px;
            }
  
            h1 {
              margin-bottom: 30px;
              font-weight: 600;
              color: #202124;
            }
  
            .task-item {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 16px 20px;
              margin-bottom: 16px;
              border-radius: 12px;
              background: #f8f9fa;
              transition: 0.2s ease;
            }
            
            .task-item:hover {
              background: #eef2f7;
              transform: translateY(-2px);
            }
            
            .task-left {
              display: flex;
              align-items: center;
              gap: 14px;
              font-size: 15px;
              line-height: 1;
            }
            
            .task-left svg {
              flex-shrink: 0;
            }
            
            .completed {
              text-decoration: line-through;
              opacity: 0.6;
            }
            
            .task-actions {
              display: flex;
              align-items: center;
              gap: 10px;
            }
            
            .task-actions form {
              margin: 0;
            }
            
            button {
              border: none;
              padding: 8px 14px;
              border-radius: 8px;
              cursor: pointer;
              font-size: 13px;
              font-weight: 500;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            
            .complete-btn {
              background: #34a853;
              color: white;
            }
            
            .complete-btn:hover {
              background: #2c8e46;
            }
            
            .delete-btn {
              background: #ea4335;
              color: white;
            }
            
            .delete-btn:hover {
              background: #d93025;
            }
  
  
            .back-btn {
              display: inline-block;
              margin-top: 25px;
              padding: 12px 18px;
              border-radius: 8px;
              background: #4285f4;
              color: white;
              text-decoration: none;
              font-weight: 500;
              transition: 0.2s;
            }
  
            .back-btn:hover {
              background: #2f6fdb;
            }
          </style>
        </head>
        <body>
  
          <div class="card">
            <h1>Tarefas</h1>
  
            ${tasks.map(task => `
              <div class="task-item">
  
                <div class="task-left ${task.status === "completed" ? "completed" : ""}">
                  ${
                    task.status === "completed"
                      ? '<svg width="16" height="16" fill="#34a853"><path d="M6 10l-2-2-1 1 3 3 6-6-1-1z"/></svg>'
                      : '<svg width="16" height="16" stroke="#ea4335" stroke-width="2" fill="none"><line x1="4" y1="4" x2="12" y2="12"/><line x1="12" y1="4" x2="4" y2="12"/></svg>'
                  }
  
                  ${task.title}
                </div>
  
                ${isAllowed && task.status !== "completed" ? `
                  <div class="task-actions">
                    <form action="/complete" method="POST">
                      <input type="hidden" name="listId" value="${listId}">
                      <input type="hidden" name="taskId" value="${task.id}">
                      <button class="complete-btn">Marcar</button>
                    </form>
  
                    <form action="/delete" method="POST">
                      <input type="hidden" name="listId" value="${listId}">
                      <input type="hidden" name="taskId" value="${task.id}">
                      <button class="delete-btn">Eliminar</button>
                    </form>
                  </div>
                ` : ""}
              </div>
            `).join("")}
  
            <a href="/tasklist" class="back-btn">
              Voltar às Listas
            </a>
  
          </div>
  
        </body>
      </html>
    `);

  } catch (error) {
    console.log(error.response?.data || error);
    resp.status(500).send("Erro ao obter tarefas");
  }
});

/*
 * Marcar tarefa como concluída.
*/
app.post("/complete", checkAllowedUser, async (req, resp) => {
  const accessToken = req.signedCookies.access_token;
  const { listId, taskId } = req.body;

  try {

    await axios.patch(
      `https://tasks.googleapis.com/tasks/v1/lists/${listId}/tasks/${taskId}`,
      { status: "completed" },
      {
        headers: {
          Authorization: "Bearer " + accessToken,
          "Content-Type": "application/json"
        }
      }
    );

    resp.redirect(`/tasks?listId=${listId}`);

  } catch (error) {
    console.log(error.response?.data || error);
    resp.status(500).send("Erro ao marcar tarefa como concluída");
  }
});

/*
 * Eliminar tarefa.
*/
app.post("/delete", checkAllowedUser, async (req, resp) => {
  const accessToken = req.signedCookies.access_token;
  const { listId, taskId } = req.body;

  try {

    await axios.delete(
      `https://tasks.googleapis.com/tasks/v1/lists/${listId}/tasks/${taskId}`,
      {
        headers: {
          Authorization: "Bearer " + accessToken
        }
      }
    );

    resp.redirect(`/tasks?listId=${listId}`);

  } catch (error) {
    console.log(error.response?.data || error);
    resp.status(500).send("Erro ao eliminar tarefa");
  }
});

/*
 * SAIR
*/
app.get("/logout", (req, resp) => {

  resp.clearCookie("access_token");
  resp.clearCookie("oauth_state");
  resp.clearCookie("user_email");

  resp.send(`
    <html>
      <head>
        <title>Logout</title>
        <style>
          body {
            margin: 0;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #4285f4, #34a853);
          }
  
          .card {
            background: white;
            padding: 50px;
            border-radius: 16px;
            box-shadow: 0 15px 40px rgba(0,0,0,0.2);
            width: 420px;
            text-align: center;
          }
  
          .icon {
            margin-bottom: 20px;
          }
  
          h2 {
            margin-bottom: 20px;
            color: #202124;
            font-weight: 600;
          }
  
          p {
            color: #5f6368;
            margin-bottom: 30px;
          }
  
          .login-btn {
            display: inline-block;
            padding: 12px 20px;
            border-radius: 8px;
            background: #4285f4;
            color: white;
            text-decoration: none;
            font-weight: 500;
            transition: 0.2s;
          }
  
          .login-btn:hover {
            background: #2f6fdb;
          }
  
        </style>
      </head>
      <body>
        <div class="card">
  
          <div class="icon">
            <svg width="40" height="40" fill="#34a853" viewBox="0 0 24 24">
              <path d="M9 16.2l-3.5-3.5-1.4 1.4L9 19 20 8l-1.4-1.4z"/>
            </svg>
          </div>
  
          <h2>Logout efetuado</h2>
          <p>A sua sessão foi terminada com sucesso.</p>
  
          <a href="/" class="login-btn">
            Login novamente
          </a>
  
        </div>
      </body>
    </html>
  `);
});

app.listen(PORT, (err) => {
  if (err) {
    return console.log("something bad happened", err);
  }
  console.log(`server is listening on ${PORT}`);
});