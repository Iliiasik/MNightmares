package mnightmares.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record SyncConfigPacket(int minSlideDisplayTimeMs, int maxSlideDisplayTimeMs, int fadeInDurationMs,
                               int fadeOutDurationMs, float overlayOpacity, float textOpacity, float imageOpacity,
                               boolean enableImage, boolean userContentReplaces, boolean hideChatWhenSleeping,
                               int eyeAnimationSpeedMs) {
    public static void encode(SyncConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.minSlideDisplayTimeMs);
        buf.writeInt(packet.maxSlideDisplayTimeMs);
        buf.writeInt(packet.fadeInDurationMs);
        buf.writeInt(packet.fadeOutDurationMs);
        buf.writeFloat(packet.overlayOpacity);
        buf.writeFloat(packet.textOpacity);
        buf.writeFloat(packet.imageOpacity);
        buf.writeBoolean(packet.enableImage);
        buf.writeBoolean(packet.userContentReplaces);
        buf.writeBoolean(packet.hideChatWhenSleeping);
        buf.writeInt(packet.eyeAnimationSpeedMs);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncConfigPacket(
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readInt()
        );
    }
}
