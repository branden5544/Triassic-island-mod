package com.triassicisland.phone;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Voice for phone calls. Set by the Simple Voice Chat plugin when that mod is installed; null otherwise,
 * so nothing here ever touches voice chat classes directly.
 */
public interface VoiceBridge {
    /** Puts both players in a private voice group. False if either can't be connected. */
    boolean startCall(UUID a, UUID b);

    void endCall(UUID a, UUID b);

    /** Whether this player has the voice chat mod running. */
    boolean hasVoice(UUID player);

    final class Holder {
        @Nullable
        public static volatile VoiceBridge bridge;

        private Holder() {}
    }
}
