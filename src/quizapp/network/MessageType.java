package quizapp.network;

/**
 * Types of messages exchanged between client and server.
 */
public enum MessageType {
    // Connection messages
    JOIN,
    JOIN_SUCCESS,
    JOIN_FAILED,
    LEAVE,
    DISCONNECT,
    
    // Game flow messages
    QUESTION,
    ANSWER,
    TIMER,
    TIME_UP,
    SCORE_UPDATE,
    RESULTS
}