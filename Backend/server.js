const express = require("express");
const cors    = require("cors");
const dotenv  = require("dotenv");

dotenv.config();

const app     = express();
const PORT    = process.env.PORT   || 3000;
const API_KEY = process.env.API_FOOTBALL_KEY;
const SEASON  = parseInt(process.env.SEASON) || 2025;
const SEASONS = [SEASON];

app.use(cors());
app.use(express.json());

// ─────────────────────────────────────────────
//  Utilidades
// ─────────────────────────────────────────────

function comprobarConfiguracion() {
    if (!API_KEY) { console.error("❌ Falta API_FOOTBALL_KEY"); process.exit(1); }
    console.log(`📅 Temporada: ${SEASON}`);
}

async function llamarApiFootball(endpoint) {
    const url = `https://v3.football.api-sports.io${endpoint}`;
    console.log(`📡 ${url}`);
    const response = await fetch(url, { method: "GET", headers: { "x-apisports-key": API_KEY } });
    const data = await response.json();
    if (data.errors && Object.keys(data.errors).length > 0) {
        throw new Error("Error API: " + JSON.stringify(data.errors));
    }
    console.log(`✅ Items: ${data.results ?? "?"}`);
    return data;
}

async function llamarConTemporadaAutomatica(buildEndpoint) {
    let lastData = null;
    let lastSeason = SEASONS[SEASONS.length - 1];
    for (const season of SEASONS) {
        try {
            const data = await llamarApiFootball(buildEndpoint(season));
            lastData = data; lastSeason = season;
            if (data?.response?.length > 0) {
                console.log(`📅 Temporada con datos: ${season}`);
                return { data, season };
            }
        } catch (err) { console.log(`⚠️  Temporada ${season} error: ${err.message}`); }
    }
    return { data: lastData ?? { response: [] }, season: lastSeason };
}

// ─────────────────────────────────────────────
//  Transformaciones
// ─────────────────────────────────────────────

function transformarClasificacion(data) {
    if (!data?.response?.length || !data.response[0].league?.standings?.length) return [];
    return data.response[0].league.standings[0].map(eq => ({
        puesto: eq.rank, nombre: eq.team?.name ?? "", escudo: eq.team?.logo ?? "",
        puntos: eq.points, pj: eq.all?.played ?? 0, pg: eq.all?.win ?? 0,
        pe: eq.all?.draw ?? 0, pp: eq.all?.lose ?? 0,
        gf: eq.all?.goals?.for ?? 0, gc: eq.all?.goals?.against ?? 0,
        dg: eq.goalsDiff ?? 0, forma: eq.form ?? ""
    }));
}

function transformarPartidos(data) {
    if (!data?.response?.length) return [];
    return data.response.map(item => {
        const f = item.fixture, teams = item.teams, goals = item.goals;
        const status = f?.status?.short ?? "NS";
        const dt = new Date(f.date);
        const fecha = dt.toLocaleDateString("es-ES", { day: "2-digit", month: "2-digit", year: "numeric" });
        const hora  = dt.toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit", hour12: false });
        const tieneGoles = status !== "NS" && status !== "TBD";
        return {
            fixtureId: f?.id ?? 0,
            jornada: item.league?.round ?? "", fecha, hora, estado: status,
            localTeamId: teams?.home?.id ?? 0, localNombre: teams?.home?.name ?? "",
            localEscudo: teams?.home?.logo ?? "",
            visitanteTeamId: teams?.away?.id ?? 0, visitanteNombre: teams?.away?.name ?? "",
            visitanteEscudo: teams?.away?.logo ?? "",
            golesLocal: tieneGoles ? (goals?.home ?? null) : null,
            golesVisitante: tieneGoles ? (goals?.away ?? null) : null
        };
    });
}

// ─────────────────────────────────────────────
//  Rutas
// ─────────────────────────────────────────────

app.get("/", (req, res) => res.json({ mensaje: "Backend TFG ✅", season: SEASON }));

