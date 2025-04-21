package pietpiper.mcmmod.util;

import net.minecraft.server.MinecraftServer;

/**
 * A globally accessible reference to the running MinecraftServer instance.
 * This should only be accessed after the server has fully started.
 */
public class ServerReference {
    private static MinecraftServer server;
    public static final boolean DEBUG_MODE = true;

    /**
     * Called once during server startup to set the server instance.
     * Should only be used from ServerLifecycleEvents.SERVER_STARTED.
     */
    public static void setServer(MinecraftServer serverInstance) {
        server = serverInstance;
    }

    /**
     * Returns the active MinecraftServer instance, or null if not yet started.
     */
    public static MinecraftServer get() {
        return server;
    }

    /**
     * Utility method to broadcast a message to all players and log to console.
     * Can be used for debugging or messaging in-game.
     */
    public static void broadcast(String message) {
        if (server != null && DEBUG_MODE) {
            server.getPlayerManager().broadcast(
                    net.minecraft.text.Text.literal("[Mcmmod] " + message),
                    false
            );
            System.out.println("[Mcmmod DEBUG] " + message);
        } else {
            System.out.println("[Mcmmod] [WARN] Tried to broadcast before server was ready: " + message);
        }
    }

    /**
     * Sends a debug message only to the console (not players).
     */
    public static void logConsole(String message) {
        if (!DEBUG_MODE || server == null) return;
        System.out.println("[Mcmmod DEBUG] " + message);
    }
}