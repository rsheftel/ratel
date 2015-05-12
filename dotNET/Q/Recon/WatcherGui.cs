using System.Data;

namespace Q.Recon {
    public interface WatcherGui : QGUI {
        void setStatus(DataRow row, SystemStatus status);
    }
}