app.get("/clasificacion/laliga", async (req, res) => {
    try {
        const { data } = await llamarConTemporadaAutomatica(s => `/standings?league=140&season=${s}`);
        res.json(transformarClasificacion(data));
    } catch (err) { res.status(500).json({ error: err.message }); }
});

app.get("/clasificacion/:leagueId", async (req, res) => {
    const { leagueId } = req.params;
    try {
        const { data } = await llamarConTemporadaAutomatica(s => `/standings?league=${leagueId}&season=${s}`);
        res.json(transformarClasificacion(data));
    } catch (err) { res.status(500).json({ error: err.message }); }
});

app.get("/jornada-actual/:leagueId", async (req, res) => {
    const { leagueId } = req.params;
    try {
        for (const season of SEASONS) {
            const dataRounds = await llamarApiFootball(`/fixtures/rounds?league=${leagueId}&season=${season}&current=true`);
            if (dataRounds?.response?.length) {
                const m = dataRounds.response[0].match(/(\d+)$/);
                return res.json({ jornada: m ? parseInt(m[1]) : 1 });
            }
            const dataFT = await llamarApiFootball(`/fixtures?league=${leagueId}&season=${season}&status=FT&last=10`);
            if (dataFT?.response?.length) {
                const rounds = dataFT.response.map(i => i.league?.round ?? "")
                    .map(r => { const m = r.match(/(\d+)$/); return m ? parseInt(m[1]) : 0; }).filter(n => n > 0);
                if (rounds.length) return res.json({ jornada: Math.max(...rounds) });
            }
        }
        res.json({ jornada: 1 });
    } catch (err) { res.json({ jornada: 1 }); }
});

app.get("/partidos/:leagueId/:jornada", async (req, res) => {
    const { leagueId, jornada } = req.params;
    let roundStr;
    switch (parseInt(leagueId)) {
        case 2: roundStr = `League Stage - ${jornada}`; break;
        case 3: roundStr = `Group Stage - ${jornada}`; break;
        default: roundStr = `Regular Season - ${jornada}`;
    }
    try {
        const { data } = await llamarConTemporadaAutomatica(
            s => `/fixtures?league=${leagueId}&season=${s}&round=${encodeURIComponent(roundStr)}`
        );
        res.json(transformarPartidos(data));
    } catch (err) { res.json([]); }
});

