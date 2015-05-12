using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Globalization;
using file;
using Hamster;
using Q.Amazon;
using ASCII = System.Text.ASCIIEncoding;
using Q.Simulator;
using Q.Util;
using systemdb.metadata;
using util;

namespace Q.Trading.Results {
    public class STOMetricResults : Util.Objects, IList {
        static readonly byte[] METRICS_KEY = BitConverter.GetBytes(0);
        static readonly byte[] PARAMS_KEY = BitConverter.GetBytes(0);
        static readonly byte[] RUNS_KEY = BitConverter.GetBytes(-1);
        readonly Database metricsDb = new Database();
        readonly Database parametersDb = new Database();
        readonly List<int> runs_;
        readonly List<string> metricNames_;
        private readonly object theSyncRoot = new object();
        readonly TypeDescriptionProvider provider;
        readonly LazyDictionary<int, RunMetrics>cache;
        readonly List<string> parameterNames_;
        List<int> sortOrder;
        readonly Dictionary<int, int> lookup;

        public STOMetricResults(Path hamsterMetrics, Path hamsterParams) {
            info("starting read from hamster db " + hamsterMetrics);
            metricsDb.Open(hamsterMetrics.path());
            parametersDb.Open(hamsterParams.path());
            runs_ = runsFromFile(metricsDb);
            lookup = runLookup(runs_);
            sortOrder = sort(runs_);
            metricNames_ = metricsFromFile(metricsDb);
            parameterNames_ = parameterNamesFromFile(parametersDb);
            provider = new STOMetricResultsTypeDescriptionProvider(new STOMetricResultsTypeDescriptor(parameterNames_, metricNames_));
            cache = new LazyDictionary<int, RunMetrics>(emptyMetrics);
            CollectionChanged += doNothing;
        }

        Dictionary<int, int> runLookup(ICollection<int> runs) {
            var result = new Dictionary<int, int>(runs.Count);
            eachIt(this.runs(), (i, run) => result[run] = i);
            return result;
        }

        static List<string> parameterNamesFromFile(Database db) {
            return (List<string>) deserialize(db.Find(PARAMS_KEY));
        }

        static List<int> runsFromFile(Database db) {
            return (List<int>) deserialize(db.Find(RUNS_KEY));
        }

        static List<string> metricsFromFile(Database db) {
            return (List<string>) deserialize(db.Find(METRICS_KEY));
        }

        #region Ugly TypeDescriptor Stuff
        class STOMetricResultsTypeDescriptor : CustomTypeDescriptor{
            readonly PropertyDescriptorCollection properties;

            public STOMetricResultsTypeDescriptor(IEnumerable<string> parameterNames, IEnumerable<string> metricNames) : base(TypeDescriptor.GetProvider(typeof(RunMetrics)).GetTypeDescriptor(typeof(RunMetrics))) {
                var descriptors = list<PropertyDescriptor>(new RunPropertyDescriptor());
                descriptors.AddRange(convert<string, PropertyDescriptor>(parameterNames, parameter => new ParameterPropertyDescriptor(parameter)));
                descriptors.AddRange(convert<string, PropertyDescriptor>(metricNames, metric => new MetricPropertyDescriptor(metric)));
                properties = new PropertyDescriptorCollection(descriptors.ToArray());
            }
            
            public override PropertyDescriptorCollection GetProperties() { return properties; }
            public override PropertyDescriptorCollection GetProperties(Attribute[] attributes) { return properties; }

            class MetricPropertyDescriptor : PropertyDescriptor {
                readonly string metric;

                public MetricPropertyDescriptor(string metric) : base(metric, new Attribute[0]) {
                    this.metric = metric;
                }         
                public override bool CanResetValue(object component) { return false; }
                public override object GetValue(object component) {
                    var runMetrics = component as RunMetrics;
                    if(runMetrics == null) throw Bomb.toss("MysteryPropertyDescriptor only supports RunMetrics, not " + component.GetType().FullName);
                    var value = runMetrics.metric(metric);
                    return double.IsNaN(value) ? " " : prettyNumber(value);
                }
                public override void ResetValue(object component) { throw new NotImplementedException(); }
                public override void SetValue(object component, object value) { throw new NotImplementedException(); }
                public override bool ShouldSerializeValue(object component) { return true; }
                public override Type ComponentType { get { return typeof(RunMetrics); } }
                public override bool IsReadOnly { get { return true; } }
                public override Type PropertyType { get { return typeof(string); } }
            }
            class ParameterPropertyDescriptor : PropertyDescriptor {
                readonly string parameter;

