package iliiasik.mnightmares.network.payload;

import iliiasik.mnightmares.MidnightNightmares;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncConfigPayload(int minSlideDisplayTimeMs, int maxSlideDisplayTimeMs, int fadeInDurationMs,
                                int fadeOutDurationMs, float overlayOpacity, float textOpacity, float imageOpacity,
                                boolean enableImage, boolean userContentReplaces, boolean hideChatWhenSleeping,
                                int eyeAnimationSpeedMs) implements CustomPacketPayload {

    public static final Type<SyncConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MidnightNightmares.MOD_ID, "sync_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigPayload> STREAM_CODEC =
            StreamCodec.of(SyncConfigPayload::write, SyncConfigPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SyncConfigPayload p) {
        buf.writeInt(p.minSlideDisplayTimeMs);
        buf.writeInt(p.maxSlideDisplayTimeMs);
        buf.writeInt(p.fadeInDurationMs);
        buf.writeInt(p.fadeOutDurationMs);
        buf.writeFloat(p.overlayOpacity);
        buf.writeFloat(p.textOpacity);
        buf.writeFloat(p.imageOpacity);
        buf.writeBoolean(p.enableImage);
        buf.writeBoolean(p.userContentReplaces);
        buf.writeBoolean(p.hideChatWhenSleeping);
        buf.writeInt(p.eyeAnimationSpeedMs);
    }

    private static SyncConfigPayload read(RegistryFriendlyByteBuf buf) {
        return new SyncConfigPayload(
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}