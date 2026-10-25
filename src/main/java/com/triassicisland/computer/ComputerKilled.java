package com.triassicisland.computer;

/** Thrown inside a script's thread to stop it (shutdown, reboot, power loss, block removed). */
final class ComputerKilled extends RuntimeException {
    ComputerKilled() {
        super("computer stopped", null, false, false);
    }
}
