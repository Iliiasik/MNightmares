package iliiasik.mnightmares.network.payload;

import iliiasik.mnightmares.MidnightNightmares;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UserContentPayload(Map<String, List<String>> content) implements CustomPacketPayload {

    public static final Type<UserContentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MidnightNightmares.MOD_ID, "user_content"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UserContentPayload> STREAM_CODEC =
            StreamCodec.of(UserContentPayload::write, UserContentPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, UserContentPayload p) {
        buf.writeInt(p.content.size());
        for (Map.Entry<String, List<String>> entry : p.content.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue().size());
            for (String s : entry.getValue()) buf.writeUtf(s);
        }
    }

    private static UserContentPayload read(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, List<String>> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int listSize = buf.readInt();
            List<String> list = new ArrayList<>();
            for (int j = 0; j < listSize; j++) list.add(buf.readUtf());
            map.put(key, list);
        }
        return new UserContentPayload(map);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}