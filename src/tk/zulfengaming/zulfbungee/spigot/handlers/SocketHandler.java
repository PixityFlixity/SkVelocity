package tk.zulfengaming.zulfbungee.spigot.handlers;

import tk.zulfengaming.zulfbungee.spigot.interfaces.ClientListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Callable;

public class SocketHandler extends ClientListener implements Callable<Optional<Socket>> {

    private final int timeout;

    public SocketHandler(ClientListenerManager clientListenerManagerIn) {
        super(clientListenerManagerIn);

        this.timeout = (int) Math.ceil((getClientListenerManager().getConnection().getHeartbeatTicks() / 20f) * 1000);
    }

    @Override
    public Optional<Socket> call() throws InterruptedException {

        // Fixes potential memory leak

        Socket socket = new Socket();

        try {

            socket.setReuseAddress(true);
            socket.setSoTimeout(timeout);

            socket.bind(new InetSocketAddress(getClientListenerManager().getClientAddress(), getClientListenerManager().getClientPort()));
            socket.connect(new InetSocketAddress(getClientListenerManager().getServerAddress(), getClientListenerManager().getServerPort()));

            return Optional.of(socket);

        } catch (IOException e) {
            // TODO: Hm... improve this.
            Thread.sleep(2000);
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                getClientListenerManager().getPluginInstance().error("Error closing unused socket:");
                e.printStackTrace();
            }
        }

        return Optional.empty();

    }
}

