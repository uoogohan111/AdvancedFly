# AdvancedFly ✈

A feature-rich flight management plugin for **Paper / Spigot 1.21+** built with the Adventure API and clean OOP architecture.

---

## Features

| Feature | Description |
|---|---|
| `/fly [player]` | Toggle flight on/off (self or others) |
| `/flyspeed <1-10>` | Set personal fly speed |
| `/walkspeed <1-10>` | Set personal walk speed |
| `/advancedfly reload` | Hot-reload config without restart |
| `/advancedfly gui` | Open the interactive GUI |
| Action Bar | "✈ Flying" shown continuously while airborne |
| Particles | Configurable particle trail while flying |
| Fall Protection | 3-second fall damage immunity after disabling flight |
| World Blacklist | Disable flight per-world in config |
| Toggle Cooldown | Configurable cooldown between /fly uses |
| Per-player speed | Fly speed saved per player across restarts |
| Vault Economy | Optional cost per /fly toggle |
| PlaceholderAPI | `%advancedfly_status%` placeholder |

---

## Permissions

| Permission | Description | Default |
|---|---|---|
| `advancedfly.fly` | Toggle own flight | op |
| `advancedfly.flyspeed` | Change fly speed | op |
| `advancedfly.walkspeed` | Change walk speed | op |
| `advancedfly.fly.others` | Toggle another player's flight | op |
| `advancedfly.admin` | All permissions + bypass restrictions | op |

---

## Building

### Requirements
- Java 21+
- Maven 3.8+
- Internet connection (downloads Paper API from repo.papermc.io)

```bash
git clone https://github.com/yourname/AdvancedFly.git
cd AdvancedFly
mvn package
# Output: target/AdvancedFly-1.0.0.jar
```

---

## Setup Instructions

### 1. Upload to GitHub

1. Create a new repository on [github.com](https://github.com/new)  
   - Name it `AdvancedFly`
   - Leave it **empty** (no README, no .gitignore)

2. Open a terminal in the project folder and run:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/AdvancedFly.git
git push -u origin main
```

> Replace `YOUR_USERNAME` with your GitHub username.

---

### 2. Enable GitHub Actions

GitHub Actions is **automatically enabled** for all repositories.  
The workflow file at `.github/workflows/build.yml` triggers immediately on your first push.

To confirm it's running:
1. Go to your repository on GitHub
2. Click the **Actions** tab at the top
3. You'll see a workflow run called **"Build AdvancedFly"**

---

### 3. Download the Built JAR

1. Go to your repository → **Actions** tab
2. Click the latest successful workflow run (green checkmark ✅)
3. Scroll down to the **Artifacts** section
4. Click **`AdvancedFly-jar`** to download the ZIP
5. Extract the ZIP — inside is `AdvancedFly-1.0.0.jar`

---

### 4. Install on Your Minecraft Server

1. Stop your Paper/Spigot server if it's running
2. Copy `AdvancedFly-1.0.0.jar` into your server's `plugins/` folder
3. Start the server
4. The plugin will generate `plugins/AdvancedFly/config.yml` on first run
5. Edit `config.yml` to customise worlds, cooldowns, messages, and more
6. Use `/advancedfly reload` to apply config changes without restarting

---

## Configuration

See [`src/main/resources/config.yml`](src/main/resources/config.yml) for the fully documented default config.

Key options:

```yaml
disabled-worlds:
  - world_nether
  - world_the_end

fly-cooldown: 5           # seconds between /fly toggles
default-fly-speed: 5      # 1-10
fall-protection-duration: 3

particles-enabled: true
action-bar-enabled: true

economy-enabled: false
economy-cost: 100.0
```

---

## Optional Integrations

### PlaceholderAPI
Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) and use `%advancedfly_status%` in scoreboards, chat formats, etc.

### Vault + Economy
Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and any economy plugin (EssentialsX Economy, CMI, etc.), then set `economy-enabled: true` in config.

---

## Project Structure

```
AdvancedFly/
├── .github/
│   └── workflows/
│       └── build.yml             ← GitHub Actions CI
├── src/main/
│   ├── java/com/advancedfly/
│   │   ├── AdvancedFlyPlugin.java ← Main class
│   │   ├── commands/
│   │   │   ├── FlyCommand.java
│   │   │   ├── FlySpeedCommand.java
│   │   │   ├── WalkSpeedCommand.java
│   │   │   └── AdvancedFlyAdminCommand.java
│   │   ├── listeners/
│   │   │   ├── PlayerJoinListener.java
│   │   │   ├── PlayerGameModeListener.java
│   │   │   ├── PlayerMoveListener.java
│   │   │   └── PlayerFallProtectionListener.java
│   │   ├── managers/
│   │   │   ├── ConfigManager.java
│   │   │   ├── FlyManager.java
│   │   │   ├── CooldownManager.java
│   │   │   └── SpeedDataManager.java
│   │   ├── gui/
│   │   │   └── FlyGuiMenu.java
│   │   └── hooks/
│   │       ├── PlaceholderHook.java
│   │       └── VaultHook.java
│   └── resources/
│       ├── plugin.yml
│       └── config.yml
└── pom.xml
```

---

## License

MIT — use freely, attribution appreciated.
"# AdvancedFly" 
