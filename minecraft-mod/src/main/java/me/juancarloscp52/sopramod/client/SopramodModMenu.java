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

package com.poc.sopramod.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.poc.sopramod.client.Screens.SopramodConfigurationScreen;
import com.poc.sopramod.client.Screens.SopramodErrorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;


public class SopramodModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> {
            if (Minecraft.getInstance().level == null) {
                return new SopramodConfigurationScreen(parent);
            } else {
                return new SopramodErrorScreen(parent, Component.translatable("sopramod.options.error"));
            }

        };
    }


}
