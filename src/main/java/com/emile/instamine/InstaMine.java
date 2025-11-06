package com.emile.instamine;

import com.emile.instamine.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

public class InstaMine implements ModInitializer {
	public static final String MOD_ID = "instamine";

	@Override
	public void onInitialize() {
		System.out.println("[InstaMine] Initializing...");
		ConfigManager.load();
		System.out.println("[InstaMine] Config loaded successfully!");
	}
}
