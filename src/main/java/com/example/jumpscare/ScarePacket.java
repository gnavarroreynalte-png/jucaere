package com.example.jumpscare;

import com.example.jumpscare.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScarePacket {
    private final int index;

    public ScarePacket(int index) { this.index = index; }

    public static void encode(ScarePacket msg, FriendlyByteBuf buf) { buf.writeInt(msg.index); }

    public static ScarePacket decode(FriendlyByteBuf buf) { return new ScarePacket(buf.readInt()); }

    public static void handle(ScarePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.show(msg.index)));
        ctx.get().setPacketHandled(true);
    }
}
