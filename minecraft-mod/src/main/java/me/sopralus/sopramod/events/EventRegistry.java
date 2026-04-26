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

package com.poc.sopramod.events;

import com.mojang.serialization.Lifecycle;
import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.db.AddHeartEvent;
import com.poc.sopramod.events.db.AdventureEvent;
import com.poc.sopramod.events.db.ArrowRainEvent;
import com.poc.sopramod.events.db.BouncyBlocksEvent;
import com.poc.sopramod.events.db.BulldozeEvent;
import com.poc.sopramod.events.db.ChickenRainEvent;
import com.poc.sopramod.events.db.CinematicScreenEvent;
import com.poc.sopramod.events.db.ConstantAttackingEvent;
import com.poc.sopramod.events.db.ConstantInteractingEvent;
import com.poc.sopramod.events.db.CreeperEvent;
import com.poc.sopramod.events.db.DVDEvent;
import com.poc.sopramod.events.db.DamageItemsEvent;
import com.poc.sopramod.events.db.DowngradeRandomGearEvent;
import com.poc.sopramod.events.db.DropHandItemEvent;
import com.poc.sopramod.events.db.DropInventoryEvent;
import com.poc.sopramod.events.db.EnchantRandomGearEvent;
import com.poc.sopramod.events.db.ExcaliburEvent;
import com.poc.sopramod.events.db.EndermiteEvent;
import com.poc.sopramod.events.db.EntityMagnetEvent;
import com.poc.sopramod.events.db.ExplodeNearbyEntitiesEvent;
import com.poc.sopramod.events.db.ExtremeExplosionEvent;
import com.poc.sopramod.events.db.FakeFakeTeleportEvent;
import com.poc.sopramod.events.db.FakeTeleportEvent;
import com.poc.sopramod.events.db.FixItemsEvent;
import com.poc.sopramod.events.db.ForceForwardEvent;
import com.poc.sopramod.events.db.ForceFrontViewEvent;
import com.poc.sopramod.events.db.ForceSneakEvent;
import com.poc.sopramod.events.db.ForceThirdPersonEvent;
import com.poc.sopramod.events.db.ForcefieldEvent;
import com.poc.sopramod.events.db.GiantSilverfishEvent;
import com.poc.sopramod.events.db.HalfHeartedEvent;
import com.poc.sopramod.events.db.HighPitchEvent;
import com.poc.sopramod.events.db.HideEventsEvent;
import com.poc.sopramod.events.db.HungryEvent;
import com.poc.sopramod.events.db.HyperSpeedEvent;
import com.poc.sopramod.events.db.IgniteNearbyEntitiesEvent;
import com.poc.sopramod.events.db.InfestationEvent;
import com.poc.sopramod.events.db.InvertedControlsEvent;
import com.poc.sopramod.events.db.LowFPSEvent;
import com.poc.sopramod.events.db.LowPitchEvent;
import com.poc.sopramod.events.db.LowRenderDistanceEvent;
import com.poc.sopramod.events.db.LuckyDropsEvent;
import com.poc.sopramod.events.db.MeteorRainEvent;
import com.poc.sopramod.events.db.MiningSightEvent;
import com.poc.sopramod.events.db.NoAttackingEvent;
import com.poc.sopramod.events.db.NoDropsEvent;
import com.poc.sopramod.events.db.NoJumpEvent;
import com.poc.sopramod.events.db.NoUseKeyEvent;
import com.poc.sopramod.events.db.OnRecommenceEvent;
import com.poc.sopramod.events.db.OnePunchEvent;
import com.poc.sopramod.events.db.OnlyBackwardsEvent;
import com.poc.sopramod.events.db.OnlySidewaysEvent;
import com.poc.sopramod.events.db.PhantomEvent;
import com.poc.sopramod.events.db.PitEvent;
import com.poc.sopramod.events.db.PlaceLavaBlockEvent;
import com.poc.sopramod.events.db.PumpkinViewEvent;
import com.poc.sopramod.events.db.RaidEvent;
import com.poc.sopramod.events.db.RandomCreeperEvent;
import com.poc.sopramod.events.db.RandomDropsEvent;
import com.poc.sopramod.events.db.RandomTPEvent;
import com.poc.sopramod.events.db.RemoveHeartEvent;
import com.poc.sopramod.events.db.RollCreditsEvent;
import com.poc.sopramod.events.db.RollingCameraEvent;
import com.poc.sopramod.events.db.SatiationEvent;
import com.poc.sopramod.events.db.SilenceEvent;
import com.poc.sopramod.events.db.SilverfishEvent;
import com.poc.sopramod.events.db.SinkholeEvent;
import com.poc.sopramod.events.db.SinkingEvent;
import com.poc.sopramod.events.db.SkyBlockEvent;
import com.poc.sopramod.events.db.SkyEvent;
import com.poc.sopramod.events.db.SlimePyramidEvent;
import com.poc.sopramod.events.db.StarterPackEvent;
import com.poc.sopramod.events.db.Teleport0Event;
import com.poc.sopramod.events.db.TeleportHeavenEvent;
import com.poc.sopramod.events.db.TeleportNearbyEntitiesEvent;
import com.poc.sopramod.events.db.TimerSpeed2Event;
import com.poc.sopramod.events.db.TimerSpeed5Event;
import com.poc.sopramod.events.db.TimerSpeedHalfEvent;
import com.poc.sopramod.events.db.TntEvent;
import com.poc.sopramod.events.db.UltraFovEvent;
import com.poc.sopramod.events.db.UltraLowFovEvent;
import com.poc.sopramod.events.db.UpgradeRandomGearEvent;
import com.poc.sopramod.events.db.VexAttackEvent;
import com.poc.sopramod.events.db.VitalsEvent;
import com.poc.sopramod.events.db.WardenEvent;
import com.poc.sopramod.events.db.XRayEvent;
import com.poc.sopramod.events.db.ZeusUltEvent;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EventRegistry {
    private static final Random random = new Random();
    public static final ResourceKey<Registry<EventType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("sopramod", "events"));
    public static final StreamCodec<RegistryFriendlyByteBuf, Event> STREAM_CODEC = ByteBufCodecs.registry(REGISTRY_KEY).dispatch(Event::getType, EventType::streamCodec);
    public static final Registry<EventType<?>> EVENTS = bootstrap();

    private static Registry<EventType<?>> bootstrap() {
        WritableRegistry<EventType<?>> registry = new MappedRegistry<>(REGISTRY_KEY, Lifecycle.stable());
        // Sous-ensemble d’événements (liste joueur) + on_recommence. Le chaos aléatoire et Twitch utilisent ce registre (on_recommence exclu du pool dans getRandomDifferentEvent).
        register(registry, "timer_speed_half", TimerSpeedHalfEvent.TYPE);
        register(registry, "timer_speed_2", TimerSpeed2Event.TYPE);
        register(registry, "timer_speed_5", TimerSpeed5Event.TYPE);
        register(registry, "add_heart", AddHeartEvent.TYPE);
        register(registry, "adventure", AdventureEvent.TYPE);
        register(registry, "arrow_rain", ArrowRainEvent.TYPE);
        register(registry, "bouncy_blocks", BouncyBlocksEvent.TYPE);
        register(registry, "bulldoze", BulldozeEvent.TYPE);
        register(registry, "chicken_rain", ChickenRainEvent.TYPE);
        register(registry, "cinematic_screen", CinematicScreenEvent.TYPE);
        register(registry, "constant_attacking", ConstantAttackingEvent.TYPE);
        register(registry, "random_creeper", RandomCreeperEvent.TYPE);
        register(registry, "creeper", CreeperEvent.TYPE);
        register(registry, "dvd", DVDEvent.TYPE);
        register(registry, "damage_items", DamageItemsEvent.TYPE);
        register(registry, "pit", PitEvent.TYPE);
        register(registry, "downgrade_random_gear", DowngradeRandomGearEvent.TYPE);
        register(registry, "drop_inventory", DropInventoryEvent.TYPE);
        register(registry, "drop_hand_item", DropHandItemEvent.TYPE);
        register(registry, "lucky_drops", LuckyDropsEvent.TYPE);
        register(registry, "enchant_random_gear", EnchantRandomGearEvent.TYPE);
        register(registry, "endermite", EndermiteEvent.TYPE);
        register(registry, "entity_magnet", EntityMagnetEvent.TYPE);
        register(registry, "excalibur", ExcaliburEvent.TYPE);
        register(registry, "explode_nearby_entities", ExplodeNearbyEntitiesEvent.TYPE);
        register(registry, "fake_fake_teleport", FakeFakeTeleportEvent.TYPE);
        register(registry, "fake_teleport", FakeTeleportEvent.TYPE);
        register(registry, "fix_items", FixItemsEvent.TYPE);
        register(registry, "vitals", VitalsEvent.TYPE);
        register(registry, "force_front_view", ForceFrontViewEvent.TYPE);
        register(registry, "force_third_person", ForceThirdPersonEvent.TYPE);
        register(registry, "force_sneak", ForceSneakEvent.TYPE);
        register(registry, "forcefield", ForcefieldEvent.TYPE);
        register(registry, "giant_silverfish", GiantSilverfishEvent.TYPE);
        register(registry, "starter_pack", StarterPackEvent.TYPE);
        register(registry, "half_hearted", HalfHeartedEvent.TYPE);
        register(registry, "force_forward", ForceForwardEvent.TYPE);
        register(registry, "high_pitch", HighPitchEvent.TYPE);
        register(registry, "hyper_speed", HyperSpeedEvent.TYPE);
        register(registry, "constant_interacting", ConstantInteractingEvent.TYPE);
        register(registry, "hungry", HungryEvent.TYPE);
        register(registry, "ignite_nearby_entities", IgniteNearbyEntitiesEvent.TYPE);
        register(registry, "infestation", InfestationEvent.TYPE);
        register(registry, "inverted_controls", InvertedControlsEvent.TYPE);
        register(registry, "low_fps", LowFPSEvent.TYPE);
        register(registry, "low_pitch", LowPitchEvent.TYPE);
        register(registry, "low_render_distance", LowRenderDistanceEvent.TYPE);
        register(registry, "extreme_explosion", ExtremeExplosionEvent.TYPE);
        register(registry, "meteor_rain", MeteorRainEvent.TYPE);
        register(registry, "mining_sight", MiningSightEvent.TYPE);
        register(registry, "no_attacking", NoAttackingEvent.TYPE);
        register(registry, "no_drops", NoDropsEvent.TYPE);
        register(registry, "no_use_key", NoUseKeyEvent.TYPE);
        register(registry, "no_jump", NoJumpEvent.TYPE);
        register(registry, "one_punch", OnePunchEvent.TYPE);
        register(registry, "only_backwards", OnlyBackwardsEvent.TYPE);
        register(registry, "only_sideways", OnlySidewaysEvent.TYPE);
        register(registry, "phantom", PhantomEvent.TYPE);
        register(registry, "place_lava_block", PlaceLavaBlockEvent.TYPE);
        register(registry, "raid", RaidEvent.TYPE);
        register(registry, "pumpkin_view", PumpkinViewEvent.TYPE);
        register(registry, "ultra_fov", UltraFovEvent.TYPE);
        register(registry, "sinking", SinkingEvent.TYPE);
        register(registry, "random_drops", RandomDropsEvent.TYPE);
        register(registry, "remove_heart", RemoveHeartEvent.TYPE);
        register(registry, "satiation", SatiationEvent.TYPE);
        register(registry, "roll_credits", RollCreditsEvent.TYPE);
        register(registry, "rolling_camera", RollingCameraEvent.TYPE);
        register(registry, "warden", WardenEvent.TYPE);
        register(registry, "silence", SilenceEvent.TYPE);
        register(registry, "silverfish", SilverfishEvent.TYPE);
        register(registry, "sinkhole", SinkholeEvent.TYPE);
        register(registry, "sky_block", SkyBlockEvent.TYPE);
        register(registry, "slime_pyramid", SlimePyramidEvent.TYPE);
        register(registry, "tnt", TntEvent.TYPE);
        register(registry, "random_tp", RandomTPEvent.TYPE);
        register(registry, "teleport_spawn", Teleport0Event.TYPE);
        register(registry, "teleport_nearby_entities", TeleportNearbyEntitiesEvent.TYPE);
        register(registry, "teleport_heaven", TeleportHeavenEvent.TYPE);
        register(registry, "sky", SkyEvent.TYPE);
        register(registry, "ultra_low_fov", UltraLowFovEvent.TYPE);
        register(registry, "upgrade_random_gear", UpgradeRandomGearEvent.TYPE);
        register(registry, "vex_attack", VexAttackEvent.TYPE);
        register(registry, "hide_events", HideEventsEvent.TYPE);
        register(registry, "xray", XRayEvent.TYPE);
        register(registry, "zeus_ult", ZeusUltEvent.TYPE);
        register(registry, "on_recommence", OnRecommenceEvent.TYPE);
        return FabricRegistryBuilder.from(registry).buildAndRegister();
    }

    private static void register(Registry<EventType<?>> registry, String id, EventType<?> type) {
        Registry.register(registry, Identifier.fromNamespaceAndPath("sopramod", id), type);
    }

    public static Optional<Holder.Reference<EventType<?>>> getRandomDifferentEvent(List<Event> notThese, List<Event> andAlsoNotThese) {
        return getRandomDifferentEvent(Stream.concat(notThese.stream(), andAlsoNotThese.stream()).toList());
    }

    public static Optional<Holder.Reference<EventType<?>>> getRandomDifferentEvent(List<Event> currentEvents) {

        List<Holder.Reference<EventType<?>>> eventCandidates = EVENTS.listElements().collect(Collectors.toList());
        Set<ResourceKey<EventType<?>>> eventsToRemove = new HashSet<>(Sopramod.getInstance().settings.disabledEventTypes);
        eventsToRemove.add(getEventId(OnRecommenceEvent.TYPE));
        Set<EventCategory> ignoredEventCategories = new HashSet<>();

        currentEvents.forEach(event -> {
            EventType<?> type = event.getType();
            eventsToRemove.add(getEventId(type));

            if (event.getTickCount() > 0 && !event.hasEnded() && type.category() != EventCategory.NONE)
                ignoredEventCategories.add(type.category());
        });

        Level overworld = Sopramod.getInstance().eventHandler.server.overworld();
        eventCandidates.forEach(typeReference -> {
            EventType<?> type = typeReference.value();
            if (!type.doesWorldHaveRequiredFeatures(overworld)
                || !type.isEnabled()
                || ignoredEventCategories.contains(type.category())) {
                eventsToRemove.add(typeReference.key());
            }
        });

        Set<Identifier> ids = eventsToRemove.stream().map(ResourceKey::identifier).collect(Collectors.toSet());
        eventCandidates.removeIf(candidate -> ids.contains(candidate.key().identifier()));
        return getRandomEvent(eventCandidates);
    }

    private static Optional<Holder.Reference<EventType<?>>> getRandomEvent(List<Holder.Reference<EventType<?>>> eventTypes) {
        if(eventTypes.isEmpty())
            return Optional.empty();

        return Optional.of(eventTypes.get(random.nextInt(eventTypes.size())));
    }

    public static ResourceKey<EventType<?>> getEventId(EventType<?> eventType) {
        return EVENTS.getResourceKey(eventType).get();
    }
}
