using System;
using System.Collections.Generic;
using Q.Recon;
using Q.Trading;

namespace Q.Research {
    public interface ResearchGUI : QGUI {
        void disableRunButton();
        void enableRunButton();
        List<string> markets();
        string slippageCalculator();
        Parameters parameters();
        DateTime? startDate();
        DateTime? endDate();
        void reportResults(Researcher researcher);
        void loadSettings();
        void saveSettings();
        string name();
        void setMarkets(IEnumerable<string> markets);
        void setParameters(Parameters parameters);
        void setStartDate(DateTime? date);
        void setEndDate(DateTime? date);
        string systemId();
        string runNumber();
        void setRunNumberEnabled(bool enable);
        void setSystemId(string s);
        void setRunNumber(string number);
        void setSlippageCalculator(string newName);
        void setRunInNativeCurrency(bool runInNativeCurrency);
        bool runInNativeCurrency();
    }
}