package server.utility;

/**
 * Represents the types of messages that can be sent between threads or components
 * in the server system.
 * <p>
 * This enum is typically used with messaging mechanisms to categorize the intent
 * or purpose of a message.
 */
public enum MessageType {

    /**
     * Indicates that a connection has been established or requested.
     */
    CONNECT,

    /**
     * Indicates that a disconnection has occurred or has been requested.
     */
    DISCONNECT,

    /**
     * Represents an error state or conveys information about a failure.
     */
    ERROR
}

