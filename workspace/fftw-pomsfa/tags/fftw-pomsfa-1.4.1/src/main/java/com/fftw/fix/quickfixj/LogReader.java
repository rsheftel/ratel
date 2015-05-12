package com.fftw.fix.quickfixj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import quickfix.DefaultMessageFactory;
import quickfix.FileStore;
import quickfix.FileStoreFactory;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.SessionID;
import quickfix.SessionSettings;

import com.fftw.task.AbstractObservableTask;

/**
 * Read in a QuickFix/J log.
 * 
 */
public class LogReader extends AbstractObservableTask
{

    private DefaultMessageFactory dmf = new DefaultMessageFactory();

    private static final String[] EXTENSIONS =
    {
        ".body", ".header", ".seqnums", ".session"
    };

    public List<Message> readFixStore (File fixLogFile) throws FileNotFoundException, IOException
    {
        // Determine the log files from the selected file

        FileStore fs = createFileStore(fixLogFile);

        long lastSeqNum = fs.getNextSenderMsgSeqNum();

        setDone(false);
        setCancelled(false);

        List<String> messageStr = new ArrayList<String>(1);
        List<Message> messages = new ArrayList<Message>((int)lastSeqNum);
        for (int i = 1; i < lastSeqNum && !isCancelRequested() && !isCancelled(); i++)
        {
            fs.get(i, i, messageStr);

            String message = messageStr.get(0);
            Message fixMessage = message == null ? null : convertStr2Message(message);
            if (fixMessage != null)
            {
                messages.add(fixMessage);
            }
            messageStr.clear();
            setProgress(i, lastSeqNum);
            setChanged();
            notifyObservers();
        }

        if (isCancelRequested() && !isDone())
        {
            setCancelled(true);
            setCancelled(false);
        }
        setProgress(100);
        setChanged();
        notifyObservers();

        return messages;
    }

    private Message convertStr2Message (String message)
    {
        try
        {
            return MessageUtils.parse(dmf, null, message);
        }
        catch (InvalidMessage e)
        {
        }
        return null;
    }

    public List<Message> readFixLog (File fixLogFile) throws FileNotFoundException, IOException
    {
        long expectedSize = fixLogFile.length();
        long bytesRead = 0;

        setDone(false);
        setCancelled(false);

        List<Message> messages = new LinkedList<Message>();
        
        FileReader fr = new FileReader(fixLogFile);
        BufferedReader br = new BufferedReader(fr);

        while (!isDone() && !isCancelRequested() && !isCancelled())
        {
            String fileLine = br.readLine();

            if (fileLine == null)
            {
                setDone(true);
            }
            else
            {
                bytesRead += fileLine.length();

                String[] messageParts = fileLine.split(" - ");
                
                Message fixMessage = convertStr2Message(messageParts[1]);
                
                messages.add(fixMessage);
                setProgress(bytesRead, expectedSize);
                setChanged();
                notifyObservers();
            }
        }
        
        br.close();

        if (isCancelRequested() && !isDone())
        {
            setCancelled(true);
            setCancelled(false);
        }
        setProgress(100);
        setChanged();
        notifyObservers();
        
        return messages;
    }

    private FileStore createFileStore (File fixLogFile)
    {
        SessionID fileSessionID = extractSessionFileName(fixLogFile);

        if (fileSessionID != null)
        {
            File dir = fixLogFile.getParentFile();
            SessionSettings ss = new SessionSettings();
            ss.setString(FileStoreFactory.SETTING_FILE_STORE_PATH, dir.getAbsolutePath());
            FileStoreFactory fsf = new FileStoreFactory(ss);
            return (FileStore)fsf.create(fileSessionID);
        }
        return null;
    }

    /**
     * return everything before the last 'dot'
     * 
     * The extension should be one of:
     * <ul>
     * <li>body</li>
     * <li>header</li>
     * <li>seqnums</li>
     * <li>session</li>
     * </ul>
     * 
     * @param fixLogFile
     * @return
     */
    private SessionID extractSessionFileName (File fixLogFile)
    {
        String fileName = fixLogFile.getName();
        String sessionStr = null;
        for (String ext : EXTENSIONS)
        {
            String[] tmp = fileName.split(ext);
            if (tmp != null && tmp[0].length() < fileName.length())
            {
                // Found a match
                sessionStr = tmp[0];
                break;
            }
        }

        if (sessionStr != null)
        {
            String[] sessionParts = sessionStr.split("-");
            return new SessionID(sessionParts[0], sessionParts[1], sessionParts[2]);
        }

        return null;
    }

    private void setProgress (long bytesRead, long expectedSize)
    {
        int progress = (int)((float)bytesRead / (float)expectedSize * 100.0);
        setProgress(progress);
    }

    public String getDescription ()
    {
        return "Reading FIX Log File";
    }

}
