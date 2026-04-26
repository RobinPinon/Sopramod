/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.poc.sopramod.mixin;

import com.poc.sopramod.client.Screens.SopramodConfigurationScreen;
import com.poc.sopramod.client.Screens.SopramodErrorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void insertSopramodSettingsButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.translatable("sopramod.options.title"), button -> {
            if (Minecraft.getInstance().level == null) {
                this.minecraft.setScreen(new SopramodConfigurationScreen(this));
            } else {
                this.minecraft.setScreen(new SopramodErrorScreen(this, Component.translatable("sopramod.options.error")));
            }
        }).pos(this.width - 100, this.height - 20).width(100).build());
    }

}
