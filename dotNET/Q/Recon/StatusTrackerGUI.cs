using System;
using System.Data;

namespace Q.Recon {
    public interface StatusTrackerGUI : QGUI{
        void setStatusTable(DataTable table);
        void setHeartbeatStatus(DataRow row, SystemStatus status);
        void setTickStatus(DataRow row, SystemStatus status);
        void launcherAvailable(string host, DateTime staleAt);
    }
}