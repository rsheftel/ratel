package com.fftw.bloomberg.rtf.types;

/**
 * All the commands that can be executed by the Real-time feed.
 */
public enum RtfCommand {

    Ack(1), Nak(2), Connect(3), Accept(4), Override(5), Status(6), Data(7), Retransmit(8);

    private int commandCode;

    private RtfCommand(int code) {
        this.commandCode = code;
    }

    public int getCommandCode() {
        return this.commandCode;
    }

    public static RtfCommand valueOf(int commandCode) {
        // Use the fact that we are sequential, but start at 1 instead of 0
        return values()[commandCode -1];
    }
}
