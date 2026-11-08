package com.pvp_utils.client.NeteaseMusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class NeteaseMusicManager {
    private NeteaseMusicManager() {
    }

    public static void open() {
        Minecraft client = Minecraft.getInstance();
        Screen parent = client.gui.screen();
        client.gui.setScreen(new NeteaseMusicScreen(parent));
    }
}
