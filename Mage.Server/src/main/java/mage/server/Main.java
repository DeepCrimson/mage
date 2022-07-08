package mage.server;

import mage.cards.ExpansionSet;
import mage.cards.Sets;
import mage.cards.decks.DeckValidatorFactory;
import mage.cards.repository.CardScanner;
import mage.cards.repository.PluginClassloaderRegistery;
import mage.cards.repository.RepositoryUtil;
import mage.game.match.MatchType;
import mage.interfaces.MageServer;
import mage.remote.Connection;
import mage.server.draft.CubeFactory;
import mage.server.managers.ConfigSettings;
import mage.server.managers.ManagerFactory;
import mage.server.record.UserStatsRepository;
import mage.server.util.*;
import mage.server.util.config.GamePlugin;
import mage.server.util.config.Plugin;
import mage.utils.MageVersion;
import org.jboss.remoting.*;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.callback.ServerInvokerCallbackHandler;
import org.jboss.remoting.transport.Connector;
import org.jboss.remoting.transport.bisocket.BisocketServerInvoker;
import org.jboss.remoting.transport.socket.SocketWrapper;
import org.jboss.remoting.transporter.TransporterClient;
import org.jboss.remoting.transporter.TransporterServer;
import org.w3c.dom.Element;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class Main {

    public static final PluginClassLoader classLoader = new PluginClassLoader();
    private static final MageVersion version = new MageVersion(Main.class);
    private static final String testModeArg = "-testMode=";
    private static final String fastDBModeArg = "-fastDbMode=";
    private static final String adminPasswordArg = "-adminPassword=";
    /**
     * The property that holds the path to the configuration file. Defaults to "config/config.xml".
     * <p>
     * To set up a different one, start the application with the java option "-Dxmage.config.path=&lt;path&gt;"
     */
    private static final String configPathProp = "xmage.config.path";
    private static final File pluginFolder = new File("plugins");
    private static final File extensionFolder = new File("extensions");
    private static final String defaultConfigPath = Paths.get("config", "config.xml").toString();
    private static TransporterServer server;

    // special test mode:
    // - fast game buttons;
    // - cheat commands;
    // - no deck validation;
    // - simplified registration and login (no password check);
    // - debug main menu for GUI and rendering testing;
    private static boolean testMode;

    private static boolean fastDbMode;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        String adminPassword = "";
        for (String arg : args) {
            if (arg.startsWith(testModeArg)) {
                testMode = Boolean.valueOf(arg.replace(testModeArg, ""));
            } else if (arg.startsWith(adminPasswordArg)) {
                adminPassword = arg.replace(adminPasswordArg, "");
                adminPassword = SystemUtil.sanitize(adminPassword);
            } else if (arg.startsWith(fastDBModeArg)) {
                fastDbMode = Boolean.valueOf(arg.replace(fastDBModeArg, ""));
            }
        }

        final String configPath = Optional.ofNullable(System.getProperty(configPathProp))
                .orElse(defaultConfigPath);

        final ConfigWrapper config = new ConfigWrapper(ConfigFactory.loadFromFile(configPath));


        if (config.isAuthenticationActivated()) {
            if (!AuthorizedUserRepository.getInstance().checkAlterAndMigrateAuthorizedUser()) {
                return;
            }
        }

        // db init and updates checks (e.g. cleanup cards db on new version)
        RepositoryUtil.bootstrapLocalDb();

        if (!extensionFolder.exists()) {
            if (!extensionFolder.mkdirs()) {
            }
        }
        File[] extensionDirectories = extensionFolder.listFiles();
        List<ExtensionPackage> extensions = new ArrayList<>();
        if (extensionDirectories != null) {
            for (File f : extensionDirectories) {
                if (f.isDirectory()) {
                    try {
                        extensions.add(ExtensionPackageLoader.loadExtension(f));
                    } catch (IOException e) {
                    }
                }
            }
        }

        if (!extensions.isEmpty()) {
            for (ExtensionPackage pkg : extensions) {
                for (ExpansionSet set : pkg.getSets()) {
                    Sets.getInstance().addSet(set);
                }
                PluginClassloaderRegistery.registerPluginClassloader(pkg.getClassLoader());
            }
        }

        if (fastDbMode) {
            CardScanner.scanned = true;
        } else {
            CardScanner.scan();
        }

        UserStatsRepository.instance.updateUserStats();

        int gameTypes = 0;
        for (GamePlugin plugin : config.getGameTypes()) {
            gameTypes++;
        }

        int tourneyTypes = 0;
        for (GamePlugin plugin : config.getTournamentTypes()) {
            tourneyTypes++;
        }

        int playerTypes = 0;
        for (Plugin plugin : config.getPlayerTypes()) {
            playerTypes++;
        }

        int cubeTypes = 0;
        for (Plugin plugin : config.getDraftCubes()) {
            cubeTypes++;
        }

        int deckTypes = 0;
        for (Plugin plugin : config.getDeckTypes()) {
            deckTypes++;
        }

        for (ExtensionPackage pkg : extensions) {
            for (Map.Entry<String, Class> entry : pkg.getDraftCubes().entrySet()) {
                cubeTypes++;
                CubeFactory.instance.addDraftCube(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Class> entry : pkg.getDeckTypes().entrySet()) {
                deckTypes++;
                DeckValidatorFactory.instance.addDeckType(entry.getKey(), entry.getValue());
            }
        }
        if (gameTypes == 0) {
        }

        Connection connection = new Connection("&maxPoolSize=" + config.getMaxPoolSize());
        connection.setHost(config.getServerAddress());
        connection.setPort(config.getPort());
        final ManagerFactory managerFactory = new MainManagerFactory(config);
        try {
            // Parameter: serializationtype => jboss
            InvokerLocator serverLocator = new InvokerLocator(connection.getURI());
            if (!isAlreadyRunning(config, serverLocator)) {
                server = new MageTransporterServer(managerFactory, serverLocator, new MageServerImpl(managerFactory, adminPassword, testMode), MageServer.class.getName(), new MageServerInvocationHandler(managerFactory));
                server.start();

                if (testMode) {
                }
                initStatistics();
            } else {
            }
        } catch (Exception ex) {
        }
    }

    static void initStatistics() {
        ServerMessagesUtil.instance.setStartDate(System.currentTimeMillis());
    }

    static boolean isAlreadyRunning(ConfigSettings config, InvokerLocator serverLocator) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(SocketWrapper.WRITE_TIMEOUT, String.valueOf(config.getSocketWriteTimeout()));
        metadata.put("generalizeSocketException", "true");
        try {
            MageServer testServer = (MageServer) TransporterClient.createTransporterClient(serverLocator.getLocatorURI(), MageServer.class, metadata);
            if (testServer != null) {
                testServer.getServerState();
                return true;
            }
        } catch (Throwable t) {
            // assume server is not running
        }
        return false;
    }

    static class ClientConnectionListener implements ConnectionListener {

        private final ManagerFactory managerFactory;

        public ClientConnectionListener(ManagerFactory managerFactory) {
            this.managerFactory = managerFactory;
        }

        @Override
        public void handleConnectionException(Throwable throwable, Client client) {
            String sessionId = client.getSessionId();
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                UUID userId = session.get().getUserId();
                StringBuilder sessionInfo = new StringBuilder();
                Optional<User> user = managerFactory.userManager().getUser(userId);
                if (user.isPresent()) {
                    sessionInfo.append(user.get().getName()).append(" [").append(user.get().getGameInfo()).append(']');
                } else {
                    sessionInfo.append("[user missing] ");
                }
                sessionInfo.append(" at ").append(session.get().getHost()).append(" sessionId: ").append(session.get().getId());
                if (throwable instanceof ClientDisconnectedException) {
                    // Seems like the random diconnects from public server land here and should not be handled as explicit disconnects
                    // So it should be possible to reconnect to server and continue games if DisconnectReason is set to LostConnection
                    //managerFactory.sessionManager().disconnect(client.getSessionId(), DisconnectReason.Disconnected);
                    managerFactory.sessionManager().disconnect(client.getSessionId(), DisconnectReason.LostConnection);
                } else {
                    managerFactory.sessionManager().disconnect(client.getSessionId(), DisconnectReason.LostConnection);
                    if (throwable == null) {
                    } else {
                    }
                }
            }

        }

    }
    static class MageTransporterServer extends TransporterServer {

        protected Connector connector;

        public MageTransporterServer(ManagerFactory managerFactory, InvokerLocator locator, Object target, String subsystem, MageServerInvocationHandler serverInvocationHandler) throws Exception {
            super(locator, target, subsystem);
            connector.addInvocationHandler("callback", serverInvocationHandler);
            connector.setLeasePeriod(managerFactory.configSettings().getLeasePeriod());
            connector.addConnectionListener(new ClientConnectionListener(managerFactory));
        }

        public Connector getConnector() throws Exception {
            return connector;
        }

        @Override
        protected Connector getConnector(InvokerLocator locator, Map config, Element xmlConfig) throws Exception {
            Connector c = super.getConnector(locator, config, xmlConfig);
            this.connector = c;
            return c;
        }
    }

    static class MageServerInvocationHandler implements ServerInvocationHandler {

        private final ManagerFactory managerFactory;

        public MageServerInvocationHandler(ManagerFactory managerFactory) {
            this.managerFactory = managerFactory;
        }

        @Override
        public void setMBeanServer(MBeanServer server) {
            /**
             * An MBean is a managed Java object, similar to a JavaBeans
             * component, that follows the design patterns set forth in the JMX
             * specification. An MBean can represent a device, an application,
             * or any resource that needs to be managed. MBeans expose a
             * management interface that consists of the following:
             *
             * A set of readable or writable attributes, or both. A set of
             * invokable operations. A self-description.
             *
             */
            if (server != null) {
            }
        }

        @Override
        public void setInvoker(ServerInvoker invoker) {
            ((BisocketServerInvoker) invoker).setSecondaryBindPort(managerFactory.configSettings().getSecondaryBindPort());
            ((BisocketServerInvoker) invoker).setBacklog(managerFactory.configSettings().getBacklogSize());
            ((BisocketServerInvoker) invoker).setNumAcceptThreads(managerFactory.configSettings().getNumAcceptThreads());
        }

        @Override
        public void addListener(InvokerCallbackHandler callbackHandler) {
            // Called for every client connecting to the server
            ServerInvokerCallbackHandler handler = (ServerInvokerCallbackHandler) callbackHandler;
            try {
                String sessionId = handler.getClientSessionId();
                managerFactory.sessionManager().createSession(sessionId, callbackHandler);
            } catch (Throwable ex) {
            }
        }

        @Override
        public Object invoke(final InvocationRequest invocation) throws Throwable {
            // Called for every client connecting to the server (after add Listener)
            String sessionId = invocation.getSessionId();
            Map map = invocation.getRequestPayload();
            String host;
            if (map != null) {
                InetAddress clientAddress = (InetAddress) invocation.getRequestPayload().get(Remoting.CLIENT_ADDRESS);
                host = clientAddress.getHostAddress();
            } else {
                host = "localhost";
            }
            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
            } else {
                session.get().setHost(host);
            }
            return null;
        }

        @Override
        public void removeListener(InvokerCallbackHandler callbackHandler) {
            ServerInvokerCallbackHandler handler = (ServerInvokerCallbackHandler) callbackHandler;
            String sessionId = handler.getClientSessionId();
            managerFactory.sessionManager().disconnect(sessionId, DisconnectReason.Disconnected);
        }

    }

    private static Class<?> loadPlugin(Plugin plugin) {
        try {
            classLoader.addURL(new File(pluginFolder, plugin.getJar()).toURI().toURL());
            return Class.forName(plugin.getClassName(), true, classLoader);
        } catch (ClassNotFoundException ex) {
        } catch (MalformedURLException ex) {
        }
        return null;
    }

    private static MatchType loadGameType(GamePlugin plugin) {
        try {
            classLoader.addURL(new File(pluginFolder, plugin.getJar()).toURI().toURL());
            return (MatchType) Class.forName(plugin.getTypeName(), true, classLoader).getConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
        } catch (Exception ex) {
        }
        return null;
    }

    private static void deleteSavedGames() {
        File directory = new File("saved/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File[] files = directory.listFiles(
                (dir, name) -> name.endsWith(".game")
        );
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static MageVersion getVersion() {
        return version;
    }

    public static boolean isTestMode() {
        return testMode;
    }
}
