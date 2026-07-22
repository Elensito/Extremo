package com.bestiarymod.config;

import com.bestiarymod.Extremo;
import net.minecraft.resources.Identifier;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BestiaryConfigManager {
    private static final Map<String, BestiaryEntry> ENTRIES = new LinkedHashMap<>();
    private static Path configDir;

    private static String normalizeId(String mobId) {
        Identifier id = Identifier.tryParse(mobId);
        return id != null ? id.toString() : mobId;
    }

    public static void loadAll(Path serverConfigDir) {
        ENTRIES.clear();
        configDir = serverConfigDir.resolve("extremo").resolve("bestiary");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            Extremo.LOGGER.error("Could not create config directory", e);
        }

        try {
            Files.list(configDir).filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml")).forEach(p -> {
                try (InputStream is = Files.newInputStream(p)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(is);
                    if (data != null) {
                        BestiaryEntry entry = BestiaryEntry.fromYaml(data);
                        if (entry.mobId != null && !entry.mobId.isEmpty()) {
                            entry.mobId = normalizeId(entry.mobId);
                            ENTRIES.put(entry.mobId, entry);
                            Extremo.LOGGER.info("Loaded bestiary entry: {}", entry.mobId);
                        }
                    }
                } catch (IOException e) {
                    Extremo.LOGGER.error("Failed to load " + p, e);
                }
            });
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to list config files", e);
        }

        Extremo.LOGGER.info("Loaded {} bestiary entries", ENTRIES.size());
    }

    public static void reload(Path serverConfigDir) {
        loadAll(serverConfigDir);
    }

    public static void saveEntry(BestiaryEntry entry) {
        if (configDir == null) return;
        Path file = configDir.resolve(entry.mobId.replace(":", "_") + ".yml");
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
            Extremo.LOGGER.info("Saved bestiary entry: {}", entry.mobId);
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save " + file, e);
        }
    }

    public static void deleteEntry(String mobId) {
        String key = normalizeId(mobId);
        ENTRIES.remove(key);
        if (configDir != null) {
            Path file = configDir.resolve(key.replace(":", "_") + ".yml");
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {}
        }
    }

    public static BestiaryEntry getEntry(String mobId) {
        return ENTRIES.get(normalizeId(mobId));
    }

    public static void addEntry(String mobId) {
        String normalized = normalizeId(mobId);
        BestiaryEntry entry = new BestiaryEntry(normalized);
        ENTRIES.put(normalized, entry);
        saveEntry(entry);
    }

    public static Collection<BestiaryEntry> getAllEntries() {
        return ENTRIES.values();
    }

    public static boolean hasEntry(String mobId) {
        return ENTRIES.containsKey(normalizeId(mobId));
    }
}
