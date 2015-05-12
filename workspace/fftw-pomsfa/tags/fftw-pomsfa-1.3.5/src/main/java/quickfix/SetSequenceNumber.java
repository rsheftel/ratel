package quickfix;

import java.io.IOException;

public class SetSequenceNumber
{

    /**
     * @param args
     */
    public static void main (String[] args) throws Exception
    {
        // TODO Auto-generated method stub
        SessionID rediSessionId = new SessionID("FIX.4.2", "MALBECRPT", "REDIRPT");
        FileStoreSetter rediFss = new FileStoreSetter("C:/temp/mike/", rediSessionId, false);
        
        rediFss.setNextSenderMsgSeqNum(770); // left 
        rediFss.setNextTargetMsgSeqNum(889); // right
        
        SessionID ppSessionId = new SessionID("FIX.4.2", "MALBEC-PPT", "MSDW-PPT");
        FileStoreSetter ppFss = new FileStoreSetter("C:/temp/mike/", ppSessionId, false);
        
        ppFss.setNextSenderMsgSeqNum(1095);
        ppFss.setNextTargetMsgSeqNum(1095);
        
        //
        SessionID tdSessionId = new SessionID("FIX.4.2", "FFTW", "TRAD");
        FileStoreSetter tdFss = new FileStoreSetter("C:/temp/mike/", tdSessionId, false);
        
        tdFss.setNextSenderMsgSeqNum(13424); // left
        tdFss.setNextTargetMsgSeqNum(165); // right
        
        
    }

    private static class FileStoreSetter extends FileStore {
        public FileStoreSetter(String path, SessionID sessionID, boolean syncWrites) throws IOException {
            super(path, sessionID, syncWrites);
        }
    }
}
