const express = require("express");
const cors    = require("cors");
const dotenv  = require("dotenv");

dotenv.config();

const app      = express();
const PORT     = process.env.PORT    || 3000;
const API_KEY  = process.env.API_FOOTBALL_KEY;
const SEASON   = process.env.SEASON  || 2024;
const LEAGUE_LALIGA = process.env.LEAGUE_LALIGA || 140;

app.use(cors());
app.use(express.json());

// ─────────────────────────────────────────────
//  Utilidades
// ─────────────────────────────────────────────

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
        headers: { "x-apisports-key": API_KEY }
    });
    const data = await response.json();
    if (!response.ok) throw new Error(`Error ${response.status}: ${JSON.stringify(data)}`);
    return data;
}

// ─────────────────────────────────────────────
//  Transformaciones
// ─────────────────────────────────────────────

function transformarClasificacion(data) {
    if (
        !data?.response?.length ||
        !data.response[0].league?.standings?.length
    ) return [];

    return data.response[0].league.standings[0].map(eq => ({
        puesto:  eq.rank,
        nombre:  eq.team?.name   ?? "",
        escudo:  eq.team?.logo   ?? "",
        puntos:  eq.points,
        pj:      eq.all?.played  ?? 0,
        pg:      eq.all?.win     ?? 0,
        pe:      eq.all?.draw    ?? 0,
        pp:      eq.all?.lose    ?? 0,
        gf:      eq.all?.goals?.for     ?? 0,
        gc:      eq.all?.goals?.against ?? 0,
        dg:      eq.goalsDiff    ?? 0,
        forma:   eq.form         ?? ""
    }));
}

/**
 * Convierte un fixture de la API en el objeto Partido simplificado.
 * La fecha viene en UTC; la formateamos a dd/MM/yyyy y HH:MM en hora local del servidor.
 */
function transformarPartidos(data) {
    if (!data?.response?.length) return [];

    return data.response.map(item => {
        const f = item.fixture;
        const teams  = item.teams;
        const goals  = item.goals;
        const status = f.status?.short ?? "NS";

        // Parsear fecha UTC → dd/MM/yyyy  HH:MM
        const dt = new Date(f.date);
        const fecha = dt.toLocaleDateString("es-ES", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
        const hora = dt.toLocaleTimeString("es-ES", {
            hour: "2-digit", minute: "2-digit", hour12: false
        });

        return {
            jornada:           item.league?.round ?? "",
            fecha,
            hora,
            estado:            status,
            localNombre:       teams.home?.name  ?? "",
            localEscudo:       teams.home?.logo  ?? "",
            visitanteNombre:   teams.away?.name  ?? "",
            visitanteEscudo:   teams.away?.logo  ?? "",
            // null si el partido no ha empezado
            golesLocal:        status === "NS" ? null : (goals.home ?? null),
            golesVisitante:    status === "NS" ? null : (goals.away ?? null)
        };
    });
}

// ─────────────────────────────────────────────
//  Rutas
// ─────────────────────────────────────────────

// Prueba
app.get("/", (req, res) => {
    res.json({ mensaje: "Backend TFG funcionando" });
});

// Clasificación LaLiga (ruta fija para compatibilidad)
app.get("/clasificacion/laliga", async (req, res) => {
    try {
        const data = await llamarApiFootball(
            `/standings?league=${LEAGUE_LALIGA}&season=${SEASON}`
        );
        res.json(transformarClasificacion(data));
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: "Error al obtener clasificación LaLiga", detalle: err.message });
    }
});

// Clasificación genérica por ID de liga
app.get("/clasificacion/:leagueId", async (req, res) => {
    const { leagueId } = req.params;
    try {
        const data = await llamarApiFootball(
            `/standings?league=${leagueId}&season=${SEASON}`
        );
        res.json(transformarClasificacion(data));
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: "Error al obtener clasificación", detalle: err.message });
    }
});

// ── NUEVA: Jornada actual de una liga ──────────────────────────
// Devuelve: { "jornada": 12 }
app.get("/jornada-actual/:leagueId", async (req, res) => {
    const { leagueId } = req.params;
    try {
        // La API de football devuelve las rondas actuales con ?current=true
        const data = await llamarApiFootball(
            `/fixtures/rounds?league=${leagueId}&season=${SEASON}&current=true`
        );

        if (!data?.response?.length) {
            return res.json({ jornada: 1 });
        }

        // El formato suele ser "Regular Season - 12"
        const roundStr = data.response[0];               // e.g. "Regular Season - 12"
        const match    = roundStr.match(/(\d+)$/);        // extrae el último número
        const jornada  = match ? parseInt(match[1]) : 1;

        res.json({ jornada });
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: "Error al obtener jornada actual", detalle: err.message });
    }
});

// ── NUEVA: Partidos de una jornada ────────────────────────────
// Devuelve: array de Partido
app.get("/partidos/:leagueId/:jornada", async (req, res) => {
    const { leagueId, jornada } = req.params;
    
    try {
        // 1. Primero consultamos qué rondas existen para esta liga
        const roundsData = await llamarApiFootball(`/fixtures/rounds?league=${leagueId}&season=${SEASON}`);
        
        // 2. Buscamos la ronda que contenga el número de la jornada al final
        // Esto soluciona si es "Regular Season - 12", "Group Stage - 12", etc.
        const roundStr = roundsData.response.find(r => r.endsWith("- " + jornada)) || roundsData.response[jornada - 1];

        const data = await llamarApiFootball(
            `/fixtures?league=${leagueId}&season=${SEASON}&round=${encodeURIComponent(roundStr)}`
        );
        res.json(transformarPartidos(data));
    } catch (err) {
        console.error(err.message);
        res.status(500).json({ error: "Error al obtener partidos", detalle: err.message });
    }
});

// Debug: JSON crudo de la API
// Usamos una Expresión Regular para capturar todo lo que venga tras /debug/
app.get(/\/debug\/(.*)/, async (req, res) => {
    try {
        // En Express con Regex, el grupo capturado (.*) está en req.params[0]
        const endpoint = req.params[0];

        if (!endpoint) {
            return res.status(400).json({ error: "Falta el endpoint. Ejemplo: /debug/fixtures" });
        }

        const data = await llamarApiFootball("/" + endpoint);
        res.json(data);
    } catch (err) {
        console.error("Error en Debug:", err.message);
        res.status(500).json({ error: err.message });
    }
});

// ─────────────────────────────────────────────
//  Arranque
// ─────────────────────────────────────────────

comprobarConfiguracion();

app.listen(PORT, () => {
    console.log(`\nServidor iniciado en http://localhost:${PORT}`);
    console.log(`  Clasificación:   GET /clasificacion/140`);
    console.log(`  Jornada actual:  GET /jornada-actual/140`);
    console.log(`  Partidos:        GET /partidos/140/12\n`);
});