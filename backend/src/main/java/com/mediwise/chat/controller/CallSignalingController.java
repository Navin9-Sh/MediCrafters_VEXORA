package com.mediwise.chat.controller;

import com.mediwise.chat.dto.CallSignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebRTC Signaling Controller over WebSocket STOMP.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * THIS CONTROLLER DOES NOT HANDLE AUDIO OR VIDEO.
 * It only routes tiny JSON messages (SDP, ICE candidates) between two
 * clients until they can establish a direct peer-to-peer connection.
 * Once connected, all media flows P2P — this server is no longer involved.
 * ─────────────────────────────────────────────────────────────────────────
 *
 * STOMP Destination Pattern:
 * Client sends to: /app/call/{signal-type}
 * Server routes to: /user/{recipientId}/queue/call
 *
 * WHY /user/{id}/queue/call (not /topic/...)?
 * /topic/* is public — anyone subscribed receives the message.
 * /user/{id}/queue/* is private — ONLY that specific user receives it.
 * Call signals must be private; you cannot broadcast your SDP offer
 * to everyone subscribed to that room.
 *
 * INTERVIEW QUESTION: "What does SimpMessagingTemplate.convertAndSendToUser()
 * do?"
 * It routes to /user/{username}/queue/{destination} where username is
 * the Principal's name (userId in our case). Spring's UserDestinationResolver
 * maps this to the WebSocket session(s) for that specific user.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CallSignalingController {

    /**
     * SimpMessagingTemplate is Spring's WebSocket message sender.
     * It lets us push messages to specific users or topics from server-side code.
     */
    private final SimpMessagingTemplate messagingTemplate;

    // ─────────────────────────────────────────────────────────────────────
    // ① CALL_INITIATE — Caller alerts callee that a call is incoming
    //
    // Android caller sends: /app/call/initiate
    // Spring routes to: /user/{recipientId}/queue/call
    //
    // On Android (callee side), the app shows "Incoming call from Dr. Smith…"
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/initiate")
    public void initiateCall(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName()); // always set server-side (cannot be spoofed)
        message.setType(CallSignalMessage.SignalType.CALL_INITIATE);

        log.info("CALL_INITIATE | room={} | from={} → to={}",
                message.getRoomId(), caller.getName(), message.getRecipientId());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ② CALL_OFFER — Caller sends their SDP offer
    //
    // What is an SDP offer?
    // SDP = Session Description Protocol. It's a text document describing
    // the caller's audio/video codecs, formats, and network preferences.
    // Example (simplified): "I support opus audio, H264 video, at these IPs"
    //
    // The callee uses this to understand what the caller can do, then
    // responds with a CALL_ANSWER selecting compatible options.
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/offer")
    public void sendOffer(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.CALL_OFFER);

        log.debug("CALL_OFFER | room={} | from={} → to={}",
                message.getRoomId(), caller.getName(), message.getRecipientId());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ③ CALL_ANSWER — Callee sends their SDP answer back
    //
    // After receiving the offer, the callee creates an RTCPeerConnection,
    // sets the remote description (the offer), creates its own answer,
    // sets that as local description, then sends it here.
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/answer")
    public void sendAnswer(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.CALL_ANSWER);

        log.debug("CALL_ANSWER | room={} | from={} → to={}",
                message.getRoomId(), caller.getName(), message.getRecipientId());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ④ ICE_CANDIDATE — Network path discovery (sent multiple times each way)
    //
    // What is ICE?
    // ICE = Interactive Connectivity Establishment. WebRTC gathers multiple
    // possible network paths ("candidates"):
    // - host: your local LAN IP (192.168.x.x)
    // - srflx: your public IP discovered via STUN
    // - relay: a TURN server (fallback)
    //
    // Both sides exchange ALL their candidates. WebRTC tries each candidate
    // pair to find the best direct route. This is called "ICE trickle" —
    // candidates arrive and are tried in parallel as they're discovered.
    //
    // This endpoint is called MANY TIMES per call (typically 3–10 candidates per
    // side).
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/ice-candidate")
    public void sendIceCandidate(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.ICE_CANDIDATE);

        // ICE candidates are frequent — use debug level to avoid log spam
        log.debug("ICE_CANDIDATE | room={} | from={} → to={}",
                message.getRoomId(), caller.getName(), message.getRecipientId());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ⑤ CALL_HANGUP — Either party ends the call
    //
    // The recipient should:
    // 1. Close their RTCPeerConnection
    // 2. Stop all MediaStream tracks (camera/mic)
    // 3. Navigate away from the call screen
    //
    // The server does NOT need to do anything here — no state to update.
    // The PATCH /appointments/{id}/complete endpoint (Step 5) handles the
    // database state transition separately.
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/hangup")
    public void hangup(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.CALL_HANGUP);

        log.info("CALL_HANGUP | room={} | initiator={}", message.getRoomId(), caller.getName());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ⑥ CALL_REJECTED — Callee rejects the incoming call
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/reject")
    public void reject(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.CALL_REJECTED);

        log.info("CALL_REJECTED | room={} | rejected by={}", message.getRoomId(), caller.getName());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ⑦ CALL_RENEGOTIATE — Re-offer for mid-call changes
    // Used when a participant toggles screen sharing or changes video quality.
    // Triggers a new offer/answer cycle without ending the call.
    // ─────────────────────────────────────────────────────────────────────
    @MessageMapping("/call/renegotiate")
    public void renegotiate(@Payload CallSignalMessage message, Principal caller) {
        message.setSenderId(caller.getName());
        message.setType(CallSignalMessage.SignalType.CALL_RENEGOTIATE);

        log.debug("CALL_RENEGOTIATE | room={} | from={}", message.getRoomId(), caller.getName());

        sendToRecipient(message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helper — routes message to a specific user's private queue
    //
    // convertAndSendToUser(userId, destination, payload) sends to:
    // /user/{userId}/queue/call
    //
    // Spring's UserDestinationResolver resolves this to the WebSocket
    // session(s) currently open for that userId (Principal name).
    // ─────────────────────────────────────────────────────────────────────
    private void sendToRecipient(CallSignalMessage message) {
        if (message.getRecipientId() == null || message.getRecipientId().isBlank()) {
            log.warn("Signal {} has no recipientId — dropping", message.getType());
            return;
        }
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId(), // target user's Principal name (their userId)
                "/queue/call", // their private STOMP destination
                message // the signal payload
        );
    }
}
