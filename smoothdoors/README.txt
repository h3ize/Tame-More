Smooth Doors (NeoForge 1.21.1)

Build: copy gradlew, gradlew.bat and gradle/wrapper/gradle-wrapper.jar from any of your other mod projects into this folder, then run: gradlew build
The jar ends up in build/libs.

Install on both the client and the server. The swing animation is client side. The swing-away behaviour changes door blocks, so the server needs the mod too.

Config:
smoothdoors-client.toml: enabled, durationTicks
smoothdoors-common.toml: directionalDoors

Double Doors: when installed, every door it opens together with the one you click also swings away from you.
