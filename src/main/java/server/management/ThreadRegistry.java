package server.management;

import server.utility.ThreadMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an in‐memory registry of all active virtual threads.
 * <p>
 * Singleton: use {@link #getInstance()} to access it from anywhere.
 */
public class ThreadRegistry {
    // Create the one instance of the ThreadRegistry
    private static final ThreadRegistry INSTANCE = new ThreadRegistry();

    /**
     * Private constructor allows for only one instance
     */
    private ThreadRegistry() { }

    /**
     * Public accessor for the singleton
     * @return the instance of the ThreadRegistry
     */
    public static ThreadRegistry getInstance() {
        return INSTANCE;
    }

    // A centralized registry of all threads
    private final ConcurrentHashMap<Thread, BlockingQueue<ThreadMessage>> threadRegistry = new ConcurrentHashMap<>();

    /**
     * Setter method to register a thread and blocking queue combination (player handler) within the thread registry.
     * @param thread The thread the player handler is on.
     * @param queue The blocking queue of the player handler.
     */
    public void register(Thread thread, BlockingQueue<ThreadMessage> queue) {
        threadRegistry.put(thread, queue);
    }

    /**
     * Getter method to obtain the blocking queue matched with a particular thread (Player Handlers blocking queue).
     * @param thread The thread whose blocking queue we are trying to obtain.
     * @return The blocking queue paired with the thread.
     */
    public BlockingQueue<ThreadMessage> getQueue(Thread thread) {
        return threadRegistry.get(thread);
    }

    /**
     * Setter method to remove a thread and blocking queue from the thread registry.
     * @param thread the thread being removed.
     */
    public void unregister(Thread thread) {
        threadRegistry.remove(thread);
    }
}
