# Chaos Bridge POC

POC minimal pour verifier le flux:

`Node.js WS server -> Minecraft Fabric mod + OBS overlay + dashboard local`

## Structure

- `server` : serveur WebSocket local (`ws://localhost:3000`)
- `minecraft-mod` : mod Fabric client (MC `1.21.2`, loader `0.19.2`)
- `overlay/index.html` : page OBS Browser Source
- `overlay/dashboard.html` : dashboard local avec un bouton par event

## 1) Lancer le serveur local

Prerequis:

- Node.js 20+

Commandes:

```bash
cd server
npm install
npm run dev
```

Le serveur ecoute sur `localhost:3000`.

Pour envoyer un evenement test:

- Ouvrir [http://localhost:3000/test](http://localhost:3000/test)
- Ou avec params:
  - [http://localhost:3000/test?title=SPAWN%20CREEPER&by=TEST_USER&duration=10](http://localhost:3000/test?title=SPAWN%20CREEPER&by=TEST_USER&duration=10)
- Lister les events disponibles:
  - [http://localhost:3000/events](http://localhost:3000/events)

Dashboard de test local (1 bouton = 1 event):

- Ouvrir `overlay/dashboard.html` dans un navigateur
- Le dashboard appelle `http://localhost:3000/test?...` pour chaque click

## 2) Lancer le mod Minecraft Fabric

Prerequis:

- Java 21
- Un launcher Fabric pour MC `1.21.2` avec Fabric loader `0.19.2`
- Gradle (ou IDE qui importe Gradle)

Dans `minecraft-mod`:

```bash
# si gradle n'est pas installe globalement:
JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.10.7-hotspot" PATH="$JAVA_HOME/bin:$PATH" ../.tools/gradle-8.10.2/bin/gradle build
```

Puis utiliser le jar genere dans `minecraft-mod/build/libs` avec Fabric.

Ce mod se connecte a `ws://localhost:3000` et affiche en chat:

`[CHAOS EVENT] <title> by <triggeredBy>`

Comportement special de test:

- `Creeper !` (et `SPAWN CREEPER`) spawn un cochon sur le joueur (safe test)

## 3) Configurer overlay OBS

Dans OBS, ajouter une source Browser avec:

- URL: `file:///D:/SITES/custom-chaos-mod+bot/overlay/index.html`
- Width: `1280`
- Height: `720`
- Cocher "Shutdown source when not visible": off (optionnel)

L'overlay ecoute `ws://localhost:3000`, affiche titre + user, puis se cache apres 5 secondes.

## Test rapide de bout en bout

1. Lancer le serveur (`npm run dev` dans `server`)
2. Ouvrir OBS avec la source Browser (`overlay/index.html`)
3. Lancer Minecraft avec le mod
4. Ouvrir `overlay/dashboard.html` et cliquer sur un event
5. Verifier:
   - message dans le chat Minecraft
   - evenement visible 5s dans OBS

Si les deux reagissent, le bridge POC est valide.
