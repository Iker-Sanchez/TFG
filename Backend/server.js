const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");

dotenv.config();

const app = express();

const PORT = process.env.PORT || 3000;
const API_KEY = process.env.API_FOOTBALL_KEY;
const SEASON = process.env.SEASON || 2024;
const LEAGUE_LALIGA = process.env.LEAGUE_LALIGA || 140;

app.use(cors());
app.use(express.json());

function comprobarConfiguracion() {
    if (!API_KEY) {
        console.error("Falta API_FOOTBALL_KEY en el archivo .env");
        process.exit(1);
    }
}

async function llamarApiFootball(endpoint) {
    const url = `https://v3.football.api-sports.io${endpoint}`;

    const response = await fetch(url, {
        method: "GET",
        headers: {
            "x-apisports-key": API_KEY
        }
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(`Error ${response.status}: ${JSON.stringify(data)}`);
    }

    return data;
}

function transformarClasificacion(data) {
    if (
        !data ||
        !data.response ||
        data.response.length === 0 ||
        !data.response[0].league ||
        !data.response[0].league.standings ||
        data.response[0].league.standings.length === 0
    ) {
        return [];
    }

    const tabla = data.response[0].league.standings[0];

    return tabla.map((equipo) => ({
        puesto: equipo.rank,
        nombre: equipo.team?.name || "",
        logo: equipo.team?.logo || "",
        puntos: equipo.points,
        pj: equipo.all?.played ?? 0,
        pg: equipo.all?.win ?? 0,
        pe: equipo.all?.draw ?? 0,
        pp: equipo.all?.lose ?? 0,
        gf: equipo.all?.goals?.for ?? 0,
        gc: equipo.all?.goals?.against ?? 0,
        dg: equipo.goalsDiff ?? 0,
        forma: equipo.form || ""
    }));
}

// Ruta de prueba
app.get("/", (req, res) => {
    res.json({
        mensaje: "Backend funcionando correctamente"
    });
});

// Ruta principal para Android
app.get("/clasificacion/laliga", async (req, res) => {
    try {
        const data = await llamarApiFootball(
            `/standings?league=${LEAGUE_LALIGA}&season=${SEASON}`
        );

        const clasificacion = transformarClasificacion(data);

        res.json(clasificacion);
    } catch (error) {
        console.error("Error en /clasificacion/laliga:", error.message);
        res.status(500).json({
            error: "No se pudo obtener la clasificación",
            detalle: error.message
        });
    }
});

// Ruta opcional para depurar
app.get("/debug/laliga", async (req, res) => {
    try {
        const data = await llamarApiFootball(
            `/standings?league=${LEAGUE_LALIGA}&season=${SEASON}`
        );

        res.json(data);
    } catch (error) {
        console.error("Error en /debug/laliga:", error.message);
        res.status(500).json({
            error: "No se pudo obtener el JSON original",
            detalle: error.message
        });
    }
});

comprobarConfiguracion();

app.listen(PORT, () => {
    console.log(`Servidor iniciado en http://localhost:${PORT}`);
    console.log(`Prueba la clasificación en: http://localhost:${PORT}/clasificacion/laliga`);
});