package tk.zulfengaming.zulfbungee.spigot.handlers;

import tk.zulfengaming.zulfbungee.spigot.socket.ClientConnection;
import tk.zulfengaming.zulfbungee.spigot.socket.packets.Heartbeat;
import tk.zulfengaming.zulfbungee.spigot.socket.packets.InvalidConfiguration;
import tk.zulfengaming.zulfbungee.spigot.socket.packets.ServerMessageEvent;
import tk.zulfengaming.zulfbungee.spigot.socket.packets.SwitchServerEvent;
import tk.zulfengaming.zulfbungee.universal.socket.Packet;
import tk.zulfengaming.zulfbungee.universal.socket.PacketTypes;

import java.net.SocketAddress;
import java.util.ArrayList;


public class PacketHandlerManager {

    public final ArrayList<PacketHandler> handlers = new ArrayList<>();

    public PacketHandlerManager(ClientConnection connectionIn) {
        handlers.add(new Heartbeat(connectionIn));
        handlers.add(new SwitchServerEvent(connectionIn));
        handlers.add(new ServerMessageEvent(connectionIn));
        handlers.add(new InvalidConfiguration(connectionIn));

    }

    public PacketHandler getHandler(Packet packetIn) {
        for (PacketHandler packetHandler : handlers)
            for (PacketTypes type : packetHandler.getTypes()) if (type == packetIn.getType()) return packetHandler;
        return null;
    }

    // ease of use. it's an absolute pain in the arse writing it out fully every time
    public void handlePacket(Packet packetIn, SocketAddress address) {
        getHandler(packetIn).handlePacket(packetIn, address);
    }
}