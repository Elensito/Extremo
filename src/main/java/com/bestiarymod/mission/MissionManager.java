package com.bestiarymod.mission;

import com.bestiarymod.Extremo;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MissionManager {
    private static final Map<String, MissionEntry> ENTRIES = new LinkedHashMap<>();
    private static final Map<String, MissionEntry> DELETED = new LinkedHashMap<>();
    private static Path configDir;

    public static void loadAll(Path serverConfigDir) {
        ENTRIES.clear();
        configDir = serverConfigDir.resolve("extremo").resolve("missions");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            Extremo.LOGGER.error("Could not create missions config dir", e);
        }

        try {
            Files.list(configDir).filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml")).forEach(p -> {
                try (InputStream is = Files.newInputStream(p)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(is);
                    if (data != null) {
                        MissionEntry entry = MissionEntry.fromYaml(data);
                        if (entry.id != null && !entry.id.isEmpty()) {
                            if (entry.isExpired()) {
                                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                                Extremo.LOGGER.info("Skipped expired mission (deleted): {}", entry.id);
                                return;
                            }
                            ENTRIES.put(entry.id, entry);
                            Extremo.LOGGER.info("Loaded mission: {}", entry.id);
                        }
                    }
                } catch (IOException e) {
                    Extremo.LOGGER.error("Failed to load " + p, e);
                }
            });
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to list mission config files", e);
        }

        Extremo.LOGGER.info("Loaded {} missions", ENTRIES.size());
    }

    public static void reload(Path serverConfigDir) {
        loadAll(serverConfigDir);
    }

    public static void saveEntry(MissionEntry entry) {
        ENTRIES.put(entry.id, entry);
        if (configDir == null) return;
        Path file = configDir.resolve(entry.id.replace(":", "_") + ".yml");
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Representer representer = new Representer(options);
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(representer, options);
            try (Writer writer = Files.newBufferedWriter(file)) {
                yaml.dump(entry.toYaml(), writer);
            }
            Extremo.LOGGER.info("Saved mission: {}", entry.id);
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save " + file, e);
        }
    }

    public static void deleteEntry(String id) {
        ENTRIES.remove(id);
        MissionState.clearMissionData(id);
        if (configDir != null) {
            Path file = configDir.resolve(id.replace(":", "_") + ".yml");
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {}
        }
    }

    public static void autoDelete(String id) {
        ENTRIES.remove(id);
        DELETED.remove(id);
        MissionState.clearMissionData(id);
        if (configDir != null) {
            Path file = configDir.resolve(id.replace(":", "_") + ".yml");
            try { Files.deleteIfExists(file); } catch (IOException ignored) {}
        }
    }

    public static void tickExpired() {
        for (MissionEntry entry : List.copyOf(ENTRIES.values())) {
            if (entry.isExpired()) {
                Extremo.LOGGER.info("Auto-deleting expired mission: {}", entry.id);
                autoDelete(entry.id);
            } else if (entry.maxClaims > 0 && MissionState.getClaimCount(entry.id) >= entry.maxClaims) {
                Extremo.LOGGER.info("Auto-deleting fully claimed mission: {}", entry.id);
                autoDelete(entry.id);
            }
        }
    }

    public static MissionEntry getEntry(String id) {
        MissionEntry e = ENTRIES.get(id);
        return e != null ? e : DELETED.get(id);
    }

    public static Collection<MissionEntry> getAllEntries() {
        return ENTRIES.values();
    }

    public static Collection<MissionEntry> getAllDeleted() {
        return DELETED.values();
    }

    public static boolean hasEntry(String id) {
        return ENTRIES.containsKey(id);
    }
}