                public ParameterPropertyDescriptor(string parameter) : base(parameter, new Attribute[0]) {
                    this.parameter = parameter;
                }         
                public override bool CanResetValue(object component) { return false; }
                public override object GetValue(object component) {
                    var runMetrics = component as RunMetrics;
                    if(runMetrics == null) throw Bomb.toss("MysteryPropertyDescriptor only supports RunMetrics, not " + component.GetType().FullName);
                    return runMetrics.parameter(parameter);
                }
                public override void ResetValue(object component) { throw new NotImplementedException(); }
                public override void SetValue(object component, object value) { throw new NotImplementedException(); }
                public override bool ShouldSerializeValue(object component) { return true; }
                public override Type ComponentType { get { return typeof(RunMetrics); } }
                public override bool IsReadOnly { get { return true; } }
                public override Type PropertyType { get { return typeof(string); } }
            }
            class RunPropertyDescriptor : PropertyDescriptor {
                public RunPropertyDescriptor() : base("run", new Attribute[0]) {}
                public override bool CanResetValue(object component) { return false; }
                public override object GetValue(object component) {
                    var runMetrics = component as RunMetrics;
                    if(runMetrics == null) throw Bomb.toss("MysteryPropertyDescriptor only supports RunMetrics, not " + component.GetType().FullName);
                    return runMetrics.run();
                }
                public override void ResetValue(object component) { throw new NotImplementedException(); }
                public override void SetValue(object component, object value) { throw new NotImplementedException(); }
                public override bool ShouldSerializeValue(object component) { return true; }
                public override Type ComponentType { get { return typeof(RunMetrics); } }
                public override bool IsReadOnly { get { return true; } }
                public override Type PropertyType { get { return typeof(int); } }
            }

        }

        class STOMetricResultsTypeDescriptionProvider : TypeDescriptionProvider {
            readonly ICustomTypeDescriptor typeDescriptor;

            public STOMetricResultsTypeDescriptionProvider(ICustomTypeDescriptor typeDescriptor) {
                this.typeDescriptor = typeDescriptor;
            }

            public override ICustomTypeDescriptor GetTypeDescriptor(Type objectType, object instance) {
                var obj = instance as RunMetrics;
                if(obj == null) throw Bomb.toss("MysteryTypeDescriptionProvider only supports RunMetrics, not " + instance.GetType().FullName);
                return typeDescriptor;
            }
        }
#endregion

        public RunMetrics emptyMetrics(int run) {
            return new RunMetrics(run, provider);
        }

        public void populateValues(RunMetrics result) {
            var bytes = metricsDb.Find(runKey(result.run()));
            var doubles = new double[bytes.Length / 8];
            Buffer.BlockCopy(bytes, 0, doubles, 0, bytes.Length);
            var runMetrics = new Dictionary<string, double>();
            eachIt(metricNames_, (i, name) => runMetrics[name] = doubles[i]);

            var paramBytes = parametersDb.Find(runKey(result.run()));
            var strings = (List<string>) deserialize(paramBytes);
            var runParameters = new Dictionary<string, string>();
            eachIt(parameterNames_, (i, name) => runParameters[name] = strings[i]);
            result.setValues(runMetrics, runParameters);
        }

        public List<int> runs() {
            return runs_;
        }

        public IEnumerable<string> metricNames() {
            return metricNames_;
        }

        static byte[] runKey(int run) {
            return BitConverter.GetBytes(run);
        }
        public static QFile hamsterParametersFile(int id, QFile paramsFile) {
            var result = new QDirectory(@"C:\tempMetricFiles\hamster").file(new [] {"" + id, "Parameters", paramsFile.name().Replace(".csv", ".ham")});
            result.ensurePath();
            return result;
        }

        public void clearCache(RunMetrics metrics) {
            cache.remove(metrics.run());
        }

        public object this[int index] {
            get { return cache.get(runs()[index]); }
            set { throw new NotImplementedException(); }
        }
        public int Count { get { return runs().Count; } }

#region Fake IList Implementation
        public IEnumerator GetEnumerator() { LogC.info("GetEnumerator called"); foreach(var run in runs()) { LogC.info("yielding next"); yield return cache.get(run);}}
        public object SyncRoot { get { return theSyncRoot; } }
        public bool IsReadOnly { get { return true; } }
        public bool IsFixedSize { get { return true; } }
        public int IndexOf(object value) { return -1; }
        public void CopyTo(Array array, int index) {
            eachIt(runs(), (i, run) => array.SetValue(this[i], index + i));
        }
        public bool IsSynchronized { get { throw new NotImplementedException(); } }
        public int Add(object value) { throw new NotImplementedException(); }
        public bool Contains(object value) { throw new NotImplementedException(); }
#endregion
#region ICollectionView
        public void Refresh() {
            LogC.info("Refresh() called");
        }

        public IDisposable DeferRefresh() {
            LogC.info("DeferRefresh() called");
            return new Deferer(this);
        }

