/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.poc.sopramod.client.sound;

import com.poc.sopramod.Variables;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Optional;

public final class RandomSoundsHelper {

    private RandomSoundsHelper() {
    }

    public static SoundInstance remapIfEnabled(SoundInstance original) {
        if (!Variables.randomSoundsChaos) {
            return original;
        }
        // SoundManager.play runs before SoundEngine resolves the instance; getVolume()/getPitch()
        // need AbstractSoundInstance.sound non-null. Mirror SoundEngine.play by resolving first.
        if (original.getSound() == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getSoundManager() != null) {
                original.resolve(mc.getSoundManager());
            }
            if (original.getSound() == null) {
                return original;
            }
        }
        RandomSource random = RandomSource.create();
        Optional<Holder.Reference<SoundEvent>> pick = BuiltInRegistries.SOUND_EVENT.getRandom(random);
        if (pick.isEmpty()) {
            return original;
        }
        SoundEvent replacement = pick.get().value();
        float volume = original.getVolume();
        float pitch = original.getPitch();
        SoundSource source = original.getSource();
        double x = original.getX();
        double y = original.getY();
        double z = original.getZ();
        return new SimpleSoundInstance(replacement, source, volume, pitch, random, x, y, z);
    }
}
