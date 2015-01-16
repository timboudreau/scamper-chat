package com.mastfrog.scamper.chat.base;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.Control;
import com.mastfrog.scamper.DataEncoding;
import com.mastfrog.scamper.ErrorHandler;
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
import com.mastfrog.settings.Settings;
import com.mastfrog.settings.SettingsBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import java.io.IOException;

/**
 *
 * @author Tim Boudreau
 */
public class ScamperClient {

    private final Class<? extends Client> clientType;
    public static final String DEFAULT_HOST = "netbeans.ath.cx";
    public static final int DEFAULT_PORT = 8007;

    private final Module[] modules;

    public ScamperClient(Class<? extends Client> clientType, Module... modules) {
        this.clientType = clientType;
        this.modules = modules;
    }

    public <T> T start(Class<T> type, String... args) throws IOException, InterruptedException {
        return start(null, -1, type, args);
    }

    public <T> T start(String host, int port, Class<T> type, String... args) throws IOException, InterruptedException {
        SctpServerAndClientBuilder builder = new SctpServerAndClientBuilder("scamper-client");
        SettingsBuilder sb = new SettingsBuilder();
        if (host != null) {
            sb.add("host", host);
        }
        if (port > 0) {
            sb.add("port", "" + port);
        }
        // Let command-line arguments supersede the passed values
        sb.parseCommandLineArguments(args);
        Settings settings = sb.build();
        // Ensure the builder has the right values
        builder.withHost(settings.getString("host", host == null ? DEFAULT_HOST : host));
        builder.onPort(settings.getInt("port", port < 0 ? DEFAULT_PORT : port));
        // Add any passed modules
        for (Module module : modules) {
            builder.withModule(module);
        }
        return builder
                // Use gzip compression
                .withModule(new CompressionModule())
                // bindings for the client
                .withModule(new ClientModule())
                // set a sane connect timeout
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                // pass our settings
                .withSettings(settings)
                // use BSON - not necessarily smaller, but faster to encode
                .withDataEncoding(DataEncoding.BSON)
                // Bind handlers for each kind of message
                .bind(REPLY_LIST_USERS, ListUsersReplyHandler.class)
                .bind(SEND_MESSAGE_TO_ROOM, BroadcastHandler.class)
                .bind(ACKNOWLEDGE_MESSAGE, AckHandler.class)
                .bind(ROOM_JOIN_OR_LEAVE, RoomJoinedHandler.class)
                .bind(USER_JOINED_ROOM, JoinLeaveHandler.class)
                .bind(NICKNAME_CHANGED, NicknameChangedHandler.class)
                .bind(SERVER_MESSAGE, ServerMessageHandler.class)
                .bind(REPLY_LIST_ROOMS, ListRoomsReplyHandler.class)
                // build the injector and return whatever object the caller
                // requested
                .buildInjector(args).getInstance(type);
    }

    static class EH implements ErrorHandler {

        private final Client client;

        @Inject
        public EH(Client client) {
            this.client = client;
        }

        @Override
        public void onError(ChannelHandlerContext ctx, Throwable t) {
            client.onError(t);
        }

    }

    static class AddressProvider implements Provider<Address> {

        private final Settings settings;

        @Inject
        AddressProvider(Settings settings) {
            this.settings = settings;
        }

        @Override
        public Address get() {
            String host = settings.getString("host", DEFAULT_HOST);
            int port = settings.getInt("port", DEFAULT_PORT);
            return new Address(host, port);
        }
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
            bind(Address.class).toProvider(AddressProvider.class);
            bind(ErrorHandler.class).to(EH.class);
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
