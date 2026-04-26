# Chaos Bridge POC

POC minimal pour verifier le flux:

`Node.js WS server -> Minecraft Fabric mod + OBS overlay + dashboard local`

## Structure

- `server` : serveur WebSocket local (`ws://localhost:3000`)
- `minecraft-mod` : mod Fabric (SopraMod) — version MC / loader : voir `minecraft-mod/gradle.properties`
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

## 2) Mod Minecraft (Fabric / SopraMod)

### Compiler (Gradle)

**Prérequis : Java 21** (Loom / Gradle du projet l’exigent ; Java 17 fera échouer la configuration).

Dans le dossier `minecraft-mod` :

**Linux / macOS / Git Bash**

```bash
cd minecraft-mod
./gradlew build
```

**Windows (cmd / PowerShell)**

```bat
cd minecraft-mod
gradlew.bat build
```

Si `java -version` affiche autre chose que 21, force le JDK 21 pour cette session, puis relance le build, par exemple :

**Git Bash (Windows)**

```bash
export JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.10.7-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
cd minecraft-mod
./gradlew build
```

**Cibles utiles**

- `compileJava` : vérifie que le code compile sans produire le JAR final
- `build` : compile, remap, génère les artefacts dans `minecraft-mod/build/libs/`

Récupère le JAR mod **prêt à l’emploi** (souvent nom du type `SopraMod-x.x-fabric*.jar` ; évite le `sources` / `dev` si plusieurs fichiers) et place-le dans le dossier `mods` de ton instance Fabric.

### Lancer le jeu avec le mod

Prérequis :

- Java 21
- Un launcher **Fabric** pour la version **Minecraft** indiquée dans `minecraft-mod/gradle.properties` (champ `minecraft_version`), avec le **Fabric Loader** indiqué (`loader_version`)

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
