package tk.zulfengaming.zulfbungee.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import tk.zulfengaming.zulfbungee.universal.socket.objects.proxy.ZulfProxyPlayer;
import tk.zulfengaming.zulfbungee.universal.socket.objects.proxy.ZulfProxyServer;
import tk.zulfengaming.zulfbungee.velocity.ZulfVelocity;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer extends ZulfProxyPlayer<ProxyServer> {

    private final Player velocityPlayer;

    private ZulfProxyServer<ProxyServer> server;
    private final ZulfVelocity zulfVelocity;
    private final ProxyServer velocity;

    private final String name;

    private final UUID uuid;

    public VelocityPlayer(Player velocityPlayerIn, ZulfVelocity pluginIn) {

        super(pluginIn.getPlatform());

        this.zulfVelocity = pluginIn;
        this.velocity = zulfVelocity.getPlatform();
        this.velocityPlayer = velocityPlayerIn;
        this.name = velocityPlayer.getUsername();
        this.uuid = velocityPlayer.getUniqueId();

        Optional<ServerConnection> currentServer = velocityPlayer.getCurrentServer();

        if (currentServer.isPresent()) {
            ServerConnection connection = currentServer.get();
            this.server = new VelocityServer(connection.getServer(), pluginIn);
        }


    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return velocityPlayer.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        velocityPlayer.sendMessage(zulfVelocity.getLegacyTextSerialiser().deserialize(message));
    }

    @Override
    public ZulfProxyServer<ProxyServer> getServer() {
        return server;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void connect(ZulfProxyServer<ProxyServer> serverIn) {
        Optional<RegisteredServer> server = velocity.getServer(serverIn.getName());
        server.ifPresent(registeredServer -> velocityPlayer.createConnectionRequest(registeredServer).connect());
    }

    @Override
    public void disconnect(String reason) {
        velocityPlayer.disconnect(zulfVelocity.getLegacyTextSerialiser().deserialize(reason));
    }

}
