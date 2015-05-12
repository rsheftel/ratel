using System;

namespace Q.Recon {

    public enum YesNoCancel { YES, NO, CANCEL }

    public interface QGUI {
        void alertUser(string message);
        YesNoCancel askUser(string message);
        void runOnGuiThread(Action action);
        void logAndAlert(string s, Exception e);
    }
}