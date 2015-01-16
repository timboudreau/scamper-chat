package com.mastfrog.scamper.chat.base;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.scamper.Control;
import com.mastfrog.scamper.DataEncoding;
import com.mastfrog.scamper.SctpServerAndClientBuilder;
import com.mastfrog.scamper.Sender;
import com.mastfrog.scamper.compression.CompressionModule;
import com.mastfrog.scamper.chat.spi.Client;
import com.mastfrog.scamper.chat.spi.ClientControl;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.ACKNOWLEDGE_MESSAGE;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.NICKNAME_CHANGED;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REPLY_LIST_USERS;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.SEND_MESSAGE_TO_ROOM;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.ROOM_JOIN_OR_LEAVE;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.SERVER_MESSAGE;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.USER_JOINED_ROOM;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REPLY_LIST_ROOMS;
import io.netty.channel.ChannelOption;
import io.netty.channel.sctp.SctpChannelOption;
import java.io.IOException;

/**
 *
 * @author Tim Boudreau
 */
public class ScamperClient {

    private final Class<? extends Client> clientType;

    private final Module[] modules;

    public ScamperClient(Class<? extends Client> clientType, Module... modules) {
        this.clientType = clientType;
        this.modules = modules;
    }

    public <T> T start(Class<T> type, String... args) throws IOException, InterruptedException {
        SctpServerAndClientBuilder builder = new SctpServerAndClientBuilder("date-demo");
        for (Module module : modules) {
            builder.withModule(module);
        }
        return builder
                .withModule(new CompressionModule())
                .withModule(new ClientModule())
                .withDataEncoding(DataEncoding.BSON)
                .useLoggingHandler()
                .bind(REPLY_LIST_USERS, ListUsersReplyHandler.class)
                .bind(SEND_MESSAGE_TO_ROOM, BroadcastHandler.class)
                .bind(ACKNOWLEDGE_MESSAGE, AckHandler.class)
                .bind(ROOM_JOIN_OR_LEAVE, RoomJoinedHandler.class)
                .bind(USER_JOINED_ROOM, JoinLeaveHandler.class)
                .bind(NICKNAME_CHANGED, NicknameChangedHandler.class)
                .bind(SERVER_MESSAGE, ServerMessageHandler.class)
                .bind(REPLY_LIST_ROOMS, ListRoomsReplyHandler.class)
                .buildInjector(args).getInstance(type);
    }

    private static class ControlImpl extends Control<Sender> {

        private final Dependencies deps;
        private Sender sender;

        @Inject
        public ControlImpl(Dependencies deps) {
            this.deps = deps;
        }

        @Override
        public void shutdown() {
            deps.shutdown();
        }

        @Override
        public synchronized Sender get() {
            return sender == null ? sender = deps.getInstance(Sender.class) : sender;
        }

        @Override
        public Dependencies getInjector() {
            return deps;
        }
    }

    class ClientModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Client.class).to(clientType).in(Scopes.SINGLETON);
            bind(ClientControl.class).to(ClientControlImpl.class);
            bind(TL).to(ControlImpl.class);
            bind(Init.class).asEagerSingleton();
        }
    }

    static class Init {

        @Inject
        Init(Client client, ClientControl ctrl) {
            client.onInit(ctrl);
        }
    }

    static final TypeLiteral<Control<Sender>> TL = new TypeLiteral<Control<Sender>>() {
    };
}
