package com.poc.sopramod.client.websocket;

import net.minecraft.network.chat.Component;

public record OverlayVoteOption(Component label, String[] matches, int value) {
}
