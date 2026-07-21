package com.substring.chat.chat_app_backend.exceptions;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Exception handler for WebSocket {@code @MessageMapping} endpoints.
 *
 * <p>Unlike REST endpoints, WebSocket errors cannot return HTTP status codes.
 * Instead, this handler sends error messages to the individual user's error
 * queue ({@code /user/queue/errors}), which the frontend can subscribe to
 * for real-time error notifications.</p>
 */
@ControllerAdvice
public class WebSocketExceptionHandler {

    /**
     * Catches any exception thrown from a {@code @MessageMapping} handler and
     * sends the error message to the user's personal error queue.
     *
     * @param ex the exception that was thrown
     * @return the error message string sent to {@code /user/queue/errors}
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleWebSocketException(Exception ex) {
        return ex.getMessage();
    }
}
