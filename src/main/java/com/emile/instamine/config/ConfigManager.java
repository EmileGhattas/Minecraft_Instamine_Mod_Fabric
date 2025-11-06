package com.emile.instamine.config;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final File CONFIG_FILE = new File("config/instamine.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // block_id -> (tool_id -> hardness)
    private static final Map<String, Map<String, Float>> CONFIG = new HashMap<>();

    public static void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
                return;
            }
            JsonObject root = JsonParser.parseReader(new FileReader(CONFIG_FILE)).getAsJsonObject();
            CONFIG.clear();

            for (String blockId : root.keySet()) {
                JsonObject toolsObj = root.getAsJsonObject(blockId);
                Map<String, Float> toolMap = new HashMap<>();
                for (String toolId : toolsObj.keySet()) {
                    toolMap.put(toolId, toolsObj.get(toolId).getAsFloat());
                }
                CONFIG.put(blockId, toolMap);
            }
            System.out.println("[InstaMine] Loaded " + CONFIG.size() + " block entries.");
        } catch (Exception e) {
            System.err.println("[InstaMine] Failed to load config: " + e.getMessage());
        }
    }

    public static boolean hasHardness(Identifier block, Identifier tool) {
        Map<String, Float> inner = CONFIG.get(block.toString());
        return inner != null && inner.containsKey(tool.toString());
    }

    public static float getHardness(Identifier block, Identifier tool) {
        return CONFIG.get(block.toString()).get(tool.toString());
    }

    public static void setHardness(Identifier block, Identifier tool, float hardness) {
        CONFIG.computeIfAbsent(block.toString(), k -> new HashMap<>())
                .put(tool.toString(), hardness);
        save();
    }

    public static void resetHardness(Identifier block, Identifier tool) {
        String b = block.toString();
        String t = tool.toString();
        Map<String, Float> inner = CONFIG.get(b);
        if (inner != null) {
            inner.remove(t);
            if (inner.isEmpty()) CONFIG.remove(b);
            save();
        }
    }

    public static Map<Identifier, Map<Identifier, Float>> viewAll() {
        Map<Identifier, Map<Identifier, Float>> result = new HashMap<>();
        for (var e : CONFIG.entrySet()) {
            Identifier b = Identifier.tryParse(e.getKey());
            if (b == null) continue;
            Map<Identifier, Float> inner = new HashMap<>();
            for (var t : e.getValue().entrySet()) {
                Identifier tool = Identifier.tryParse(t.getKey());
                if (tool != null) inner.put(tool, t.getValue());
            }
            result.put(b, Map.copyOf(inner));
        }
        return Map.copyOf(result);
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            JsonObject root = new JsonObject();
            for (var e : CONFIG.entrySet()) {
                JsonObject inner = new JsonObject();
                for (var t : e.getValue().entrySet()) {
                    inner.addProperty(t.getKey(), t.getValue());
                }
                root.add(e.getKey(), inner);
            }
            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            System.err.println("[InstaMine] Failed to save config: " + e.getMessage());
        }
    }

    private static void saveDefault() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            String json = """
            {
              "minecraft:deepslate": { "minecraft:netherite_pickaxe": 1.59 },
              "minecraft:granite":   { "minecraft:iron_pickaxe":      1.25 }
            }
            """;
            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                w.write(json);
            }
        } catch (Exception e) {
            System.err.println("[InstaMine] Failed to create default config: " + e.getMessage());
        }
    }
}
