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
 * Bootstrap for scamper-chat clients.  To use:  Implements Client and pass
 * it to the constructor.  Call start(), passing it the type of object that
 * will launch your UI - typically this will be something that has the client
 * and an instance of ClientControl in its constructor, which will be injected
 * into it (annotate your constructor with &#064Inject).  Launch your UI
 * and call joinRoom() on your ClientControl to initiate a connection with
 * the server.
 * <p>
 * See the scamper-cli project for an example implementation.
 *
 * @author Tim Boudreau
 */
public class ScamperClient {

    private final Class<? extends Client> clientType;
    public static final String DEFAULT_HOST = "netbeans.ath.cx";
    public static final int DEFAULT_PORT = 8007;

    private final Module[] modules;

    /**
     * Create a new ScamperClient to bootstrap your client application.
     * 
     * @param clientType The class of your implementation of Client.
     * @param modules Any Guice modules that set up bindings your code
     * needs
     */
    public ScamperClient(Class<? extends Client> clientType, Module... modules) {
        this.clientType = clientType;
        this.modules = modules;
    }

    /**
     * Start the client.
     * 
     * @param <T> The type you want to be returned
     * @param type The type you want to be returned
     * @param args Any command-line arguments passed on the command-line - these
     * will be interpreted in the form <code>--name value</code> or just
     * <code>--name</code> for boolean properties, as described in the
     * documentation of <a href="http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-modules/giulius-parent/giulius-settings/target/apidocs/com/mastfrog/settings/SettingsBuilder.html#parseCommandLineArguments-java.lang.String...-">SettingsBuilder</a>.
     * @return An object of the type you passed, instantiated and injected by Guice
     * @throws IOException If something goes wrong
     * @throws InterruptedException If something goes wrong
     */
    public <T> T start(Class<T> type, String... args) throws IOException {
        return start(null, -1, type, args);
    }

    /**
     * Start the client.
     * 
     * @param <T> The type you want to be returned
     * @param host The default host, if not overridden by the passed command-line arguments (e.g. <code>--host foo.com</code>)
     * @param port The defaullt port, if not overridden by the passed command-line arguments (e.g. <code>--port 8007</code>).
     * @param type The type you want to be returned
     * @param args Any command-line arguments passed on the command-line - these
     * will be interpreted in the form <code>--name value</code> or just
     * <code>--name</code> for boolean properties, as described in the
     * documentation of <a href="http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-modules/giulius-parent/giulius-settings/target/apidocs/com/mastfrog/settings/SettingsBuilder.html#parseCommandLineArguments-java.lang.String...-">SettingsBuilder</a>.
     * @return
     * @throws IOException 
     */
    public <T> T start(String host, int port, Class<T> type, String... args) throws IOException {
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
