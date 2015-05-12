package com.fftw.bloomberg.rtf.messages;

import org.joda.time.LocalDate;

import java.io.Serializable;

import com.fftw.util.datetime.DateTimeUtils;
import com.fftw.util.strings.FixedWidthFormatter;
import com.fftw.bloomberg.rtf.types.RtfMode;
import com.fftw.bloomberg.rtf.types.RtfCommand;

/**
 * Header record of a Real-time message.
 * <p/>
 * A header record is always 45 bytes long
 */
public class RtfHeader implements Serializable {
    private RtfCommand command; //
    private RtfMode mode;
    private LocalDate date;
    private String filenameExtension; // Used for batch mode only
    private int length; // Length of the entire packet (inclusive of STX and ETX)
    private String filename; // Used for batch mode only

    // Sequence number is used in commands 1-5 and 7-8.  For 6 - Status it is
    // the pricing and site number when sending to Bloomberg
    private int sequenceNumber;

    private int pricingNumber;
    private int siteNumber;

    /**
     * Create an online header.
     *
     * @param command
     * @param date
     * @param sequenceNumber
     */
    public RtfHeader(RtfCommand command, LocalDate date, int sequenceNumber) {
        if (RtfCommand.Status == command) {
            throw new IllegalArgumentException("Status command not valid with sequence number");
        }

        this.command = command;
        this.mode = RtfMode.Online;
        this.date = date;
        this.sequenceNumber = sequenceNumber;

        // unused fields
        this.filenameExtension = "   "; // 3 spaces
        this.filename = "        "; // 8 spaces
    }

    /**
     * Create a heartbeat header
     *
     * @param command
     * @param date
     * @param pricingNumber
     * @param siteNumber
     */
    public RtfHeader(RtfCommand command, LocalDate date, int pricingNumber, int siteNumber) {
        if (RtfCommand.Status != command) {
            throw new IllegalArgumentException("Status command must be specified, "
                    + command + " not valid with pricing and site number");
        }

        this.command = command;
        this.mode = RtfMode.Online;
        this.date = date;
        this.pricingNumber = pricingNumber;
        this.siteNumber = siteNumber;

        // unused fields
        this.filenameExtension = "   "; // 3 spaces
        this.filename = "        "; // 8 spaces
    }

    public void setLength(int length) {
        if (length < 47) {
            throw new IllegalArgumentException("Length can not be less than 47, trying to set to " + length);
        }
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    /**
     * Set the pricingNumber.
     * <p/>
     * This is only valid on Status commands.
     *
     * @param pricingNumber
     * @throws IllegalArgumentException when called on non Status commands.
     */
    public void setPricingNumber(int pricingNumber) {
        if (command != RtfCommand.Status) {
            throw new IllegalArgumentException("Pricing number only valid on 'Status' command.  Called on " + command);
        }

        this.pricingNumber = pricingNumber;
    }

    /**
     * Set the site number.
     * <p/>
     * This is only valid on Status commands.
     *
     * @param siteNumber
     * @throws IllegalArgumentException when called on non Status commands.
     */
    public void setSiteNumber(int siteNumber) {
        if (command != RtfCommand.Status) {
            throw new IllegalArgumentException("Site number only valid on 'Status' command.  Called on " + command);
        }

        this.siteNumber = siteNumber;
    }

    /**
     * Set the pricing and site numbers.
     * <p/>
     * This is only valid for Status commands.
     *
     * @param pricing
     * @param site
     * @throws IllegalArgumentException when the command is not a Status
     */
    public void setPricingAndSiteNumber(int pricing, int site) {
        setPricingNumber(pricing);
        setSiteNumber(site);
    }

    /**
     * Set the sequence number.
     * <p/>
     * This is valid for all commands <i>except</i> Status.
     *
     * @param sequence
     * @throws IllegalArgumentException when called on a Status command
     */
    public void setSequenceNumber(int sequence) {
        if (command == RtfCommand.Status) {
            throw new IllegalArgumentException("Sequence number not valid for 'Status' command.  Called on " + command);
        }

        this.sequenceNumber = sequence;
    }

    /**
     * Return a string that is suitable to be send over the wire
     *
     * @return
     */
    public String protocolString() {
        StringBuilder sb = new StringBuilder(45);
        sb.append(command.getCommandCode());
        sb.append(" "); // 1 reserved byte
        sb.append(mode.ordinal());
        sb.append(DateTimeUtils.formatBBDate(date));
        sb.append(FixedWidthFormatter.formatString(filenameExtension, 3));
        sb.append("   "); // 3 reserved bytes
        sb.append(String.format("%04d", length));
        sb.append(FixedWidthFormatter.formatString(filename, 8));

        if (RtfCommand.Status == command) {
            sb.append(String.format("%05d", pricingNumber));
            sb.append(String.format("%03d", siteNumber));
        } else {
            sb.append(String.format("%08d", sequenceNumber));
        }
        sb.append("00000000"); //  8 reserved bytes -- BB zero fills

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(command).append(", ").append(mode).append(", ");
        sb.append(date);
        sb.append(", length=").append(length);

        if (RtfCommand.Status == command) {
            sb.append(", pricing=").append(pricingNumber);
            sb.append(", site=").append(siteNumber);
        } else {
            sb.append(", sequence=").append(sequenceNumber);
        }

        sb.append(", Filename=").append(filename == null ? "" : filename);
        sb.append(", extension=").append(filenameExtension == null ? "" : filenameExtension);

        return sb.toString();
    }

    public RtfCommand getCommand() {
        return command;
    }

    public RtfMode getMode() {
        return mode;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Give a raw string, create a RtfHeader object from it.
     * <p/>
     * The start-of-text (STX) should not be passed.  The STX should be stripped
     * from the string, as it is used by the communication protocol and not really
     * part of the header record.
     *
     * @param rawMessage
     * @return
     */
    public static RtfHeader valueOf(String rawMessage) {
        // All header records are 45 bytes long, we must have at least that many,
        // and not go over that value

        if (rawMessage.charAt(0) == 0x02) {
            throw new IllegalArgumentException("Message should not start with STX");
        }

        if (45 > rawMessage.length()) {
            throw new IllegalArgumentException("Message length must be at least 45 bytes for a RtfHeader.  Length is "
                    + rawMessage.length());
        }

        // Parse the command
        RtfCommand command = RtfCommand.valueOf(Integer.parseInt(rawMessage.substring(0, 1)));
        RtfMode mode = RtfMode.valueOf(Integer.parseInt(rawMessage.substring(2, 3)));

        LocalDate date = DateTimeUtils.parseBBDate(rawMessage.substring(3, 11));
        String filenameExtension = rawMessage.substring(11, 14); // Not used
        int length = Integer.parseInt(rawMessage.substring(17, 21));
        String filename = rawMessage.substring(21, 29);

        RtfHeader header = null;

        // if the command is a status parse as pricing and site numbers
        if (RtfCommand.Status == command) {
            int pricingNumber = Integer.parseInt(rawMessage.substring(29, 34));
            int siteNumber = Integer.parseInt(rawMessage.substring(34, 37));
            header = new RtfHeader(command, date, pricingNumber, siteNumber);
        } else {
            int sequenceNumber = Integer.parseInt(rawMessage.substring(29, 37));
            header = new RtfHeader(command, date, sequenceNumber);
        }

        header.mode = mode;
        header.length = length;
        header.filenameExtension = filenameExtension.trim();
        header.filename = filename.trim();

        return header;
    }
}