        public bool MoveCurrentToFirst() {
            LogC.info("MoveCurrentToFirst() called");
            CurrentPosition = 1;
            return true;
        }

        public bool MoveCurrentToLast() {
            LogC.info("MoveCurrentToLast() called");
            CurrentPosition = runs().Count;
            return true;
        }

        public bool MoveCurrentToNext() {
            LogC.info("MoveCurrentToNext() called");
            CurrentPosition++;
            return CurrentPosition <= runs().Count;
        }

        public bool MoveCurrentToPrevious() {
            LogC.info("MoveCurrentToPrevious() called");
            CurrentPosition--;
            return CurrentPosition > 0;
        }

        public bool MoveCurrentTo(object item) {
            var runMetrics = (RunMetrics) item;
            LogC.info("MoveCurrentToPrevious(" + runMetrics.run() + ") called");
            var index = runs().IndexOf(runMetrics.run());
            if(index < 0) return false;
            CurrentPosition = index + 1;
            return true;
        }

        public bool MoveCurrentToPosition(int position) {
            LogC.info("MoveCurrentToPrevious(" + position + ") called");
            CurrentPosition = position;
            return position > 0 && position <= runs().Count;
        }

        public CultureInfo Culture {
            get { return CultureInfo.CurrentCulture; }
            set {  throw new NotImplementedException();}
        }
        public IEnumerable SourceCollection {
            get {
            LogC.info("SourceCollection called");
                return this;
            }
        }
        public Predicate<object> Filter {
            get; set;
        }
        public bool CanFilter {
            get { return false; }
        }
        public SortDescriptionCollection SortDescriptions {
            get { return new SortDescriptionCollection(); }
        }
        public bool CanSort {
            get { return true; }
        }
        public bool CanGroup {
            get { return false; }
        }
        public ObservableCollection<GroupDescription> GroupDescriptions {
            get { return new ObservableCollection<GroupDescription>(); }
        }
        public ReadOnlyObservableCollection<object> Groups {
            get { return new ReadOnlyObservableCollection<object>(new ObservableCollection<object>()); }
        }
        public bool IsEmpty {
            get {
                LogC.info("IsEmpty() called (answer = " + isEmpty(runs()) + ")");
                return isEmpty(runs());
            }
        }
        public object CurrentItem {
            get {
                LogC.info("CurrentItem called (current position = " + CurrentPosition + ")");
                return this[lookup[sortOrder[CurrentPosition - 1]]];
            }
        }
        public int CurrentPosition { get; private set; }
        public bool IsCurrentAfterLast {
            get {
                LogC.info("IsCurrentAfterLast() called (answer = " + (CurrentPosition > runs().Count) + ")");
                return CurrentPosition > runs().Count;
            }
        }
        public bool IsCurrentBeforeFirst {
            get {
                LogC.info("IsCurrentBeforeFirst() called (answer = " + (CurrentPosition < 1) + ")");
                return CurrentPosition < 1;
            }
        }
        //public event CurrentChangingEventHandler CurrentChanging;
        //public event EventHandler CurrentChanged;
        public void Clear() { throw new NotImplementedException(); } 
        public void Insert(int index, object value) { throw new NotImplementedException(); }
        public void Remove(object value) { throw new NotImplementedException(); }
        public void RemoveAt(int index) { throw new NotImplementedException(); }
#endregion


#region hamster file creation stuff
        public static void Main(string[] args) {
            var arguments = Arguments.arguments(args, jStrings("id", "addParametersOnly"));
            var id = arguments.integer("id");
            var details = SystemDetailsTable.DETAILS.details(id);
            var sto = new sto.STO(details);
            hamsterfyParametersFile(id, sto.paramsFile());
            if (arguments.get("addParametersOnly", false)) return;
            List<Symbol> symbols;
            List<Portfolio> portfolios;
            STO.populateSymbolsPortfolios(details, out symbols, out portfolios);
            hamsterfyMetricsFile(id, sto.metricFile("ALL"));
            each(symbols, s => hamsterfyMetricsFile(id, sto.metricFile(s.name)));
            each(portfolios, p => hamsterfyMetricsFile(id, sto.metricFile(p.name)));
        }
        static QFile hamsterfyMetricsFile(int id, QFile metricsFile) {
            var hamster = hamsterMetricsFile(id, metricsFile);
            info("starting write to hamster db: " + hamster);
            var db = new Database();
            db.Create(hamster.path());
            var metricCsv = new CsvStreamer(metricsFile, true);
            var fileMetricNames = rest(list<string>(metricCsv.header()));
            var runs = new List<int>();
            eachUntilNull(() => metricCsv.next(), jRecord => {
                var record = list<string>(jRecord);
                var run = int.Parse(first(record));
                runs.Add(run);
                var doubles = array(convert(rest(record), s => STOMetricsWriter.fromMetricCsv(s)));
                var bytes = new byte[doubles.Length * 8];
                Buffer.BlockCopy(doubles, 0, bytes, 0, bytes.Length);
                db.Insert(runKey(run), bytes);
            });
            db.Insert(METRICS_KEY, serialize(list(fileMetricNames)));
            db.Insert(RUNS_KEY, serialize(runs));
            insertMetricSortedRuns(db);
            db.Close();
            info("done hamster write");
            return hamster;
        }

