const express = require("express");
const cors = require("cors");

const app = express();
const PORT = 3000;

// Pon aquí tu API key de API-Football / API-Sports
const apiKey = "31641f930e60d56ebc3412d04c4e4bff";

app.use(cors());
app.use(express.static("public"));

// Config común para la API
const API_HOST = "v3.football.api-sports.io";

async function llamarApi(endpoint) {
    const response = await fetch(`https://${API_HOST}${endpoint}`, {
        method: "GET",
        headers: {
            "x-apisports-key": apiKey,
            "x-rapidapi-host": API_HOST
        }
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(`Error ${response.status}: ${JSON.stringify(data)}`);
    }

    return data;
}

// 1. Equipos de La Liga
app.get("/equipos", async (req, res) => {
    try {
        const data = await llamarApi("/teams?league=140&season=2023");
        res.json(data);
    } catch (error) {
        console.error("ERROR /equipos:", error.message);
        res.status(500).json({ error: error.message });
    }
});

// 2. Plantilla de un equipo
app.get("/plantilla/:teamId", async (req, res) => {
    try {
        const { teamId } = req.params;
        const data = await llamarApi(`/players/squads?team=${teamId}`);
        res.json(data);
    } catch (error) {
        console.error("ERROR /plantilla:", error.message);
        res.status(500).json({ error: error.message });
    }
});

// 3. Estadísticas de jugadores de un equipo en una temporada
app.get("/estadisticas-jugadores/:teamId", async (req, res) => {
    try {
        const { teamId } = req.params;
        const season = 2024; // prueba también con 2025 si quieres temporada actual

        let pagina = 1;
        let todosLosJugadores = [];
        let totalPaginas = 1;

        do {
            const data = await llamarApi(`/players?team=${teamId}&season=${season}&page=${pagina}`);

            if (data.response) {
                todosLosJugadores = todosLosJugadores.concat(data.response);
            }

            totalPaginas = data.paging?.total || 1;
            pagina++;
        } while (pagina <= totalPaginas);

        res.json({
            response: todosLosJugadores
        });

    } catch (error) {
        console.error("ERROR /estadisticas-jugadores:", error.message);
        res.status(500).json({ error: error.message });
    }
});

app.get("/jugador/:playerId", async (req, res) => {
    try {
        const { playerId } = req.params;
        const season = 2024;

        const data = await llamarApi(`/players?id=${playerId}&season=${season}`);
        res.json(data);
    } catch (error) {
        console.error("ERROR /jugador:", error.message);
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Servidor iniciado en http://localhost:${PORT}`);
});