/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.poc.sopramod.mixin;

import com.poc.sopramod.client.sound.RandomSoundsHelper;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @ModifyVariable(
        method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private SoundInstance sopramodRemapRandomSounds(SoundInstance sound) {
        return RandomSoundsHelper.remapIfEnabled(sound);
    }

    @ModifyVariable(
        method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private SoundInstance sopramodRemapRandomSoundsDelayed(SoundInstance sound) {
        return RandomSoundsHelper.remapIfEnabled(sound);
    }
}
