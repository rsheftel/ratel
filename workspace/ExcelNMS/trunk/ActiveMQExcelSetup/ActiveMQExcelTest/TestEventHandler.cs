using System;
using Microsoft.Office.Interop.Excel;

namespace ActiveMQExcelTest {
    public class TestEventHandler : IRTDUpdateEvent
    {
        #region IRTDUpdateEvent Members
        public void UpdateNotify()
        {
            Console.WriteLine("UpdateNotify called");
        }

        public void Disconnect()
        {
            Console.WriteLine("Disconnect called");
        }

        public int HeartbeatInterval
        {
            get { return 10; }
            set { }
        }
        #endregion
    }
}