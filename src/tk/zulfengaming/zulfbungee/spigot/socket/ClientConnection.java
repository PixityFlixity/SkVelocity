package tk.zulfengaming.zulfbungee.spigot.socket;

import org.bukkit.scheduler.BukkitTask;
import tk.zulfengaming.zulfbungee.spigot.ZulfBungeeSpigot;
import tk.zulfengaming.zulfbungee.spigot.handlers.*;
import tk.zulfengaming.zulfbungee.spigot.task.tasks.HeartbeatTask;
import tk.zulfengaming.zulfbungee.universal.socket.Packet;
import tk.zulfengaming.zulfbungee.universal.socket.PacketTypes;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientConnection implements Runnable {

    private final ZulfBungeeSpigot pluginInstance;

    // threads

    private BukkitTask heartbeatThread;

    private Socket socket;

    // the latest packet from the queue coming in.
    private final BlockingQueue<Packet> skriptPacketQueue = new SynchronousQueue<>();

    private final AtomicBoolean running = new AtomicBoolean(true);

    // managers

    private final PacketHandlerManager packetHandlerManager;

    private final ClientListenerManager clientListenerManager;

    // other tasks

    private final Phaser socketBarrier;

    private DataOutHandler dataOutHandler;

    private DataInHandler dataInHandler;

    // identification

    private final String serverName;

    public ClientConnection(ZulfBungeeSpigot pluginInstanceIn) throws UnknownHostException {

        this.pluginInstance = pluginInstanceIn;

        this.packetHandlerManager = new PacketHandlerManager(this);

        this.clientListenerManager = new ClientListenerManager(this);

        this.serverName = pluginInstanceIn.getYamlConfig().getString("server-name");

        socketBarrier = clientListenerManager.getSocketBarrier();

        init();

    }

    private void init() {

        HeartbeatTask heartbeatTask = new HeartbeatTask(this);

        this.heartbeatThread = pluginInstance.getTaskManager().newRepeatingTask(heartbeatTask, "Heartbeat", pluginInstance.getYamlConfig().getInt("heartbeat-ticks"));

        this.dataInHandler = new DataInHandler(clientListenerManager, this);
        this.dataOutHandler = new DataOutHandler(clientListenerManager, this);

        socketBarrier.register();

        pluginInstance.getTaskManager().newTask(clientListenerManager, "ClientListenerManager");
        pluginInstance.getTaskManager().newTask(dataInHandler, "DataIn");
        pluginInstance.getTaskManager().newTask(dataOutHandler, "DataOut");


    }


    public void run() {

        do {
            try {

                if (clientListenerManager.isSocketConnected().get()) {

                    Packet packetIn = dataInHandler.getDataQueue().poll(5, TimeUnit.SECONDS);

                    if (packetIn != null) {

                        if (packetIn.shouldHandle()) {

                            packetHandlerManager.handlePacket(packetIn, socket.getRemoteSocketAddress());

                        } else {
                            skriptPacketQueue.put(packetIn);
                        }

                    }
                } else {

                    socketBarrier.arriveAndAwaitAdvance();

                    socket = clientListenerManager.getSocketHandoff().take();
                }

            } catch (InterruptedException ignored) {

            }

        } while (running.get());

        socketBarrier.arriveAndDeregister();

    }

    public Optional<Packet> read() throws InterruptedException {

        if (clientListenerManager.isSocketConnected().get()) {

            return Optional.ofNullable(skriptPacketQueue.poll(5, TimeUnit.SECONDS));

        } else {

            return Optional.empty();

        }
    }

    public void send_direct(Packet packetIn) {

        try {

            if (clientListenerManager.isSocketConnected().get()) {
                dataOutHandler.getDataQueue().put(packetIn);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!(packetIn.getType() == PacketTypes.HEARTBEAT)) {
            pluginInstance.logDebug("Sent packet " + packetIn.getType().toString() + "...");
        }

    }

    public Optional<Packet> send(Packet packetIn) throws InterruptedException {

        send_direct(packetIn);

        return read();

    }

    public AtomicBoolean isRunning() {
        return running;
    }

    public boolean isConnected() {
        return clientListenerManager.isSocketConnected().get();
    }

    public void shutdown() throws IOException {

        if (running.compareAndSet(true, false)) {

            heartbeatThread.cancel();

            clientListenerManager.shutdown();

        }

    }

    public ZulfBungeeSpigot getPluginInstance() {
        return pluginInstance;
    }

    public String getServerName() {
        return serverName;
    }

    public ClientListenerManager getClientManager() {
        return clientListenerManager;
    }
}