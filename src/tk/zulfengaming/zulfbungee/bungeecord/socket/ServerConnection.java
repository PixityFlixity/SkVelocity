package tk.zulfengaming.zulfbungee.bungeecord.socket;

import tk.zulfengaming.zulfbungee.bungeecord.ZulfBungeecord;
import tk.zulfengaming.zulfbungee.bungeecord.handlers.DataInHandler;
import tk.zulfengaming.zulfbungee.bungeecord.handlers.DataOutHandler;
import tk.zulfengaming.zulfbungee.bungeecord.interfaces.PacketHandlerManager;
import tk.zulfengaming.zulfbungee.universal.socket.Packet;
import tk.zulfengaming.zulfbungee.universal.socket.PacketTypes;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerConnection implements Runnable {

    private final Server server;
    // plugin instance ?
    private final ZulfBungeecord pluginInstance;

    private final Socket socket;

    private final AtomicBoolean socketConnected = new AtomicBoolean(true);

    private final SocketAddress address;
    private final String id;

    // handling packets
    private final PacketHandlerManager packetManager;

    // data I/O
    private DataInHandler dataInHandler;
    private DataOutHandler dataOutHandler;

    private Packet packetInBuffer;

    private AtomicBoolean running = new AtomicBoolean(true);

    public ServerConnection(Server serverIn, String idIn) throws IOException {
        this.socket = serverIn.getSocket();

        this.packetManager = serverIn.getPacketManager();

        this.pluginInstance = serverIn.getPluginInstance();
        this.server = serverIn;

        this.address = socket.getRemoteSocketAddress();
        this.id = idIn;

        init();
    }

    public void init() throws IOException {

        this.dataInHandler = new DataInHandler(this);
        this.dataOutHandler = new DataOutHandler(this);

        pluginInstance.getTaskManager().newTask(dataInHandler, "DataInHandler");
        pluginInstance.getTaskManager().newTask(dataOutHandler, "DataOutHandler");

    }

    public void run() {

        do {

            try {

                if (isSocketConnected().get()) {

                    Packet packetIn = dataInHandler.getQueue().poll(5, TimeUnit.SECONDS);
                    packetInBuffer = packetIn;

                    if (packetIn != null) {
                        Packet handledPacket = packetManager.handlePacket(packetIn, address);

                        if (packetIn.isReturnable()) {
                            send(handledPacket);
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

            pluginInstance.logInfo("Disconnecting client " + address + " (" + id + ")");

            server.removeServerConnection(this);

            try {

                socket.close();

            } catch (IOException e) {

                pluginInstance.error("Error closing socket on connection " + id);

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
            pluginInstance.error("That packet failed to send.");
            e.printStackTrace();
        }

        if (packetIn.getType() != PacketTypes.HEARTBEAT) {
            pluginInstance.logDebug("Sent packet " + packetIn.getType().toString() + "...");
        }

    }

    public Server getServer() {
        return server;
    }

    public String getId() {
        return id;
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