app.get("/estadisticas/:leagueId/:teamId", async (req, res) => {
    const { leagueId, teamId } = req.params;
    try {
        const { data } = await llamarConTemporadaAutomatica(
            s => `/teams/statistics?league=${leagueId}&season=${s}&team=${teamId}`
        );
        const st = data?.response;
        if (!st) return res.json({});
        const pj = st.fixtures?.played?.total ?? 0, v = st.fixtures?.wins?.total ?? 0;
        const e  = st.fixtures?.draws?.total  ?? 0, d = st.fixtures?.loses?.total ?? 0;
        const gf = st.goals?.for?.total?.total ?? 0, gc = st.goals?.against?.total?.total ?? 0;
        let am = 0, ro = 0;
        if (st.cards?.yellow) Object.values(st.cards.yellow).forEach(x => { if (x?.total) am += x.total; });
        if (st.cards?.red)    Object.values(st.cards.red).forEach(x    => { if (x?.total) ro += x.total; });
        res.json({
            nombre: st.team?.name ?? "", escudo: st.team?.logo ?? "",
            partidosJugados: pj, victorias: v, empates: e, derrotas: d,
            golesFavor: gf, golesContra: gc, diferencia: gf - gc,
            victoriasLocal: st.fixtures?.wins?.home ?? 0, empatesLocal: st.fixtures?.draws?.home ?? 0,
            derrotasLocal: st.fixtures?.loses?.home ?? 0,
            victoriasVisitante: st.fixtures?.wins?.away ?? 0, empatesVisitante: st.fixtures?.draws?.away ?? 0,
            derrotasVisitante: st.fixtures?.loses?.away ?? 0,
            tarjetasAmarillas: am, tarjetasRojas: ro, formaReciente: st.form ?? "",
            mediaGolesFavor: pj > 0 ? Math.round((gf/pj)*10)/10 : 0,
            mediaGolesContra: pj > 0 ? Math.round((gc/pj)*10)/10 : 0,
            porcentajeVictorias: pj > 0 ? Math.round((v/pj)*100) : 0,
            penaltisAFavor: st.penalty?.scored?.total ?? 0,
            penaltisEnContra: st.penalty?.missed?.total ?? 0,
            maxRachaVictorias: st.biggest?.streak?.wins ?? 0,
            maxRachaSinPerder: Math.max(st.biggest?.streak?.wins ?? 0, st.biggest?.streak?.draws ?? 0)
        });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

// ── Predicción de un partido ───────────────────────────────────
app.get("/prediccion/:fixtureId", async (req, res) => {
    const { fixtureId } = req.params;
    try {
        const data = await llamarApiFootball(`/predictions?fixture=${fixtureId}`);
        const r = data?.response?.[0];
        if (!r) return res.json({});
        const pred = r.predictions, comp = r.comparison, teams = r.teams, h2h = r.h2h ?? [];
        const h2hResultados = h2h.slice(0, 5).map(m => {
            const hN = m.teams?.home?.name ?? "?", aN = m.teams?.away?.name ?? "?";
            const gH = m.goals?.home ?? 0, gA = m.goals?.away ?? 0;
            const fecha = m.fixture?.date
                ? new Date(m.fixture.date).toLocaleDateString("es-ES", { day:"2-digit", month:"2-digit", year:"numeric" })
                : "";
            return `${hN} ${gH} - ${gA} ${aN}  (${fecha})`;
        });
        res.json({
            localNombre: teams?.home?.name ?? "", localEscudo: teams?.home?.logo ?? "",
            visitanteNombre: teams?.away?.name ?? "", visitanteEscudo: teams?.away?.logo ?? "",
            ganadorNombre: pred?.winner?.name ?? pred?.winner?.comment ?? "Sin datos",
            consejo: pred?.advice ?? "",
            pctLocal: pred?.percent?.home ?? "?", pctEmpate: pred?.percent?.draw ?? "?",
            pctVisitante: pred?.percent?.away ?? "?",
            golesEsperadosLocal: pred?.goals?.home ?? "?",
            golesEsperadosVisitante: pred?.goals?.away ?? "?",
            formaLocal: comp?.form?.home ?? "?", formaVisitante: comp?.form?.away ?? "?",
            ataqueLocal: comp?.att?.home ?? "?", ataqueVisitante: comp?.att?.away ?? "?",
            defensaLocal: comp?.def?.home ?? "?", defensaVisitante: comp?.def?.away ?? "?",
            h2hLocal: comp?.h2h?.home ?? "?", h2hVisitante: comp?.h2h?.away ?? "?",
            totalLocal: comp?.total?.home ?? "?", totalVisitante: comp?.total?.away ?? "?",
            formaRecienteLocal: teams?.home?.league?.form ?? "",
            formaRecienteVisitante: teams?.away?.league?.form ?? "",
            h2hResultados
        });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

// ── Estadísticas de un partido jugado ─────────────────────────
app.get("/estadisticas-partido/:fixtureId", async (req, res) => {
    const { fixtureId } = req.params;
    try {
        const data = await llamarApiFootball(`/fixtures/statistics?fixture=${fixtureId}`);
        const teams = data?.response ?? [];
        if (teams.length < 2) return res.json({});
        const local = teams[0]?.statistics ?? [], vis = teams[1]?.statistics ?? [];
        const getStat = (arr, name) => {
            const s = arr.find(x => x.type === name);
            if (!s || s.value === null) return 0;
            return parseInt(String(s.value).replace("%", "")) || 0;
        };
        res.json({
            posesionLocal: getStat(local, "Ball Possession"), posesionVisitante: getStat(vis, "Ball Possession"),
            tirosLocal: getStat(local, "Total Shots"), tirosVisitante: getStat(vis, "Total Shots"),
            tirosPuertaLocal: getStat(local, "Shots on Goal"), tirosPuertaVisitante: getStat(vis, "Shots on Goal"),
            pasesLocal: getStat(local, "Total passes"), pasesVisitante: getStat(vis, "Total passes"),
            precisionPasesLocal: getStat(local, "Passes accurate"), precisionPasesVisitante: getStat(vis, "Passes accurate"),
            cornersLocal: getStat(local, "Corner Kicks"), cornersVisitante: getStat(vis, "Corner Kicks"),
            faltasLocal: getStat(local, "Fouls"), faltasVisitante: getStat(vis, "Fouls"),
            amarillasLocal: getStat(local, "Yellow Cards"), amarillasVisitante: getStat(vis, "Yellow Cards"),
            rojasLocal: getStat(local, "Red Cards"), rojasVisitante: getStat(vis, "Red Cards"),
            offsidesLocal: getStat(local, "Offsides"), offsidesVisitante: getStat(vis, "Offsides")
        });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

// ── Once probable de un equipo ─────────────────────────────────
// GET /once-probable/:leagueId/:teamId/:fixtureId
// Devuelve: { formacion, jugadores: [{id, nombre, numero, posicion, minutos, foto, baja}] }
app.get("/once-probable/:leagueId/:teamId/:fixtureId", async (req, res) => {
    const { leagueId, teamId, fixtureId } = req.params;
    try {
        // 1. Obtener la formación más usada
        let formacion = "4-3-3";
        try {
            const statsData = await llamarApiFootball(
                `/teams/statistics?league=${leagueId}&season=${SEASON}&team=${teamId}`
            );
            const lineups = statsData?.response?.lineups ?? [];
            if (lineups.length > 0) {
                formacion = lineups.sort((a, b) => b.played - a.played)[0].formation;
            }
        } catch (e) { console.log("⚠️  No se pudo obtener formación:", e.message); }

        // 2. Obtener lesionados/sancionados para este fixture
        const idsNoDisponibles = new Set();
        try {
            const injuriesData = await llamarApiFootball(
                `/injuries?fixture=${fixtureId}&season=${SEASON}`
            );
            if (injuriesData?.response?.length) {
                injuriesData.response.forEach(item => {
                    // Solo los de este equipo
                    if (String(item.team?.id) === String(teamId)) {
                        idsNoDisponibles.add(item.player?.id);
                    }
                });
            }
            console.log(`🚑 Bajas equipo ${teamId}: ${[...idsNoDisponibles].join(", ") || "ninguna"}`);
        } catch (e) { console.log("⚠️  No se pudo obtener lesionados:", e.message); }

        // 3. Obtener jugadores ordenados por minutos jugados
        const playersData = await llamarApiFootball(
            `/players?team=${teamId}&season=${SEASON}&league=${leagueId}`
        );

        if (!playersData?.response?.length) {
            return res.json({ formacion, jugadores: [] });
        }

        // Traducir posición de inglés largo a código corto
        const traducirPosicion = (pos) => {
            if (!pos) return "M";
            const p = pos.toLowerCase();
            if (p === "goalkeeper")    return "G";
            if (p === "defender")      return "D";
            if (p === "midfielder")    return "M";
            if (p === "attacker")      return "F";
            // Códigos cortos ya correctos
            if (p === "g") return "G";
            if (p === "d") return "D";
            if (p === "m") return "M";
            if (p === "f") return "F";
            return "M";
        };

        // Agrupar por posición y seleccionar los que más minutos tienen
        const todos = playersData.response
            .map(p => ({
                id:       p.player?.id    ?? 0,
                nombre:   p.player?.name  ?? "",
                foto:     p.player?.photo ?? "",
                numero:   p.player?.number ?? 0,
                posicion: traducirPosicion(p.statistics?.[0]?.games?.position),
                minutos:  p.statistics?.[0]?.games?.minutes  ?? 0,
                partidos: p.statistics?.[0]?.games?.appearences ?? 0,
                titular:  p.statistics?.[0]?.games?.lineups ?? 0
            }))
            .filter(p => p.minutos > 0)
            .sort((a, b) => b.titular - a.titular || b.minutos - a.minutos);

        // Separar disponibles y no disponibles
        const disponibles   = todos.filter(p => !idsNoDisponibles.has(p.id));
        const noDisponibles = todos.filter(p => idsNoDisponibles.has(p.id));

        // Seleccionar 11 jugadores respetando posiciones (solo disponibles)
        const lineasNum = formacion.split("-").map(Number);
        const def = lineasNum[0] ?? 4;
        const med = lineasNum.slice(1, -1).reduce((a, b) => a + b, 0);
        const del_ = lineasNum[lineasNum.length - 1] ?? 3;

        const porteros   = disponibles.filter(p => p.posicion === "G").slice(0, 1);
        const defensas   = disponibles.filter(p => p.posicion === "D").slice(0, def);
        const medios     = disponibles.filter(p => p.posicion === "M").slice(0, med);
        const delanteros = disponibles.filter(p => p.posicion === "F").slice(0, del_);

        let once = [...porteros, ...defensas, ...medios, ...delanteros];

        // Si faltan jugadores, rellenar con disponibles del mismo rol
        if (once.length < 11) {
            const idsOnce = new Set(once.map(p => p.id));
            const resto = disponibles.filter(p => !idsOnce.has(p.id));
            once = [...once, ...resto].slice(0, 11);
        }

        console.log(`👕 Once ${teamId}: ${once.length} jugadores disponibles, ${idsNoDisponibles.size} bajas, formación ${formacion}`);
        res.json({ formacion, jugadores: once.slice(0, 11), bajas: noDisponibles });

    } catch (err) {
        console.error(`❌ /once-probable/${leagueId}/${teamId}:`, err.message);
        res.status(500).json({ error: err.message });
    }
});


// ── Estadísticas de un jugador en una liga ────────────────────
// GET /estadisticas-jugador/:playerId/:leagueId
app.get("/estadisticas-jugador/:playerId/:leagueId", async (req, res) => {
    const { playerId, leagueId } = req.params;
    try {
        const data = await llamarApiFootball(
            `/players?id=${playerId}&season=${SEASON}`
        );

        const jugador = data?.response?.[0];
        if (!jugador) return res.json({});

        const player = jugador.player;

        // Buscar las estadísticas de la liga concreta
        // Si no hay datos de esa liga, usar las de la primera liga disponible
        let stats = jugador.statistics?.find(s => String(s.league?.id) === String(leagueId));
        if (!stats && jugador.statistics?.length > 0) {
            // Fallback: la liga con más minutos jugados
            stats = jugador.statistics.reduce((best, s) => {
                return (s.games?.minutes ?? 0) > (best.games?.minutes ?? 0) ? s : best;
            }, jugador.statistics[0]);
        }
        if (!stats) return res.json({});

        const resultado = {
            nombre:              player?.name               ?? "",
            foto:                player?.photo              ?? "",
            equipo:              stats.team?.name           ?? "",
            escudoEquipo:        stats.team?.logo           ?? "",
            posicion:            stats.games?.position      ?? "",
            numero:              stats.games?.number        ?? 0,
            nacionalidad:        player?.nationality        ?? "",
            edad:                player?.age               ?? 0,
            valoracion:          stats.games?.rating
                                    ? parseFloat(stats.games.rating).toFixed(1)
                                    : null,
            // Participación
            partidos:            stats.games?.appearences   ?? 0,
            titularidades:       stats.games?.lineups       ?? 0,
            minutos:             stats.games?.minutes       ?? 0,
            // Ataque
            goles:               stats.goals?.total         ?? 0,
            asistencias:         stats.goals?.assists       ?? 0,
            tirosTotales:        stats.shots?.total         ?? 0,
            tirosPuerta:         stats.shots?.on            ?? 0,
            // Pases
            pasesTotales:        stats.passes?.total        ?? 0,
            pasesClave:          stats.passes?.key          ?? 0,
            precisionPases:      stats.passes?.accuracy     ?? 0,
            // Defensa
            tacklesTotales:      stats.tackles?.total       ?? 0,
            intercepciones:      stats.tackles?.interceptions ?? 0,
            bloqueOS:            stats.tackles?.blocks      ?? 0,
            // Duelos
            duelosTotales:       stats.duels?.total         ?? 0,
            duelosGanados:       stats.duels?.won           ?? 0,
            // Regates
            regatesIntentados:   stats.dribbles?.attempts   ?? 0,
            regatesExitosos:     stats.dribbles?.success    ?? 0,
            // Faltas
            faltasCometidas:     stats.fouls?.committed     ?? 0,
            faltasRecibidas:     stats.fouls?.drawn         ?? 0,
            // Tarjetas
            tarjetasAmarillas:   stats.cards?.yellow        ?? 0,
            tarjetasRojas:       stats.cards?.red           ?? 0,
            // Penaltis
            penaltisGanados:     stats.penalty?.won         ?? 0,
            penaltisConvertidos: stats.penalty?.scored      ?? 0,
            penaltisFallados:    stats.penalty?.missed      ?? 0,
        };

        console.log(`👤 Stats ${resultado.nombre}: ${resultado.goles}G ${resultado.asistencias}A`);
        res.json(resultado);
    } catch (err) {
        console.error(`❌ /estadisticas-jugador/${playerId}/${leagueId}:`, err.message);
        res.status(500).json({ error: err.message });
    }
});


// ── Búsqueda de jugadores por nombre ──────────────────────────
// GET /buscar-jugadores/:nombre/:season
app.get("/buscar-jugadores/:nombre", async (req, res) => {
    const { nombre } = req.params;
    try {
        const data = await llamarApiFootball(
            `/players?search=${encodeURIComponent(nombre)}&season=${SEASON}`
        );

        if (!data?.response?.length) return res.json({ jugadores: [] });

        // Deduplicar por id de jugador (la API puede devolver duplicados)
        const vistos = new Set();
        const jugadores = data.response
            .filter(p => {
                if (vistos.has(p.player?.id)) return false;
                vistos.add(p.player?.id);
                return true;
            })
            .slice(0, 10)
            .map(p => ({
                id:          p.player?.id    ?? 0,
                nombre:      p.player?.name  ?? "",
                equipo:      p.statistics?.[0]?.team?.name ?? "Sin equipo",
                foto:        p.player?.photo ?? "",
                escudoEquipo: p.statistics?.[0]?.team?.logo ?? ""
            }));

        res.json({ jugadores });
    } catch (err) {
        console.error(`❌ /buscar-jugadores/${nombre}:`, err.message);
        res.json({ jugadores: [] });
    }
});

// ── Debug ──────────────────────────────────────────────────────
app.get("/debug", async (req, res) => {
    const endpoint = req.query.q;
    if (!endpoint) return res.json({ uso: "Usa ?q=/endpoint" });
    try { res.json(await llamarApiFootball(endpoint)); }
    catch (err) { res.status(500).json({ error: err.message }); }
});

// ─────────────────────────────────────────────
//  Arranque
// ─────────────────────────────────────────────

comprobarConfiguracion();

app.listen(PORT, () => {
    console.log(`\n🚀 Servidor en http://localhost:${PORT}`);
    console.log(`   Temporada:      ${SEASON}`);
    console.log(`   Clasificación:  GET /clasificacion/140`);
    console.log(`   Jornada:        GET /jornada-actual/140`);
    console.log(`   Partidos:       GET /partidos/140/32`);
    console.log(`   Predicción:     GET /prediccion/FIXTURE_ID`);
    console.log(`   Once probable:  GET /once-probable/140/529\n`);
});