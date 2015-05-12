using System;
using System.Collections.Generic;
using Q.Recon;
using Q.Trading;

namespace Q.Research {
    internal class FakeResearchGUI : FakeGUI, ResearchGUI {
        List<string> markets_ = new List<string>();
        Parameters parameters_;
        internal readonly Researcher researcher;
        internal bool runButtonEnabled = true;
        internal bool runButtonWasDisabled;
        DateTime? startDate_;
        DateTime? endDate_;
        string name_;
        string systemId_;
        bool runNumberEnabled_;
        string runNumber_;
        string slippageCalculator_ = "";
        bool runInNativeCurrency_;

        public FakeResearchGUI() {
            researcher = new Researcher(this);
        }

        public void setMarkets(IEnumerable<string> newMarkets) {
            markets_ = list(newMarkets);
        }

        public void setParameters(Parameters newParameters) {
            parameters_ = newParameters;
        }


        public void runSystem() {
            researcher.run(false);
        }

        public bool runComplete() {
            return researcher.runComplete();
        }

        public void disableRunButton() {
            runButtonEnabled = false;
            runButtonWasDisabled = true;
        }

        public void enableRunButton() {
            runButtonEnabled = true;
        }

        public List<string> markets() {
            return copy(markets_);
        }

        public string slippageCalculator() {
            return slippageCalculator_;
        }

        public Parameters parameters() {
            return parameters_;
        }

        public void setStartDate(DateTime? start) {
            startDate_ = start;
        }
        public void setEndDate(DateTime? end) {
            endDate_ = end;
        }

        public string systemId() {
            return systemId_;
        }

        public string runNumber() {
            return runNumber_;
        }

        public void setRunNumberEnabled(bool enable) {
            runNumberEnabled_ = enable;
        }

        public DateTime? startDate() {
            return startDate_;
        }
        public DateTime? endDate() {
            return endDate_;
        }

        public void reportResults(Researcher r) {
        }

        public void loadSettings() {
            Researcher.load(this, false);
        }

        public void saveSettings() {
            Researcher.save(this);
        }

        public string name() {
            return name_;
        }

        public void setName(string newName) {
            name_ = newName;
        }

        public bool Equals(FakeResearchGUI other) {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return Equals(other.name_, name_) && other.endDate_.Equals(endDate_) && other.startDate_.Equals(startDate_) && Equals(other.parameters_, parameters_) && listEquals(other.markets_, markets_) && other.runInNativeCurrency_.Equals(runInNativeCurrency_);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (FakeResearchGUI) && Equals((FakeResearchGUI) obj);
        }

        public override int GetHashCode() {
            unchecked {
                var result = (name_ != null ? name_.GetHashCode() : 0);
                result = (result * 397)^(endDate_.HasValue ? endDate_.Value.GetHashCode() : 0);
                result = (result * 397)^(startDate_.HasValue ? startDate_.Value.GetHashCode() : 0);
                result = (result * 397)^(parameters_ != null ? parameters_.GetHashCode() : 0);
                result = (result * 397)^(markets_ != null ? markets_.GetHashCode() : 0);
                result = (result * 397)^runInNativeCurrency_.GetHashCode();
                return result;
            }
        }

        public override string ToString() {
            return string.Format("Name_: {0}, EndDate_: {1}, StartDate_: {2}, Parameters_: {3}, Markets_: {4},NativeCcy_:{5}", name_, endDate_, startDate_, parameters_, markets_, runInNativeCurrency_);
        }

        public void setSystemId(string systemId) {
            systemId_ = systemId;
        }

        public void loadSystem() {
            Researcher.loadSystem(this);
        }

        public bool runNumberEnabled() {
            return runNumberEnabled_;
        }

        public void setRunNumber(string number) {
            runNumber_ = number;
        }

        public void setSlippageCalculator(string newName) {
            slippageCalculator_ = newName;
        }

        public void setRunInNativeCurrency(bool runInNativeCurrency) {
            runInNativeCurrency_ = runInNativeCurrency;
        }

        public bool runInNativeCurrency() {
            return runInNativeCurrency_;
        }
    }
}