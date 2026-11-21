package com.example.jumpscare.client;

import com.example.jumpscare.JumpscareMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = JumpscareMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[JumpscareMod.IMAGE_COUNT];
    private static final long SHOW_MS = 400; // cuanto dura cada imagen en pantalla
    private static int current = -1;
    private static long showUntil = 0;

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            TEXTURES[i] = new ResourceLocation(JumpscareMod.MOD_ID, "textures/gui/scare" + i + ".png");
        }
    }

    public static void show(int index) {
        current = index;
        showUntil = System.currentTimeMillis() + SHOW_MS;
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(JumpscareMod.SCARE_SOUND.get(), 1.0f));
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("scare", (gui, graphics, partialTick, w, h) -> {
            if (current < 0 || System.currentTimeMillis() > showUntil) return;
            // imagen de 512x512 estirada a toda la pantalla
            graphics.blit(TEXTURES[current], 0, 0, w, h, 0, 0, 512, 512, 512, 512);
        });
    }
}
