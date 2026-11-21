package com.example.jumpscare;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

@Mod(JumpscareMod.MOD_ID)
public class JumpscareMod {
    public static final String MOD_ID = "jumpscare";
    public static final int IMAGE_COUNT = 3;

    // /gamerule jumpscareEnabled false  -> lo desactiva
    public static final GameRules.Key<GameRules.BooleanValue> ENABLED =
            GameRules.register("jumpscareEnabled", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    // /gamerule jumpscareIntervalTicks N -> cada cuantos ticks (1 tick = 50 ms, el minimo posible)
    public static final GameRules.Key<GameRules.IntegerValue> INTERVAL =
            GameRules.register("jumpscareIntervalTicks", GameRules.Category.MISC, GameRules.IntegerValue.create(20));

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    public static final RegistryObject<SoundEvent> SCARE_SOUND = SOUNDS.register("scare",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "scare")));

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static final Random RANDOM = new Random();

    public JumpscareMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(0, ScarePacket.class, ScarePacket::encode, ScarePacket::decode, ScarePacket::handle);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        GameRules rules = server.getGameRules();
        if (!rules.getBoolean(ENABLED)) return;
        int interval = Math.max(1, rules.getInt(INTERVAL));
        if (server.getTickCount() % interval != 0) return;

        CHANNEL.send(PacketDistributor.ALL.noArg(), new ScarePacket(RANDOM.nextInt(IMAGE_COUNT)));
    }
}
