package xyz.agmstudio.neobots.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

public abstract class MenuPacket<T> implements CustomPacketPayload {
    private final int id;
    private final T value;

    public MenuPacket(int id, T value) {
        this.id = id;
        this.value = value;
    }
    public int id() {
        return id;
    }
    public T get() {
        return value;
    }

    public static final class BooleanPayload extends MenuPacket<Boolean> {
        public static final Type<BooleanPayload> TYPE = new Type<>(NeoBots.rl("boolean_menu_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BooleanPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, BooleanPayload::id, ByteBufCodecs.BOOL, BooleanPayload::get, BooleanPayload::new
        );

        public BooleanPayload(int id, boolean value) {
            super(id, value);
        }
        @Override public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    public static final class IntegerPayload extends MenuPacket<Integer> {
        public static final Type<IntegerPayload> TYPE = new Type<>(NeoBots.rl("integer_menu_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, IntegerPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, IntegerPayload::id, ByteBufCodecs.INT, IntegerPayload::get, IntegerPayload::new
        );

        public IntegerPayload(int id, int value) {
            super(id, value);
        }
        @Override public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    public static final class StringPayload extends MenuPacket<String> {
        public static final Type<StringPayload> TYPE = new Type<>(NeoBots.rl("string_menu_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StringPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, StringPayload::id, ByteBufCodecs.STRING_UTF8, StringPayload::get, StringPayload::new
        );

        public StringPayload(int id, String value) {
            super(id, value);
        }
        @Override public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    public static final class DoublePayload extends MenuPacket<Double> {
        public static final Type<DoublePayload> TYPE = new Type<>(NeoBots.rl("double_menu_payload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoublePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, DoublePayload::id, ByteBufCodecs.DOUBLE, DoublePayload::get, DoublePayload::new
        );

        public DoublePayload(int id, double value) {
            super(id, value);
        }
        @Override public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}