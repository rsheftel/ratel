using System.Collections.Generic;
using System.ComponentModel;
using Q.Util;

namespace Q.Trading.Results {
    public class RunMetrics : Objects, INotifyPropertyChanged {
        readonly int run_;
        readonly Dictionary<string, double> metricValues;
        readonly Dictionary<string, string> parameterValues;
        
        public event PropertyChangedEventHandler PropertyChanged;

        public RunMetrics(int run, Dictionary<string, double> metricValues, Dictionary<string, string> parameterValues, TypeDescriptionProvider provider) {
            run_ = run;
            this.metricValues = metricValues;
            this.parameterValues = parameterValues;
            TypeDescriptor.AddProvider(provider, this);
            PropertyChanged += doNothing;
        }

        public RunMetrics(int run, TypeDescriptionProvider provider) : this(run, new Dictionary<string, double>(), new Dictionary<string, string>(), provider) {}

        public override string ToString() {
            return "run " + run() + ", values:\n" + toShortString(metricValues);
        }

        public double metric(string name) {
            return metricValues.ContainsKey(name) ? metricValues[name] : double.NaN;
        }

        public void setValues(Dictionary<string, double> newMetricValues, Dictionary<string, string> newParameterValues ) {
            each(newMetricValues, (name, newValue) => {
                                      metricValues[name] = newValue;
                                      PropertyChanged(this, new PropertyChangedEventArgs(name));
                                  });
            each(newParameterValues, (name, newValue) => {
                                         parameterValues[name] = newValue;
                                         PropertyChanged(this, new PropertyChangedEventArgs(name));
                                     });
        }

        public int run() {
            return run_;
        }

        public string parameter(string name) {
            return parameterValues.ContainsKey(name) ? parameterValues[name] : "";
        }
    }
}