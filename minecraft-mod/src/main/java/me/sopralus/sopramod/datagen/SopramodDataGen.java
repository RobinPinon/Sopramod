package com.poc.sopramod.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;

public class SopramodDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        Pack pack = fabricDataGenerator.createPack();
        SopramodBlockTagProvider blockTagProvider = pack.addProvider(SopramodBlockTagProvider::new);

        pack.addProvider(SopramodEnchantmentTagProvider::new);
        pack.addProvider(SopramodEntityTypeTagProvider::new);
        pack.addProvider((output, completableFuture) -> new SopramodItemTagProvider(output, completableFuture, blockTagProvider));
    }
}