        static void insertMetricSortedRuns(Database db) {
            var metrics = metricsFromFile(db);
            var runs = runsFromFile(db);
            var dubble = new double[1];
            eachIt(metrics, (i, metric) => {
                var sList = new List<KeyValuePair<int, double>>(runs.Count);
                each(runs, run => {
                    var bytes = db.Find(runKey(run));
                    Buffer.BlockCopy(bytes, 8 * i, dubble, 0, 8);
                    sList.Add(new KeyValuePair<int, double>(run, dubble[0]));
                });
                sList.Sort((a, b) => a.Value.CompareTo(b.Value));
                var sortedRuns = list(convert(sList, kv => kv.Key));
                db.Insert(sortedRunsKey(metric), serialize((sortedRuns)));
                LogC.info("wrote sorted runs for " + metric);
            });
        }

        static void hamsterfyParametersFile(int id, QFile paramsFile) {
            var hamster = hamsterParametersFile(id, paramsFile);
            info("starting write to hamster db: " + hamster);
            var db = new Database();
            db.Create(hamster.path());
            var paramCsv = new CsvStreamer(paramsFile, true);
            var fileParamNames = rest(list<string>(paramCsv.header()));
            var runs = new List<int>();
            eachUntilNull(() => paramCsv.next(), jRecord => {
                var record = list<string>(jRecord);
                var run = int.Parse(first(record));
                runs.Add(run);
                db.Insert(runKey(run), serialize(list(rest(record))));
            });
            db.Insert(PARAMS_KEY, serialize(list(fileParamNames)));
            db.Insert(RUNS_KEY, serialize(runs));
            insertParameterSortedRuns(db);
            db.Close();
            info("done hamster write");
        }

        static void insertParameterSortedRuns(Database db) {
            var parameters = parameterNamesFromFile(db);
            var runs = runsFromFile(db);
            eachIt(parameters, (i, parameter) => {
                var sList = new List<KeyValuePair<int, string>>(runs.Count);
                each(runs, run => {
                    var bytes = db.Find(runKey(run));
                    var values = (List<string>)deserialize(bytes);
                    sList.Add(new KeyValuePair<int, string>(run, values[i]));
                });
                sList.Sort((a, b) => a.Value.CompareTo(b.Value));
                var sortedRuns = list(convert(sList, kv => kv.Key));
                db.Insert(sortedRunsKey(parameter), serialize((sortedRuns)));
            });
        }
#endregion

        static byte[] sortedRunsKey(string key) {
            var encoder = new ASCII();
            return encoder.GetBytes(key + "0000");
        }
        public static QFile hamsterMetricsFile(int id, QFile metricsFile) {
            var result = new QDirectory(@"C:\tempMetricFiles\hamster").file(new [] {"" + id, "Metrics", metricsFile.name().Replace(".csv", ".ham")});
            result.ensurePath();
            return result;
        }

        public List<int> runsByMetric(string metricName) {
            return runsByKey(metricsDb, metricName);
        }

        public List<int> runsByParameter(string parameterName) {
            return runsByKey(parametersDb, parameterName);
        }

        static List<int> runsByKey(Database db, string key) {
            try {
                return (List<int>) deserialize(db.Find(sortedRunsKey(key)));
            } catch (Exception e) {
                throw Bomb.toss("can't find key " + key + " length " + sortedRunsKey(key).GetLength(0), e);
            }
        }
        #region Implementation of INotifyCollectionChanged
        public event NotifyCollectionChangedEventHandler CollectionChanged;
        #endregion
        public void sortBy(string name) {
            if(name.Equals("run")) sortOrder = sort(runs());
            sortOrder = parameterNames_.Contains(name) ? runsByParameter(name) : runsByMetric(name);
            CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
        }

        public void setDeferRefresh() {}

        public void endDeferRefresh() {}
    }

    public class Deferer : IDisposable {
        readonly STOMetricResults results;

        public Deferer(STOMetricResults results) {
            this.results = results;
            results.setDeferRefresh();
        }
        #region Implementation of IDisposable
        public void Dispose() {
            results.endDeferRefresh();
        }
        #endregion
    }
}