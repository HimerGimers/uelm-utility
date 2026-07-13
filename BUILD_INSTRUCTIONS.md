# Building Uelm Utility - Complete Guide

## Option 1: GitHub Actions (Easiest - No Setup Required)

This is the **recommended** approach. The GitHub Actions workflow automatically builds the mod whenever you push to GitHub.

### Steps:

1. **Create a GitHub account** (if you don't have one): https://github.com/signup

2. **Create a new repository**:
   - Go to https://github.com/new
   - Name it `uelm-utility` (or anything you want)
   - Choose "Public" or "Private"
   - Click "Create repository"

3. **Push this code to GitHub**:
   - Download and extract the `uelm-utility-source.zip` on your local machine
   - Open Terminal/Command Prompt in the extracted folder
   - Run these commands:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/uelm-utility.git
   git push -u origin main
   ```
   (Replace `YOUR_USERNAME` with your actual GitHub username)

4. **Watch the build**:
   - Go to your GitHub repo
   - Click the "Actions" tab
   - You'll see "Build Uelm Utility Mod" running
   - Wait for it to complete (usually 5-10 minutes on first build, since Forge toolchain is large)

5. **Download the compiled jar**:
   - After the workflow completes, scroll down to "Artifacts"
   - Click "uelm-utility-jar" to download
   - Extract it to get `uelm-utility-1.0.0.jar`

6. **Install the mod**:
   - Put the jar in your `.minecraft/mods/` folder
   - Make sure you have Forge 1.8.9 (11.15.1.2318) installed
   - Launch the game and check the mods menu to confirm it loaded

---

## Option 2: Local Build (If You Have Java 8)

If you prefer to build locally:

### Requirements:
- **Java 8 JDK** (required for 1.8.9 Forge mods)
  - Download from: https://adoptium.net/temurin/releases/?version=8
  - Do NOT use Java 9+; the old ForgeGradle 2.1 will fail silently
  - After install, verify: `java -version` should show `1.8.x`

### Build:
1. Extract `uelm-utility-source.zip`
2. Open Terminal/Command Prompt in the folder
3. Run:
   ```bash
   chmod +x gradlew          # On Mac/Linux only
   ./gradlew build           # or gradlew.bat build on Windows
   ```
4. Wait for the build to complete
5. Your jar will be at: `build/libs/uelm-utility-1.0.0.jar`

### Troubleshooting Local Builds:
- **"command not found: gradle"** → Use `./gradlew` instead (the wrapper)
- **Java version error** → You need Java 8 specifically. Run `java -version` to check.
- **Network timeouts** → ForgeGradle downloads ~500MB on first run. This is normal and takes several minutes.
- **Out of memory** → The build needs ~2GB RAM. Increase with:
  ```bash
  export _JAVA_OPTIONS="-Xmx2G"
  ./gradlew build
  ```

---

## Option 3: Use a Maintained Template (If Classic Build Fails)

If ForgeGradle 2.1 keeps fighting you, use this modern 1.8.9 template instead:

1. Clone the template:
   ```bash
   git clone https://github.com/nea89o/Forge1.8.9Template.git uelm-utility
   cd uelm-utility
   ```

2. Replace `src/main/java` and `src/main/resources` with folders from this project

3. Update `build.gradle` to change the mod name/version to match

4. Build:
   ```bash
   ./gradlew build
   ```

---

## Verifying the Jar

Once you have the jar, you can verify it's valid:

```bash
# Should show your mod class
jar tf uelm-utility-1.0.0.jar | grep UelmUtility

# Expected output:
# com/uelm/utility/UelmUtility.class
# com/uelm/utility/config/ConfigHandler.class
# com/uelm/utility/features/AutoReplant.class
# com/uelm/utility/features/AutoFish.class
# com/uelm/utility/input/KeybindHandler.class
# com/uelm/utility/gui/GuiUelmConfig.class
```

If you see those class files, the jar is valid.

---

## Installing the Compiled Mod

1. **Get Forge 1.8.9**:
   - Download from https://files.minecraftforge.net/net/minecraftforge/forge/index.html
   - Select "1.8.9" and grab the installer (11.15.1.2318 or later)
   - Run it and choose "Install Client"

2. **Install the mod jar**:
   - Open `.minecraft/mods/` folder
   - Drop `uelm-utility-1.0.0.jar` in there

3. **Launch**:
   - Start Minecraft with the Forge profile
   - Check "Mods" menu to confirm Uelm Utility appears

4. **Use the mod**:
   - Press **Right Shift** in-game to open the config
   - You can rebind it in Options > Controls under "Uelm Utility"

---

## Modifying the Mod

If you want to tweak the code:

1. Open the `src/main/java/com/uelm/utility/` folder
2. Edit any `.java` file (e.g., to change default delays or keybind)
3. Commit and push (if using GitHub Actions), or re-run `./gradlew build`
4. The jar will be rebuilt

---

## Still Stuck?

- **Gradle wrapper won't run**: Make sure `gradlew` (Unix) or `gradlew.bat` (Windows) exists and is executable
- **Forge repo 403 errors**: This is a network/proxy issue. Try from a different network or check your firewall.
- **Tests failing**: Tests aren't required for Forge mods. The build should still produce a working jar.

Good luck! 🎮
