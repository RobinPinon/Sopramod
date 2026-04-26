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
import com.poc.sopramod.events.db.AngryBeeEvent;
import com.poc.sopramod.events.db.ArmorCurseEvent;
import com.poc.sopramod.events.db.ArmorTrimEvent;
import com.poc.sopramod.events.db.ArrowRainEvent;
import com.poc.sopramod.events.db.BalloonRaceEvent;
import com.poc.sopramod.events.db.BeeEvent;
import com.poc.sopramod.events.db.BlackAndWhiteEvent;
import com.poc.sopramod.events.db.BlazeEvent;
import com.poc.sopramod.events.db.BlindnessEvent;
import com.poc.sopramod.events.db.BlurEvent;
import com.poc.sopramod.events.db.BouncyBlocksEvent;
import com.poc.sopramod.events.db.BulldozeEvent;
import com.poc.sopramod.events.db.CRTEvent;
import com.poc.sopramod.events.db.ChickenRainEvent;
import com.poc.sopramod.events.db.CinematicScreenEvent;
import com.poc.sopramod.events.db.ConstantAttackingEvent;
import com.poc.sopramod.events.db.ConstantInteractingEvent;
import com.poc.sopramod.events.db.CreativeFlightEvent;
import com.poc.sopramod.events.db.CreeperEvent;
import com.poc.sopramod.events.db.CurseRandomGearEvent;
import com.poc.sopramod.events.db.DVDEvent;
import com.poc.sopramod.events.db.DamageItemsEvent;
import com.poc.sopramod.events.db.DeathSightEvent;
import com.poc.sopramod.events.db.DowngradeRandomGearEvent;
import com.poc.sopramod.events.db.DropHandItemEvent;
import com.poc.sopramod.events.db.DropInventoryEvent;
import com.poc.sopramod.events.db.EnchantRandomGearEvent;
import com.poc.sopramod.events.db.EndermiteEvent;
import com.poc.sopramod.events.db.EntityMagnetEvent;
import com.poc.sopramod.events.db.ExplodeNearbyEntitiesEvent;
import com.poc.sopramod.events.db.ExplosivePickaxeEvent;
import com.poc.sopramod.events.db.ExtremeExplosionEvent;
import com.poc.sopramod.events.db.FakeFakeTeleportEvent;
import com.poc.sopramod.events.db.FakeTeleportEvent;
import com.poc.sopramod.events.db.FatigueEvent;
import com.poc.sopramod.events.db.FireEvent;
import com.poc.sopramod.events.db.FixItemsEvent;
import com.poc.sopramod.events.db.FlingEntitiesEvent;
import com.poc.sopramod.events.db.FlipMobsEvent;
import com.poc.sopramod.events.db.FlyingMachineEvent;
import com.poc.sopramod.events.db.ForceForwardEvent;
import com.poc.sopramod.events.db.ForceFrontViewEvent;
import com.poc.sopramod.events.db.ForceHorseRidingEvent;
import com.poc.sopramod.events.db.ForceJump2Event;
import com.poc.sopramod.events.db.ForceJumpEvent;
import com.poc.sopramod.events.db.ForceSneakEvent;
import com.poc.sopramod.events.db.ForceThirdPersonEvent;
import com.poc.sopramod.events.db.ForcefieldEvent;
import com.poc.sopramod.events.db.GiveRandomOreEvent;
import com.poc.sopramod.events.db.GlassSightEvent;
import com.poc.sopramod.events.db.GravitySightEvent;
import com.poc.sopramod.events.db.HalfHeartedEvent;
import com.poc.sopramod.events.db.HauntedChestsEvent;
import com.poc.sopramod.events.db.HealEvent;
import com.poc.sopramod.events.db.HerobrineEvent;
import com.poc.sopramod.events.db.HideEventsEvent;
import com.poc.sopramod.events.db.HighPitchEvent;
import com.poc.sopramod.events.db.HighlightAllMobsEvent;
import com.poc.sopramod.events.db.HorseEvent;
import com.poc.sopramod.events.db.HungryEvent;
import com.poc.sopramod.events.db.HyperSlowEvent;
import com.poc.sopramod.events.db.HyperSpeedEvent;
import com.poc.sopramod.events.db.IgniteNearbyEntitiesEvent;
import com.poc.sopramod.events.db.InfestationEvent;
import com.poc.sopramod.events.db.InfiniteLavaEvent;
import com.poc.sopramod.events.db.IntenseThunderStormEvent;
import com.poc.sopramod.events.db.InvertedColorsEvent;
import com.poc.sopramod.events.db.InvertedControlsEvent;
import com.poc.sopramod.events.db.InvisibleEveryoneEvent;
import com.poc.sopramod.events.db.InvisibleHostileMobsEvent;
import com.poc.sopramod.events.db.InvisiblePlayerEvent;
import com.poc.sopramod.events.db.ItemRainEvent;
import com.poc.sopramod.events.db.JumpscareEvent;
import com.poc.sopramod.events.db.LSDEvent;
import com.poc.sopramod.events.db.LagEvent;
import com.poc.sopramod.events.db.LevitationEvent;
import com.poc.sopramod.events.db.LowFPSEvent;
import com.poc.sopramod.events.db.LowGravityEvent;
import com.poc.sopramod.events.db.LowPitchEvent;
import com.poc.sopramod.events.db.LowRenderDistanceEvent;
import com.poc.sopramod.events.db.LuckyDropsEvent;
import com.poc.sopramod.events.db.MLGBucketEvent;
import com.poc.sopramod.events.db.MeteorRainEvent;
import com.poc.sopramod.events.db.MidasTouchEvent;
import com.poc.sopramod.events.db.MiningSightEvent;
import com.poc.sopramod.events.db.MouseDriftingEvent;
import com.poc.sopramod.events.db.NightVisionEvent;
import com.poc.sopramod.events.db.NoAttackingEvent;
import com.poc.sopramod.events.db.OnRecommenceEvent;
import com.poc.sopramod.events.db.NoDropsEvent;
import com.poc.sopramod.events.db.NoJumpEvent;
import com.poc.sopramod.events.db.NoUseKeyEvent;
import com.poc.sopramod.events.db.NoiseMachineEvent;
import com.poc.sopramod.events.db.NothingEvent;
import com.poc.sopramod.events.db.OnePunchEvent;
import com.poc.sopramod.events.db.OnlyBackwardsEvent;
import com.poc.sopramod.events.db.OnlySidewaysEvent;
import com.poc.sopramod.events.db.PhantomEvent;
import com.poc.sopramod.events.db.PitEvent;
import com.poc.sopramod.events.db.PlaceCobwebBlockEvent;
import com.poc.sopramod.events.db.PlaceLavaBlockEvent;
import com.poc.sopramod.events.db.PoolEvent;
import com.poc.sopramod.events.db.PumpkinViewEvent;
import com.poc.sopramod.events.db.RaidEvent;
import com.poc.sopramod.events.db.RainbowFogEvent;
import com.poc.sopramod.events.db.RainbowPathEvent;
import com.poc.sopramod.events.db.RainbowSheepEverywhereEvent;
import com.poc.sopramod.events.db.RainbowTrailsEvent;
import com.poc.sopramod.events.db.RandomCameraTiltEvent;
import com.poc.sopramod.events.db.RandomCreeperEvent;
import com.poc.sopramod.events.db.RandomDropsEvent;
import com.poc.sopramod.events.db.RandomTPEvent;
import com.poc.sopramod.events.db.RandomizeArmorEvent;
import com.poc.sopramod.events.db.ReducedReachEvent;
import com.poc.sopramod.events.db.RemoveEnchantmentsEvent;
import com.poc.sopramod.events.db.RemoveHeartEvent;
import com.poc.sopramod.events.db.ResistanceEvent;
import com.poc.sopramod.events.db.RideClosestMobEvent;
import com.poc.sopramod.events.db.RollCreditsEvent;
import com.poc.sopramod.events.db.RollingCameraEvent;
import com.poc.sopramod.events.db.SatiationEvent;
import com.poc.sopramod.events.db.ShuffleInventoryEvent;
import com.poc.sopramod.events.db.SilenceEvent;
import com.poc.sopramod.events.db.SilverfishEvent;
import com.poc.sopramod.events.db.SinkholeEvent;
import com.poc.sopramod.events.db.SinkingEvent;
import com.poc.sopramod.events.db.SkyBlockEvent;
import com.poc.sopramod.events.db.SkyEvent;
import com.poc.sopramod.events.db.SlimeEvent;
import com.poc.sopramod.events.db.SlimePyramidEvent;
import com.poc.sopramod.events.db.SlipperyEvent;
import com.poc.sopramod.events.db.SoSweetEvent;
import com.poc.sopramod.events.db.SpawnKillerBunnyEvent;
import com.poc.sopramod.events.db.SpawnPetEvent;
import com.poc.sopramod.events.db.SpawnRainbowSheepEvent;
import com.poc.sopramod.events.db.SpeedEvent;
import com.poc.sopramod.events.db.SpinningMobsEvent;
import com.poc.sopramod.events.db.StarterPackEvent;
import com.poc.sopramod.events.db.StutteringEvent;
import com.poc.sopramod.events.db.Teleport0Event;
import com.poc.sopramod.events.db.TeleportHeavenEvent;
import com.poc.sopramod.events.db.TeleportNearbyEntitiesEvent;
import com.poc.sopramod.events.db.TimelapseEvent;
import com.poc.sopramod.events.db.TimerSpeed2Event;
import com.poc.sopramod.events.db.TimerSpeed5Event;
import com.poc.sopramod.events.db.TimerSpeedHalfEvent;
import com.poc.sopramod.events.db.TntEvent;
import com.poc.sopramod.events.db.TopDownViewEvent;
import com.poc.sopramod.events.db.TrueFrostWalkerEvent;
import com.poc.sopramod.events.db.UltraFovEvent;
import com.poc.sopramod.events.db.UltraLowFovEvent;
import com.poc.sopramod.events.db.UpgradeRandomGearEvent;
import com.poc.sopramod.events.db.UpsideDownEvent;
import com.poc.sopramod.events.db.VerticalScreenEvent;
import com.poc.sopramod.events.db.VexAttackEvent;
import com.poc.sopramod.events.db.VitalsEvent;
import com.poc.sopramod.events.db.VoidSightEvent;
import com.poc.sopramod.events.db.WardenEvent;
import com.poc.sopramod.events.db.XRayEvent;
import com.poc.sopramod.events.db.XpRainEvent;
import com.poc.sopramod.events.db.ZeusUltEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
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
        register(registry, "remove_enchantments", RemoveEnchantmentsEvent.TYPE);
        register(registry, "armor_curse", ArmorCurseEvent.TYPE);
        register(registry, "raid", RaidEvent.TYPE);
        register(registry, "arrow_rain", ArrowRainEvent.TYPE);
        register(registry, "warden", WardenEvent.TYPE);
        register(registry, "blur", BlurEvent.TYPE);
        register(registry, "chicken_rain", ChickenRainEvent.TYPE);
        register(registry, "cinematic_screen", CinematicScreenEvent.TYPE);
        register(registry, "random_tp", RandomTPEvent.TYPE);
        register(registry, "creeper", CreeperEvent.TYPE);
        register(registry, "crt", CRTEvent.TYPE);
        register(registry, "drop_hand_item", DropHandItemEvent.TYPE);
        register(registry, "drop_inventory", DropInventoryEvent.TYPE);
        register(registry, "dvd", DVDEvent.TYPE);
        register(registry, "explode_nearby_entities", ExplodeNearbyEntitiesEvent.TYPE);
        register(registry, "extreme_explosion", ExtremeExplosionEvent.TYPE);
        register(registry, "force_forward", ForceForwardEvent.TYPE);
        register(registry, "force_jump_extreme", ForceJump2Event.TYPE);
        register(registry, "force_jump", ForceJumpEvent.TYPE);
        register(registry, "herobrine", HerobrineEvent.TYPE);
        register(registry, "high_pitch", HighPitchEvent.TYPE);
        register(registry, "hungry", HungryEvent.TYPE);
        register(registry, "hyper_slow", HyperSlowEvent.TYPE);
        register(registry, "hyper_speed", HyperSpeedEvent.TYPE);
        register(registry, "ignite_nearby_entities", IgniteNearbyEntitiesEvent.TYPE);
        register(registry, "intense_thunder_storm", IntenseThunderStormEvent.TYPE);
        register(registry, "inverted_colors", InvertedColorsEvent.TYPE);
        register(registry, "inverted_controls", InvertedControlsEvent.TYPE);
        register(registry, "item_rain", ItemRainEvent.TYPE);
        register(registry, "low_gravity", LowGravityEvent.TYPE);
        register(registry, "low_pitch", LowPitchEvent.TYPE);
        register(registry, "low_render_distance", LowRenderDistanceEvent.TYPE);
        register(registry, "lsd", LSDEvent.TYPE);
        register(registry, "lucky_drops", LuckyDropsEvent.TYPE);
        register(registry, "meteor_rain", MeteorRainEvent.TYPE);
        register(registry, "mouse_drifting", MouseDriftingEvent.TYPE);
        register(registry, "no_drops", NoDropsEvent.TYPE);
        register(registry, "no_jump", NoJumpEvent.TYPE);
        register(registry, "half_hearted", HalfHeartedEvent.TYPE);
        register(registry, "only_backwards", OnlyBackwardsEvent.TYPE);
        register(registry, "only_sideways", OnlySidewaysEvent.TYPE);
        register(registry, "place_lava_block", PlaceLavaBlockEvent.TYPE);
        register(registry, "random_drops", RandomDropsEvent.TYPE);
        register(registry, "reduced_reach", ReducedReachEvent.TYPE);
        register(registry, "roll_credits", RollCreditsEvent.TYPE);
        register(registry, "slippery", SlipperyEvent.TYPE);
        register(registry, "teleport_spawn", Teleport0Event.TYPE);
        register(registry, "teleport_heaven", TeleportHeavenEvent.TYPE);
        register(registry, "timelapse", TimelapseEvent.TYPE);
        register(registry, "tnt", TntEvent.TYPE);
        register(registry, "ultra_fov", UltraFovEvent.TYPE);
        register(registry, "ultra_low_fov", UltraLowFovEvent.TYPE);
        register(registry, "upside_down", UpsideDownEvent.TYPE);
        register(registry, "vertical_screen", VerticalScreenEvent.TYPE);
        //register(registry, "where_is_everything", WhereIsEverythingEvent.TYPE); // No longer works on >1.19
        register(registry, "xp_rain", XpRainEvent.TYPE);
        register(registry, "heal", HealEvent.TYPE);
        register(registry, "randomize_armor", RandomizeArmorEvent.TYPE);
        register(registry, "force_third_person", ForceThirdPersonEvent.TYPE);
        register(registry, "force_front_view", ForceFrontViewEvent.TYPE);
        register(registry, "hide_events", HideEventsEvent.TYPE);
        register(registry, "top_down_view", TopDownViewEvent.TYPE);
        register(registry, "phantom", PhantomEvent.TYPE);
        register(registry, "timer_speed_2", TimerSpeed2Event.TYPE);
        register(registry, "timer_speed_5", TimerSpeed5Event.TYPE);
        register(registry, "timer_speed_half", TimerSpeedHalfEvent.TYPE);
        register(registry, "resistance", ResistanceEvent.TYPE);
        register(registry, "fatigue", FatigueEvent.TYPE);
        register(registry, "blindness", BlindnessEvent.TYPE);
        register(registry, "speed", SpeedEvent.TYPE);
        register(registry, "starter_pack", StarterPackEvent.TYPE);
        register(registry, "damage_items", DamageItemsEvent.TYPE);
        register(registry, "levitation", LevitationEvent.TYPE);
        register(registry, "spinning_mobs", SpinningMobsEvent.TYPE);
        register(registry, "sinkhole", SinkholeEvent.TYPE);
        register(registry, "pool", PoolEvent.TYPE);
        register(registry, "random_creeper", RandomCreeperEvent.TYPE);
        register(registry, "sinking", SinkingEvent.TYPE);
        register(registry, "slime", SlimeEvent.TYPE);
        register(registry, "horse", HorseEvent.TYPE);
        register(registry, "fire", FireEvent.TYPE);
        register(registry, "adventure", AdventureEvent.TYPE);
        register(registry, "pit", PitEvent.TYPE);
        register(registry, "sky", SkyEvent.TYPE);
        register(registry, "pumpkin_view", PumpkinViewEvent.TYPE);
        register(registry, "night_vision", NightVisionEvent.TYPE);
        register(registry, "explosive_pickaxe", ExplosivePickaxeEvent.TYPE);
        register(registry, "highlight_all_mobs", HighlightAllMobsEvent.TYPE);
        register(registry, "upgrade_random_gear", UpgradeRandomGearEvent.TYPE);
        register(registry, "downgrade_random_gear", DowngradeRandomGearEvent.TYPE);
        register(registry, "curse_random_gear", CurseRandomGearEvent.TYPE);
        register(registry, "enchant_random_gear", EnchantRandomGearEvent.TYPE);
        register(registry, "invisible_player", InvisiblePlayerEvent.TYPE);
        register(registry, "invisible_hostile_mobs", InvisibleHostileMobsEvent.TYPE);
        register(registry, "invisible_everyone", InvisibleEveryoneEvent.TYPE);
        register(registry, "vex_attack", VexAttackEvent.TYPE);
        register(registry, "zeus_ult", ZeusUltEvent.TYPE);
        register(registry, "gravity_sight", GravitySightEvent.TYPE);
        register(registry, "death_sight", DeathSightEvent.TYPE);
        register(registry, "glass_sight", GlassSightEvent.TYPE);
        register(registry, "void_sight", VoidSightEvent.TYPE);
        register(registry, "mining_sight", MiningSightEvent.TYPE);
        register(registry, "sky_block", SkyBlockEvent.TYPE);
        register(registry, "ride_closest_mob", RideClosestMobEvent.TYPE);
        register(registry, "true_frost_walker", TrueFrostWalkerEvent.TYPE);
        register(registry, "so_sweet", SoSweetEvent.TYPE);
        register(registry, "place_cobweb_block", PlaceCobwebBlockEvent.TYPE);
        register(registry, "flip_mobs", FlipMobsEvent.TYPE);
        register(registry, "spawn_rainbow_sheep", SpawnRainbowSheepEvent.TYPE);
        register(registry, "fix_items", FixItemsEvent.TYPE);
        register(registry, "midas_touch", MidasTouchEvent.TYPE);
        register(registry, "give_random_ore", GiveRandomOreEvent.TYPE);
        register(registry, "bee", BeeEvent.TYPE);
        register(registry, "angry_bee", AngryBeeEvent.TYPE);
        register(registry, "silverfish", SilverfishEvent.TYPE);
        register(registry, "blaze", BlazeEvent.TYPE);
        register(registry, "endermite", EndermiteEvent.TYPE);
        register(registry, "satiation", SatiationEvent.TYPE);
        register(registry, "vitals", VitalsEvent.TYPE);
        register(registry, "teleport_nearby_entities", TeleportNearbyEntitiesEvent.TYPE);
        register(registry, "force_sneak", ForceSneakEvent.TYPE);
        register(registry, "shuffle_inventory", ShuffleInventoryEvent.TYPE);
        register(registry, "bulldoze", BulldozeEvent.TYPE);
        register(registry, "slime_pyramid", SlimePyramidEvent.TYPE);
        register(registry, "flying_machine", FlyingMachineEvent.TYPE);
        register(registry, "add_heart", AddHeartEvent.TYPE);
        register(registry, "remove_heart", RemoveHeartEvent.TYPE);
        register(registry, "noise_machine", NoiseMachineEvent.TYPE);
        register(registry, "xray", XRayEvent.TYPE);
        register(registry, "lag", LagEvent.TYPE);
        register(registry, "low_fps", LowFPSEvent.TYPE);
        register(registry, "infinite_lava", InfiniteLavaEvent.TYPE);
        register(registry, "random_camera_tilt", RandomCameraTiltEvent.TYPE);
        register(registry, "no_attacking", NoAttackingEvent.TYPE);
        register(registry, "constant_attacking", ConstantAttackingEvent.TYPE);
        register(registry, "spawn_killer_bunny", SpawnKillerBunnyEvent.TYPE);
        register(registry, "spawn_pet", SpawnPetEvent.TYPE);
        register(registry, "haunted_chests", HauntedChestsEvent.TYPE);
        register(registry, "no_use_key", NoUseKeyEvent.TYPE);
        register(registry, "constant_interacting", ConstantInteractingEvent.TYPE);
        register(registry, "mlg_bucket", MLGBucketEvent.TYPE);
        register(registry, "stuttering", StutteringEvent.TYPE);
        register(registry, "force_horse_riding", ForceHorseRidingEvent.TYPE);
        register(registry, "jumpscare", JumpscareEvent.TYPE);
        register(registry, "rolling_camera", RollingCameraEvent.TYPE);
        register(registry, "fling_entities", FlingEntitiesEvent.TYPE);
        register(registry, "black_and_white", BlackAndWhiteEvent.TYPE);
        register(registry, "creative_flight", CreativeFlightEvent.TYPE);
        register(registry, "fake_teleport", FakeTeleportEvent.TYPE);
        register(registry, "fake_fake_teleport", FakeFakeTeleportEvent.TYPE);
        register(registry, "forcefield", ForcefieldEvent.TYPE);
        register(registry, "entity_magnet", EntityMagnetEvent.TYPE);
        register(registry, "one_punch", OnePunchEvent.TYPE);
        register(registry, "infestation", InfestationEvent.TYPE);
        register(registry, "rainbow_path", RainbowPathEvent.TYPE);
        register(registry, "silence", SilenceEvent.TYPE);
        register(registry, "nothing", NothingEvent.TYPE);
        register(registry, "rainbow_trails", RainbowTrailsEvent.TYPE);
        register(registry, "rainbow_sheep_everywhere", RainbowSheepEverywhereEvent.TYPE);
        register(registry, "armor_trim", ArmorTrimEvent.TYPE);
        register(registry, "bouncy_blocks", BouncyBlocksEvent.TYPE);
        register(registry, "balloon_race", BalloonRaceEvent.TYPE);
        register(registry, "rainbow_fog", RainbowFogEvent.TYPE);
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

        //Only enable the stuttering event on a dedicated server, because otherwise worldgen will be all wrong.
        //The MathHelper mixin turning off linear interpolation is only applied on the client, but if this is a singleplayer environment,
        //the integrated server has the modified MathHelper, too, causing incorrect worldgen.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
            eventsToRemove.add(getEventId(StutteringEvent.TYPE));

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
