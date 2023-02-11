package tk.zulfengaming.zulfbungee.universal.socket.objects;

public enum PacketTypes {

    PLAYER_SEND_MESSAGE,
    PROXY_PLAYERS,
    PROXY_PLAYER_UUID,
    PLAYER_SERVER,
    PLAYER_SWITCH_SERVER,
    HEARTBEAT,
    CONNECTION_NAME,
    PROXY_CLIENT_INFO,
    NETWORK_VARIABLE_MODIFY,
    NETWORK_VARIABLE_GET,
    PLAYER_ONLINE,
    SERVER_SWITCH_EVENT,
    SERVER_SEND_MESSAGE_EVENT,
    CONNECT_EVENT,
    DISCONNECT_EVENT,
    KICK_PLAYER,
    KICK_EVENT,
    INVALID_CONFIGURATION, // unused for now
    GLOBAL_SCRIPT,

}
