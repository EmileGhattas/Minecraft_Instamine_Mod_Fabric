package com.emile.instamine.config;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {
    private static final File CONFIG_FILE = new File("config/instamine.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Map<String, Float>> CONFIG = new HashMap<>();

    public static void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
                return;
            }

            JsonObject root = JsonParser.parseReader(new FileReader(CONFIG_FILE)).getAsJsonObject();
            for (String blockId : root.keySet()) {
                JsonObject tools = root.getAsJsonObject(blockId);
                Map<String, Float> toolMap = new HashMap<>();
                for (String toolId : tools.keySet()) {
                    toolMap.put(toolId, tools.get(toolId).getAsFloat());
                }
                CONFIG.put(blockId, toolMap);
            }
            System.out.println("[InstaMine] Loaded " + CONFIG.size() + " block entries.");
        } catch (Exception e) {
            System.err.println("[InstaMine] Failed to load config: " + e.getMessage());
        }
    }

    public static boolean hasHardness(Identifier block, Identifier tool) {
        String b = block.toString();
        String t = tool.toString();
        return CONFIG.containsKey(b) && CONFIG.get(b).containsKey(t);
    }

    public static float getHardness(Identifier block, Identifier tool) {
        String b = block.toString();
        String t = tool.toString();
        return CONFIG.get(b).get(t);
    }

    public static void setCustomHardness(Identifier block, Identifier tool, float value) {
        String b = block.toString();
        String t = tool.toString();
        CONFIG.computeIfAbsent(b, k -> new HashMap<>()).put(t, value);
        save();
        System.out.println("[InstaMine] Updated hardness for " + b + " / " + t + " = " + value);
    }

    public static void resetHardness(Identifier block, Identifier tool) {
        String b = block.toString();
        String t = tool.toString();
        if (CONFIG.containsKey(b)) {
            CONFIG.get(b).remove(t);
            if (CONFIG.get(b).isEmpty()) CONFIG.remove(b);
            save();
            System.out.println("[InstaMine] Reset hardness for " + b + " / " + t);
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            JsonObject root = new JsonObject();
            for (var entry : CONFIG.entrySet()) {
                JsonObject blockObj = new JsonObject();
                for (var tool : entry.getValue().entrySet()) {
                    blockObj.addProperty(tool.getKey(), tool.getValue());
                }
                root.add(entry.getKey(), blockObj);
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
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
              "minecraft:stone": {
                "minecraft:iron_pickaxe": 1.5
              }
            }
            """;
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write(json);
            }
        } catch (Exception e) {
            System.err.println("[InstaMine] Failed to create default config: " + e.getMessage());
        }
    }

    public static Map<Identifier, Map<Identifier, Float>> viewAll() {
        Map<Identifier, Map<Identifier, Float>> copy = new HashMap<>();
        for (var e : CONFIG.entrySet()) {
            Identifier block = Identifier.of(e.getKey());
            Map<Identifier, Float> inner = new HashMap<>();
            for (var t : e.getValue().entrySet()) {
                inner.put(Identifier.of(t.getKey()), t.getValue());
            }
            copy.put(block, Collections.unmodifiableMap(inner));
        }
        return Collections.unmodifiableMap(copy);
    }
}
