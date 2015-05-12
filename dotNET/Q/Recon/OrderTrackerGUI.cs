using System.Collections.Generic;
using System.Data;

namespace Q.Recon {        
    
    public interface OrderTrackerGUI: QGUI {
        void setSystemChoices(IEnumerable<string> sivs);
        bool sivSelected();
        string siv();
        void setOrderTable(DataTable table);
        void setMarketChoices(List<string> markets);
        void setPvChoices(List<string> pvs);
        string pv();
        bool pvSelected();
        string market();
        string filter();
        void setStatus(DataRow row, OrderStatus newStatus);
        void addContextMenu(DataRow row);
        void removeContextMenu(DataRow row);
    }

    public enum OrderStatus {
        NO_ACTION_REQUIRED, ACTION_REQUIRED, FAILED, SIM_MISMATCH,
        NOT_FERRET, STAGE
    }
}