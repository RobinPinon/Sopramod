import { WebSocketServer } from "ws";
import { createServer } from "node:http";

type ChaosEvent = {
  title: string;
  triggeredBy: string;
  duration: number;
};

const PORT = 3000;
const AVAILABLE_EVENTS = [
  "0.5x timer speed",
  "2x timer speed",
  "5x timer speed",
  "Add heart",
  "Adventure mode",
  "Arrow rain",
  "Bounce block",
  "Bulldoze",
  "Chicken rain",
  "Cinematic camera",
  "Constant attacking",
  "Creeper anxiety",
  "Creeper !",
  "DvD Screen Saver",
  "Damage player items",
  "Deep Pit",
  "Downgrade Random Gear",
  "Drop inventory",
  "Drop item in hand",
  "Drop multiplier",
  "Enchant Random Gear",
  "Endermites",
  "Entity Magnet",
  "Explode nearby entities",
  "Fake Fake Teleport",
  "Fake Teleport",
  "Fix player items",
  "Fix player vitals",
  "Force front view",
  "Force Third Person view",
  "Force sneak",
  "Forcefield",
  "Give starterpack to players",
  "Half hearted (stored previous number of heart)",
  "help my w key",
  "High pitch",
  "Hyper speed",
  "I cant stop interacting",
  "I should eat smthg",
  "Ignite nearby entities",
  "Infestation",
  "inverted controls",
  "low fps",
  "low pitch",
  "low render distance",
  "Mega explosion",
  "Meteor rain",
  "Mining sight",
  "No attacking allowed",
  "No drops",
  "No interacting allowed",
  "No jump",
  "One punch",
  "Only backwards movement",
  "Only sideways movement",
  "Phantom squad",
  "place lava",
  "Prepare to fight",
  "Pumpkin view",
  "QUAKE FOV",
  "Quicksand",
  "Random drops",
  "Remove heart",
  "Remove hunger",
  "Roll credits",
  "Rolling Camera",
  "Shhh don’t make any noise (warden)",
  "Silence",
  "Silverfish",
  "Sinkhole",
  "Skyblock",
  "Slime pyramid",
  "Spawn TNT",
  "Teleport player a few meters",
  "Teleport player to a random location",
  "Teleport to spawn",
  "Teleport all nearby entities to player",
  "Teleport to heaven",
  "To the moon",
  "Ultra zoom",
  "Upgrade random gear",
  "Vex attack",
  "What is happening",
  "XRay",
  "Zeus Ult"
] as const;

const httpServer = createServer((req, res) => {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    res.end();
    return;
  }

  const url = new URL(req.url ?? "/", `http://${req.headers.host ?? "localhost"}`);
  if (req.method === "GET" && url.pathname === "/test") {
    const title = url.searchParams.get("title") ?? "SPAWN CREEPER";
    const by = url.searchParams.get("by") ?? "TEST_USER";
    const duration = Number(url.searchParams.get("duration") ?? "10");
    const event: ChaosEvent = { title, triggeredBy: by, duration };
    sendTestEvent(event);
    res.writeHead(200, { "content-type": "application/json; charset=utf-8" });
    res.end(JSON.stringify({ ok: true, sent: event }));
    return;
  }

  if (req.method === "GET" && url.pathname === "/events") {
    res.writeHead(200, { "content-type": "application/json; charset=utf-8" });
    res.end(JSON.stringify({ events: AVAILABLE_EVENTS }));
    return;
  }

  res.writeHead(200, { "content-type": "text/plain; charset=utf-8" });
  res.end("Chaos POC server running. Use /test and /events.\n");
});

const wss = new WebSocketServer({ server: httpServer });

function broadcast(data: unknown): void {
  const payload = JSON.stringify(data);
  for (const client of wss.clients) {
    if (client.readyState === client.OPEN) {
      client.send(payload);
    }
  }
}

export function sendTestEvent(event: ChaosEvent): void {
  const envelope = {
    type: "CHAOS_EVENT",
    ...event
  };
  console.log("[server] Sending event:", envelope);
  broadcast(envelope);
}

wss.on("connection", (socket) => {
  console.log("[server] Client connected.");
  socket.on("close", () => console.log("[server] Client disconnected."));
});

httpServer.listen(PORT, () => {
  console.log(`[server] HTTP + WS listening on http://localhost:${PORT}`);
  console.log("[server] Trigger test event with: http://localhost:3000/test");
  sendTestEvent({
    title: "SPAWN CREEPER",
    triggeredBy: "TEST_USER",
    duration: 10
  });
});
