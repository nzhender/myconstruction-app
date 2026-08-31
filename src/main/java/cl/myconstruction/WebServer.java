package cl.myconstruction;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void iniciar() throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8085), 0);

        server.createContext("/", exchange -> {

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                mostrarLogin(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/login", exchange -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                procesarLogin(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println(
                "MyConstruction disponible en http://localhost:8085");
    }

    private static void mostrarLogin(HttpExchange exchange)
            throws IOException {

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>MyConstruction</title>

                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f3f4f6;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                        }

                        .login {
                            background: white;
                            width: 360px;
                            padding: 35px;
                            border-radius: 12px;
                            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
                        }

                        h1 {
                            text-align: center;
                            color: #1f2937;
                        }

                        p {
                            text-align: center;
                            color: #6b7280;
                        }

                        input {
                            width: 100%;
                            padding: 12px;
                            margin: 8px 0;
                            box-sizing: border-box;
                            border: 1px solid #d1d5db;
                            border-radius: 6px;
                        }

                        button {
                            width: 100%;
                            padding: 12px;
                            margin-top: 12px;
                            background: #1f2937;
                            color: white;
                            border: none;
                            border-radius: 6px;
                            cursor: pointer;
                        }

                        button:hover {
                            background: #374151;
                        }
                    </style>

                </head>

                <body>

                    <div class="login">

                        <h1>MyConstruction</h1>

                        <p>Acceso de clientes</p>

                        <form method="POST" action="/login">

                            <input
                                type="email"
                                name="email"
                                placeholder="Correo electrónico"
                                required>

                            <input
                                type="password"
                                name="password"
                                placeholder="Contraseña"
                                required>

                            <button type="submit">
                                Iniciar sesión
                            </button>

                        </form>

                    </div>

                </body>
                </html>
                """;

        enviarRespuesta(exchange, html);
    }

    private static void procesarLogin(HttpExchange exchange)
            throws IOException {

        String datos = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        Map<String, String> parametros = parsearFormulario(datos);

        String email = parametros.get("email");
        String password = parametros.get("password");

        LoginService loginService = new LoginService();

        boolean correcto =
                loginService.validarLogin(email, password);

        String html;

        if (correcto) {

            html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>MyConstruction</title>
                    </head>

                    <body style="
                        font-family: Arial;
                        text-align:center;
                        margin-top:100px;
                        background:#f3f4f6;">

                        <h1>Bienvenido a MyConstruction</h1>

                        <h2>Login realizado correctamente</h2>

                        <p>
                            Bienvenido Administrador.
                        </p>

                        <a href="/">
                            Cerrar sesión
                        </a>

                    </body>
                    </html>
                    """;

        } else {

            html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>MyConstruction</title>
                    </head>

                    <body style="
                        font-family:Arial;
                        text-align:center;
                        margin-top:100px;">

                        <h1>MyConstruction</h1>

                        <h2 style="color:red;">
                            Credenciales incorrectas
                        </h2>

                        <a href="/">
                            Volver al login
                        </a>

                    </body>
                    </html>
                    """;
        }

        enviarRespuesta(exchange, html);
    }

    private static Map<String, String>
            parsearFormulario(String datos) {

        Map<String, String> parametros = new HashMap<>();

        for (String parametro : datos.split("&")) {

            String[] partes = parametro.split("=", 2);

            if (partes.length == 2) {

                String clave = URLDecoder.decode(
                        partes[0],
                        StandardCharsets.UTF_8);

                String valor = URLDecoder.decode(
                        partes[1],
                        StandardCharsets.UTF_8);

                parametros.put(clave, valor);
            }
        }

        return parametros;
    }

    private static void enviarRespuesta(
            HttpExchange exchange,
            String respuesta) throws IOException {

        byte[] bytes =
                respuesta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8");

        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream output =
                exchange.getResponseBody()) {

            output.write(bytes);
        }
    }
}