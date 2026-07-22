package com.bestiarymod.spawn;

import com.bestiarymod.Extremo;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluids;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class SpawnConfigManager {

    private static final Map<String, SpawnEntryConfig> CONFIG = new LinkedHashMap<>();
    private static boolean registered = false;

    public static void init() {
        ensureDefaultConfig();
        load();
        if (!registered) {
            registerSpawns();
            registered = true;
        }
    }

    public static void reload() {
        CONFIG.clear();
        load();
        Extremo.LOGGER.info("Configuración de spawn recargada ({} entradas)", CONFIG.size());
    }

    public static SpawnEntryConfig getConfig(String mobId) {
        return CONFIG.get(mobId);
    }

    public static Collection<SpawnEntryConfig> getAllConfigs() {
        return CONFIG.values();
    }

    private static void ensureDefaultConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("extremo");
        Path configFile = configDir.resolve("spawns.yml");
        if (Files.notExists(configFile)) {
            try {
                Files.createDirectories(configDir);
                InputStream defaultStream = SpawnConfigManager.class.getClassLoader()
                        .getResourceAsStream("config/extremo/spawns.yml");
                if (defaultStream != null) {
                    Files.copy(defaultStream, configFile);
                } else {
                    Files.writeString(configFile, generateDefaultConfig());
                }
                Extremo.LOGGER.info("Creado archivo de configuración de spawn: {}", configFile);
            } catch (IOException e) {
                Extremo.LOGGER.error("No se pudo crear el archivo de spawn", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void load() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("extremo/spawns.yml");
        if (Files.notExists(configFile)) return;

        try (InputStream is = Files.newInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(is);
            if (data == null) return;

            Map<String, Object> spawns = (Map<String, Object>) data.get("spawns");
            if (spawns == null) return;

            for (Map.Entry<String, Object> entry : spawns.entrySet()) {
                String mobId = entry.getKey();
                Map<String, Object> cfg = (Map<String, Object>) entry.getValue();
                if (cfg == null) continue;

                boolean enabled = Boolean.TRUE.equals(cfg.getOrDefault("enabled", false));
                if (!enabled) continue;

                SpawnEntryConfig sec = new SpawnEntryConfig();
                sec.mobId = mobId;
                sec.enabled = Boolean.TRUE.equals(cfg.getOrDefault("enabled", true));
                sec.weight = castInt(cfg.getOrDefault("weight", 10));
                sec.minGroup = castInt(cfg.getOrDefault("min_group", 1));
                sec.maxGroup = castInt(cfg.getOrDefault("max_group", 1));
                sec.cycleInterval = castInt(cfg.getOrDefault("cycle_interval", 40));
                sec.spawnMinDistance = castInt(cfg.getOrDefault("spawn_min_distance", 24));
                sec.spawnMaxDistance = castInt(cfg.getOrDefault("spawn_max_distance", 64));

                String categoryStr = (String) cfg.getOrDefault("spawn_category", "MONSTER");
                try {
                    sec.spawnCategory = MobCategory.valueOf(categoryStr.toUpperCase());
                } catch (Exception e) {
                    sec.spawnCategory = MobCategory.MONSTER;
                }

                Map<String, Object> conditions = (Map<String, Object>) cfg.get("conditions");
                if (conditions != null) {
                    sec.conditions = parseConditions(conditions);
                }

                CONFIG.put(mobId, sec);
                Extremo.LOGGER.info("Cargada configuración de spawn: {} (peso={}, categoría={})",
                        mobId, sec.weight, sec.spawnCategory);
            }
        } catch (IOException e) {
            Extremo.LOGGER.error("Error al leer configuración de spawn", e);
        } catch (ClassCastException e) {
            Extremo.LOGGER.error("Error de formato en spawns.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static SpawnConditions parseConditions(Map<String, Object> cond) {
        SpawnConditions sc = new SpawnConditions();
        sc.dimensions = (List<String>) cond.getOrDefault("dimensions", List.of());
        sc.biomes = (List<String>) cond.getOrDefault("biomes", List.of());

        sc.spawnBlocks = (List<String>) cond.getOrDefault("spawn_blocks", List.of());
        sc.canSeeSky = (Boolean) cond.get("can_see_sky");
        sc.slimeChunk = (Boolean) cond.getOrDefault("slime_chunk", false);
        sc.canSpawnInWater = (Boolean) cond.get("can_spawn_in_water");
        sc.canSpawnInLava = (Boolean) cond.get("can_spawn_in_lava");
        sc.structures = (List<String>) cond.getOrDefault("structures", List.of());

        sc.heightMin = castInt(cond.getOrDefault("height_min", -64));
        sc.heightMax = castInt(cond.getOrDefault("height_max", 320));

        sc.weather = (String) cond.getOrDefault("weather", "any");

        sc.skyAccess = (String) cond.getOrDefault("sky_access", "any");

        sc.maxPlayerDistance = castInt(cond.getOrDefault("max_player_distance", 0));
        sc.minPlayerDistance = castInt(cond.getOrDefault("min_player_distance", 0));

        Map<String, Object> timeRange = (Map<String, Object>) cond.get("time_range");
        if (timeRange != null && Boolean.TRUE.equals(timeRange.get("enabled"))) {
            sc.timeEnabled = true;
            sc.timeMin = castInt(timeRange.getOrDefault("min", 0));
            sc.timeMax = castInt(timeRange.getOrDefault("max", 24000));
        }

        return sc;
    }

    private static void registerSpawns() {
        for (SpawnEntryConfig sec : CONFIG.values()) {
            Extremo.LOGGER.info("[SpawnDebug] Buscando tipo de entidad para: {}", sec.mobId);
            var opt = BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(sec.mobId));
            Extremo.LOGGER.info("[SpawnDebug] Lookup result para {}: present={}, id={}, value={}",
                    sec.mobId, opt.isPresent(), opt.map(ref -> ref.key().identifier()).orElse(null), opt.map(ref -> ref.value()).orElse(null));

            EntityType<?> entityType = opt.map(ref -> ref.value()).orElse(null);
            if (entityType == null) {
                Extremo.LOGGER.warn("[SpawnDebug] Mob desconocido para spawn: {} — NO SE REGISTRA", sec.mobId);
                continue;
            }

            Extremo.LOGGER.info("[SpawnDebug] Tipo resuelto: {} -> {} (class={})",
                    sec.mobId, BuiltInRegistries.ENTITY_TYPE.getKey(entityType), entityType.getClass().getName());

            Predicate<BiomeSelectionContext> biomeSelector = ctx -> {
                SpawnEntryConfig config = CONFIG.get(sec.mobId);
                if (config == null || config.conditions == null) return false;
                SpawnConditions c = config.conditions;

                if (!c.dimensions.isEmpty()) {
                    boolean dimMatch = false;
                    for (String d : c.dimensions) {
                        if (d.equals("minecraft:overworld") && ctx.hasTag(BiomeTags.IS_OVERWORLD)) { dimMatch = true; break; }
                        if (d.equals("minecraft:the_nether") && ctx.hasTag(BiomeTags.IS_NETHER)) { dimMatch = true; break; }
                        if (d.equals("minecraft:the_end") && ctx.hasTag(BiomeTags.IS_END)) { dimMatch = true; break; }
                    }
                    if (!dimMatch) {
                        Extremo.LOGGER.info("[SpawnDebug] BiomeSelector RECHAZADO para {} — dims={} no match en este biome", sec.mobId, c.dimensions);
                        return false;
                    }
                }

                Extremo.LOGGER.info("[SpawnDebug] BiomeSelector ACEPTADO para {}", sec.mobId);
                return true;
            };

            BiomeModifications.addSpawn(biomeSelector, sec.spawnCategory, entityType,
                    sec.weight, sec.minGroup, sec.maxGroup);
            Extremo.LOGGER.info("[SpawnDebug] Registrado spawn natural de {} (entityType={}, categoría {}, peso {}, grupo {}-{})",
                    sec.mobId, BuiltInRegistries.ENTITY_TYPE.getKey(entityType), sec.spawnCategory, sec.weight, sec.minGroup, sec.maxGroup);
        }

        Extremo.LOGGER.info("[SpawnDebug] Sistema de spawn natural iniciado ({} mobs configurados)", CONFIG.size());
    }

    public static boolean checkConditions(ServerLevelAccessor level, BlockPos pos, SpawnConditions c) {
        if (c == null) return true;

        ServerLevel serverLevel = level.getLevel();

        if (pos.getY() < c.heightMin || pos.getY() > c.heightMax) {
            Extremo.LOGGER.info("[SpawnCond] FAIL height: y={} not in [{}, {}]", pos.getY(), c.heightMin, c.heightMax);
            return false;
        }

        int light = serverLevel.getMaxLocalRawBrightness(pos);
        if (light < c.lightMin || light > c.lightMax) {
            Extremo.LOGGER.info("[SpawnCond] FAIL light: {} not in [{}, {}]", light, c.lightMin, c.lightMax);
            return false;
        }

        if (!c.biomes.isEmpty()) {
            var biomeHolder = serverLevel.getBiome(pos);
            String biomeId = biomeHolder.unwrap().map(
                key -> key.identifier().toString(),
                direct -> "unknown"
            );
            if (!c.biomes.contains(biomeId)) {
                Extremo.LOGGER.info("[SpawnCond] FAIL biome: {} not in {}", biomeId, c.biomes);
                return false;
            }
        }

        if (!c.spawnBlocks.isEmpty()) {
            String blockId = BuiltInRegistries.BLOCK.getKey(serverLevel.getBlockState(pos.below()).getBlock()).toString();
            if (!c.spawnBlocks.contains(blockId)) {
                Extremo.LOGGER.info("[SpawnCond] FAIL block: {} not in {}", blockId, c.spawnBlocks);
                return false;
            }
        }

        if (c.canSeeSky != null) {
            boolean canSeeSky = serverLevel.canSeeSky(pos);
            if (c.canSeeSky != canSeeSky) {
                Extremo.LOGGER.info("[SpawnCond] FAIL canSeeSky: required={}, actual={}", c.canSeeSky, canSeeSky);
                return false;
            }
        }

        if (c.skyAccess != null && !c.skyAccess.equals("any")) {
            boolean canSeeSky = serverLevel.canSeeSky(pos);
            if (c.skyAccess.equals("required") && !canSeeSky) {
                Extremo.LOGGER.info("[SpawnCond] FAIL skyAccess=required but canSeeSky=false");
                return false;
            }
            if (c.skyAccess.equals("not_required") && canSeeSky) {
                Extremo.LOGGER.info("[SpawnCond] FAIL skyAccess=not_required but canSeeSky=true");
                return false;
            }
        }

        if (c.slimeChunk) {
            long seed = serverLevel.getSeed();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            Random random = new Random(seed);
            long check = (random.nextLong() * chunkX * chunkX + random.nextLong() * chunkZ * chunkZ) % 7;
            if (check != 0) {
                Extremo.LOGGER.info("[SpawnCond] FAIL slime_chunk");
                return false;
            }
        }

        if (c.canSpawnInWater != null) {
            boolean inWater = serverLevel.getFluidState(pos.below()).isSource();
            if (c.canSpawnInWater != inWater) {
                Extremo.LOGGER.info("[SpawnCond] FAIL water: required={}, actual={}", c.canSpawnInWater, inWater);
                return false;
            }
        }

        if (c.canSpawnInLava != null) {
            boolean inLava = serverLevel.getFluidState(pos.below()).isSourceOfType(Fluids.LAVA);
            if (c.canSpawnInLava != inLava) {
                Extremo.LOGGER.info("[SpawnCond] FAIL lava: required={}, actual={}", c.canSpawnInLava, inLava);
                return false;
            }
        }

        if (!c.structures.isEmpty()) {
            boolean inStructure = false;
            var structureLookup = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
            if (structureLookup.isPresent()) {
                for (String structId : c.structures) {
                    Identifier id = Identifier.tryParse(structId);
                    if (id == null) continue;
                    var structureRef = structureLookup.get().get(id);
                    if (structureRef.isPresent()) {
                        Structure structure = structureRef.get().value();
                        var start = serverLevel.structureManager().getStructureAt(pos, structure);
                        if (start != null && start.isValid()) {
                            inStructure = true;
                            break;
                        }
                    }
                }
            }
            if (!inStructure) {
                Extremo.LOGGER.info("[SpawnCond] FAIL structures: none matched from {}", c.structures);
                return false;
            }
        }

        if (!c.weather.equals("any")) {
            boolean raining = serverLevel.isRaining();
            boolean thundering = serverLevel.isThundering();
            switch (c.weather.toLowerCase()) {
                case "clear" -> { if (raining || thundering) { Extremo.LOGGER.info("[SpawnCond] FAIL weather=clear but raining={} thundering={}", raining, thundering); return false; } }
                case "rain"  -> { if (!raining) { Extremo.LOGGER.info("[SpawnCond] FAIL weather=rain but not raining"); return false; } }
                case "thunder" -> { if (!thundering) { Extremo.LOGGER.info("[SpawnCond] FAIL weather=thunder but not thundering"); return false; } }
            }
        }

        if (c.timeEnabled) {
            long time = serverLevel.getOverworldClockTime() % 24000;
            if (time < c.timeMin || time > c.timeMax) {
                Extremo.LOGGER.info("[SpawnCond] FAIL time: {} not in [{}, {}]", time, c.timeMin, c.timeMax);
                return false;
            }
        }

        if (c.maxPlayerDistance > 0 || c.minPlayerDistance > 0) {
            double nearestDist = serverLevel.players().stream()
                    .mapToDouble(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))
                    .min().orElse(Double.MAX_VALUE);
            double dist = Math.sqrt(nearestDist);
            if (c.maxPlayerDistance > 0 && dist > c.maxPlayerDistance) {
                Extremo.LOGGER.info("[SpawnCond] FAIL maxPlayerDistance: {} > {}", dist, c.maxPlayerDistance);
                return false;
            }
            if (c.minPlayerDistance > 0 && dist < c.minPlayerDistance) {
                Extremo.LOGGER.info("[SpawnCond] FAIL minPlayerDistance: {} < {}", dist, c.minPlayerDistance);
                return false;
            }
        }

        Extremo.LOGGER.info("[SpawnCond] ALL PASSED for pos={}", pos);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static int castInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        return 10;
    }

    // ---- Data classes ----

    public static class SpawnEntryConfig {
        public String mobId;
        public boolean enabled = true;
        public int weight;
        public int minGroup;
        public int maxGroup;
        public int cycleInterval = 40;
        public int spawnMinDistance = 24;
        public int spawnMaxDistance = 64;
        public MobCategory spawnCategory = MobCategory.MONSTER;
        public SpawnConditions conditions = new SpawnConditions();
    }

    public static class SpawnConditions {
        public List<String> dimensions = List.of();
        public List<String> biomes = List.of();
        public List<String> spawnBlocks = List.of();
        public Boolean canSeeSky = null;
        public Boolean slimeChunk = false;
        public Boolean canSpawnInWater = null;
        public Boolean canSpawnInLava = null;
        public List<String> structures = List.of();
        public int lightMin = 0;
        public int lightMax = 15;
        public int heightMin = -64;
        public int heightMax = 320;
        public String weather = "any";
        public String skyAccess = "any";
        public int maxPlayerDistance = 0;
        public int minPlayerDistance = 0;
        public boolean timeEnabled = false;
        public int timeMin = 0;
        public int timeMax = 24000;
    }

    private static String generateDefaultConfig() {
        return """
                # ================================================================
                # CONFIGURACIÓN DE SPAWN NATURAL - Extremo Mod
                # ================================================================
                # Creado automáticamente. Edita este archivo para configurar
                # el spawn natural de mobs personalizados.
                # Usa /extremo reload para recargar.
                # ================================================================
                
                spawns:
                  extremo:dasher:
                    enabled: true
                    weight: 25
                    min_group: 1
                    max_group: 1
                    spawn_category: MONSTER
                    cycle_interval: 40
                    spawn_min_distance: 24
                    spawn_max_distance: 64
                    conditions:
                      dimensions:
                        - minecraft:overworld
                      biomes: []
                """;
    }
}
