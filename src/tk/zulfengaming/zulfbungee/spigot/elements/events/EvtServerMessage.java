package tk.zulfengaming.zulfbungee.spigot.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import tk.zulfengaming.zulfbungee.spigot.event.events.EventProxyMessage;
import tk.zulfengaming.zulfbungee.universal.socket.objects.client.ClientServer;

@Name("Proxy Server Switch Server")
@Description("When a server messages another server.")
public class EvtServerMessage extends SkriptEvent {

    private Literal<String> title;

    static {
        Skript.registerEvent("Server Message", EvtServerMessage.class, EventProxyMessage.class, "[(bungeecord|bungee|proxy|velocity)] server message [(titled|called)] %string%");

        EventValues.registerEventValue(EventProxyMessage.class, String.class, new Getter<String, EventProxyMessage>() {
            @Override
            public String get(EventProxyMessage eventProxyMessage) {
                return eventProxyMessage.getMessage().getText();
            }
        }, 0);

        EventValues.registerEventValue(EventProxyMessage.class, ClientServer.class, new Getter<ClientServer, EventProxyMessage>() {
            @Override
            public ClientServer get(EventProxyMessage eventProxyMessage) {
                return eventProxyMessage.getMessage().getFrom();
            }
        }, 0);

    }

    @Override
    public boolean init(Literal<?>[] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        title = (Literal<String>) literals[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        EventProxyMessage messageEvent = (EventProxyMessage) event;
        return messageEvent.getMessage().getTitle().equals(title.getSingle());

    }

    @Override
    public String toString(Event event, boolean b) {
        return "server message event with title " + title.toString(event, b);
    }
}
