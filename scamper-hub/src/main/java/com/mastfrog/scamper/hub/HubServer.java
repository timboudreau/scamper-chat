package com.mastfrog.scamper.hub;

import com.google.inject.AbstractModule;
import com.mastfrog.giulius.Ordered;
import com.mastfrog.giulius.ShutdownHookRegistry;
import com.mastfrog.scamper.Control;
import com.mastfrog.scamper.DataEncoding;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.SctpServer;
import com.mastfrog.scamper.SctpServerAndClientBuilder;
import com.mastfrog.scamper.compression.CompressionModule;
import com.mastfrog.scamper.hub.impl.Rooms;
import com.mastfrog.scamper.chat.api.ServerMessage;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.SEND_MESSAGE_TO_ROOM;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.JOIN_ROOM;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REQUEST_LIST_ROOMS;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REQUEST_LIST_USERS;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.SERVER_MESSAGE;
import io.netty.channel.ChannelFuture;
import java.io.IOException;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
public class HubServer extends AbstractModule {

    public static void main(String[] args) throws IOException, InterruptedException {
        Control<SctpServer> control = new SctpServerAndClientBuilder("datedemo")
                .onPort(8007)
                .withWorkerThreads(3)
                .withModule(new CompressionModule())
                .withModule(new HubServer())
                .useLoggingHandler()
                .withDataEncoding(DataEncoding.BSON)
                .bind(JOIN_ROOM, JoinRoomHandler.class)
                .bind(SEND_MESSAGE_TO_ROOM, SendMessageHandler.class)
                .bind(REQUEST_LIST_USERS, ListUsersHandler.class)
                .bind(REQUEST_LIST_ROOMS, ListRoomsHandler.class)
                .buildServer(args);
        SctpServer server = control.get();
        ChannelFuture future = server.start();
        future.sync();
    }

    @Override
    protected void configure() {
        bind(ShutdownNotifier.class).asEagerSingleton();
    }

    @Ordered(Integer.MIN_VALUE)
    static class ShutdownNotifier implements Runnable {

        private final Rooms rooms;

        @Inject
        ShutdownNotifier(Rooms rooms, ShutdownHookRegistry reg) {
            this.rooms = rooms;
            reg.add(this);
        }

        @Override
        public void run() {
            Message<ServerMessage> m = SERVER_MESSAGE.newMessage(new ServerMessage("Server is shutting down.", true));
            rooms.broadcast(m);
        }
    }
}
