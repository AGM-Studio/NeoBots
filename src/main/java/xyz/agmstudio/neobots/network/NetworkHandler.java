package xyz.agmstudio.neobots.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

public class NetworkHandler {
    public enum PacketDirection {
        SERVER, CLIENT, BIDIRECTIONAL
    }
    public static final class PacketHolder<T extends CustomPacketPayload> {
        private final CustomPacketPayload.Type<T> type;
        private final StreamCodec<RegistryFriendlyByteBuf, T> codec;
        private final IPayloadHandler<T> handler;
        private final PacketDirection direction;

        public PacketHolder(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler, PacketDirection direction) {
            this.type = type;
            this.codec = codec;
            this.handler = handler;
            this.direction = direction;
        }

        public void register(PayloadRegistrar registrar) {
            switch (direction) {
                case SERVER -> registrar.playToServer(type, codec, handler);
                case CLIENT -> registrar.playToClient(type, codec, handler);
                case BIDIRECTIONAL -> registrar.playBidirectional(type, codec, handler);
            }
        }
    }
    public static final List<PacketHolder<? extends CustomPacketPayload>> packets = new ArrayList<>();
    public static <T extends CustomPacketPayload> void registerPacket(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler, PacketDirection direction) {
        packets.add(new PacketHolder<>(type, codec, handler, direction));
    }

    public static void registerPackets(IEventBus bus) {
        bus.register(NetworkHandler.class);

        registerPacket(MenuPacket.BooleanPayload.TYPE, MenuPacket.BooleanPayload.STREAM_CODEC, NetworkHandler::handleMenuPackets, PacketDirection.SERVER);
        registerPacket(MenuPacket.IntegerPayload.TYPE, MenuPacket.IntegerPayload.STREAM_CODEC, NetworkHandler::handleMenuPackets, PacketDirection.SERVER);
        registerPacket(MenuPacket.StringPayload.TYPE, MenuPacket.StringPayload.STREAM_CODEC, NetworkHandler::handleMenuPackets, PacketDirection.SERVER);
        registerPacket(MenuPacket.DoublePayload.TYPE, MenuPacket.DoublePayload.STREAM_CODEC, NetworkHandler::handleMenuPackets, PacketDirection.SERVER);
    }

    @SubscribeEvent
    public static void registerPayloads(@NotNull RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        for (PacketHolder<? extends CustomPacketPayload> packet: packets) packet.register(registrar);
    }

    private static <T extends MenuPacket<?>> void handleMenuPackets(T payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof AbstractMenu menu) {
                if (payload instanceof MenuPacket.BooleanPayload packet) menu.handlePacket(packet.id(), packet.get());
                if (payload instanceof MenuPacket.IntegerPayload packet) menu.handlePacket(packet.id(), packet.get());
                if (payload instanceof MenuPacket.StringPayload packet) menu.handlePacket(packet.id(), packet.get());
                if (payload instanceof MenuPacket.DoublePayload packet) menu.handlePacket(packet.id(), packet.get());
            }
        });
    }
}