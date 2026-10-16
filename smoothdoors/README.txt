Smooth Doors (NeoForge 1.21.1)

Build: copy gradlew, gradlew.bat and gradle/wrapper/gradle-wrapper.jar from any of your other mod projects into this folder, then run: gradlew build
The jar ends up in build/libs.

Doors, trapdoors and fence gates (including iron doors/trapdoors and modded ones built on the vanilla classes) swing open and closed smoothly.
The animation is purely client side. Nothing about the blocks changes, so a door always stays on the same edge of its block, and the server does not need the mod.

Config:
smoothdoors-client.toml: enabled, durationTicks
