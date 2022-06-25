package tk.zulfengaming.zulfbungee.bungeecord.socket;

import tk.zulfengaming.zulfbungee.bungeecord.ZulfBungeecord;
import tk.zulfengaming.zulfbungee.bungeecord.handlers.DataInHandler;
import tk.zulfengaming.zulfbungee.bungeecord.handlers.DataOutHandler;
import tk.zulfengaming.zulfbungee.bungeecord.handlers.PacketHandlerManager;
import tk.zulfengaming.zulfbungee.universal.socket.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseServerConnection implements Runnable {

    private final Server server;
    // plugin instance ?
    private final ZulfBungeecord pluginInstance;

    private final Socket socket;

    private final AtomicBoolean socketConnected = new AtomicBoolean(true);

    private final SocketAddress address;

    // handling packets
    private final PacketHandlerManager packetManager;

    // data I/O
    private final DataInHandler dataInHandler;
    private final DataOutHandler dataOutHandler;

    private ServerInfo serverInfo;

    private Packet packetInBuffer;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public BaseServerConnection(Server serverIn, Socket socketIn) throws IOException {
        this.socket = socketIn;

        this.packetManager = serverIn.getPacketManager();

        this.pluginInstance = serverIn.getPluginInstance();
        this.server = serverIn;

        this.address = socket.getRemoteSocketAddress();

        this.dataInHandler = new DataInHandler(this);
        this.dataOutHandler = new DataOutHandler(this);

        pluginInstance.getTaskManager().newTask(dataInHandler, "DataInHandler");
        pluginInstance.getTaskManager().newTask(dataOutHandler, "DataOutHandler");
    }


    public void run() {

        do {

            try {

                if (socketConnected.get()) {

                    Packet packetIn = dataInHandler.getQueue().poll(5, TimeUnit.SECONDS);
                    packetInBuffer = packetIn;

                    if (packetIn != null) {

                        try {

                            Packet handledPacket = packetManager.handlePacket(packetIn, this);

                            if (packetIn.isReturnable() && handledPacket != null) {
                                send(handledPacket);
                            }

                        } catch (Exception e) {

                            // Used if unhandled exception occurs
                            pluginInstance.error(String.format("Unhandled exception occurred in connection with address %s", socket.getRemoteSocketAddress()));
                            e.printStackTrace();

                            end();
                        }

                    }
                }

            } catch (InterruptedException ignored) {

            }

        } while (running.get());


    }

    public void shutdown() {
        running.compareAndSet(true, false);
    }

    public void end()  {

        if (running.compareAndSet(true, false)) {

            server.removeServerConnection(this);

            try {

                socket.close();

            } catch (IOException e) {

                pluginInstance.error("Error closing socket on connection " + address);

                e.printStackTrace();
            }
        }
    }

    private Optional<Packet> read() {
        return Optional.ofNullable(packetInBuffer);

    }

    public void send(Packet packetIn) {

        try {

            dataOutHandler.getQueue().put(packetIn);

        } catch (InterruptedException e) {
            pluginInstance.error("That packet failed to send due to thread interruption?:");
            pluginInstance.error(packetIn.toString());
        }

        if (packetIn.getType() != PacketTypes.HEARTBEAT) {
            pluginInstance.logDebug("Sent packet " + packetIn.getType().toString() + "...");
        }

    }

    public void sendScript(Path scriptPathIn, ScriptAction actionIn) {

        pluginInstance.getTaskManager().newTask(() -> {

            String scriptName = scriptPathIn.getFileName().toString();

            try {

                byte[] data = Files.readAllBytes(scriptPathIn);

                send(new Packet(PacketTypes.GLOBAL_SCRIPT, true, true, new ScriptInfo(actionIn,
                        scriptName, data)));

            } catch (IOException e) {
                pluginInstance.error(String.format("Error while parsing script %s!", scriptName));
                e.printStackTrace();
            }

        }, UUID.randomUUID().toString());

    }

    public Server getServer() {
        return server;
    }

    public ServerInfo getClientInfo() {
        return serverInfo;
    }

    public void setClientInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Socket getSocket() {
        return socket;
    }

    public ZulfBungeecord getPluginInstance() {
        return pluginInstance;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public AtomicBoolean isSocketConnected() {
        return socketConnected;
    }

    public AtomicBoolean isRunning() {
        return running;
    }
}
