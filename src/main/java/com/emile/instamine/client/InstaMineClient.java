package com.emile.instamine.client;

import net.fabricmc.api.ClientModInitializer;

public final class InstaMineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[InstaMine] Client initialized!");
    }
}
