# STORM's pop particles
Fabric client mod for Minecraft 1.21.11. Inspired by impact flash-lite by flamesentinell.

- Plays a STORM effect when a totem pops
- 7 styles: STORM Blue, Lime, Gold, Pink, Teal, White, Rainbow
- `/storm` in chat opens the HUD (style, on/off, intensity, test pop)

## Get the .jar
Option A (no install): upload this folder to a new GitHub repo -> Actions tab ->
"Build STORM jar" -> download the artifact -> unzip -> put the .jar in .minecraft/mods.
Option B (local): install JDK 21 + Gradle, run `gradle build`, jar is in build/libs/
(use storms-pop-particles-1.0.0.jar, NOT the -sources one).
Needs Fabric Loader + Fabric API in your mods folder.
