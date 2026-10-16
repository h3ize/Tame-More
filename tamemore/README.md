# Tame More (NeoForge 1.21.1)

Tame ocelots and foxes like cats. Includes an advancement for each.



## Install into a project
1. Download the NeoForge 1.21.1 MDK (github.com/neoforged/MDK, 1.21.1 branch/tag) and unzip it.
2. In gradle.properties set: mod_id=tamemore, mod_name=Tame More (rest as you like).
3. Delete the MDK example classes in src/main/java and the example files in src/main/resources.
4. Copy this project's `src/main/java` and `src/main/resources` folders into the MDK's `src/main`.
   (META-INF/accesstransformer.cfg is picked up automatically by ModDevGradle.)
5. Run `./gradlew build` -> jar is in build/libs. Or `./gradlew runClient` to test.

## If the build complains
- `serialize(...)` in ModAttachments: swap `TameData.MAP_CODEC` for `TameData.MAP_CODEC.codec()`.
- Access transformer error: the Fox/Ocelot method names changed in your mappings; check them in the decompiled sources.
