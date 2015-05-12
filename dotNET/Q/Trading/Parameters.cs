using System;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Text;
using file;
using Q.Util;
using sto;
using systemdb.metadata;
using O = Q.Util.Objects;
using JDetails = systemdb.metadata.SystemDetailsTable.SystemDetails;
using JDetailsTable = systemdb.metadata.SystemDetailsTable;
using JMap = java.util.Map;

namespace Q.Trading {
    [Serializable]
    public class Parameters : O, IEnumerable<string>, IDeserializationCallback {
        static readonly List<string> TRUES = list("TRUE", "true", "T", "t", "1");
        static readonly List<string> READ_ONLY_PARAM_NAMES = list("RunMode", "RunNumber");
        static readonly JDetailsTable TABLE = new JDetailsTable();
        static readonly Dictionary<string, JMap> paramCache = new Dictionary<string, JMap>();
        static readonly Dictionary<int, JDetails> detailsCache = new Dictionary<int, JDetails>();

        readonly Dictionary<string, string> data = new Dictionary<string, string>();
        [NonSerialized] Lazy<JDetails> details_;
        string systemClassName_;
        bool loaded;

        public Parameters() : this(new Dictionary<string, double>()) {}

        public Parameters(string systemClassName) : this(new Dictionary<string, double>()) {
            systemClassName_ = systemClassName;
        }

        public Parameters(Dictionary<string, double> data) {
            each(data, (s, d) => this.data.Add(s, "" + d));
            details_ = new Lazy<JDetails>(() => details(getDirect<int>("systemId")));
        }

        public static JDetails details(int systemId) {
            if(!detailsCache.ContainsKey(systemId)) detailsCache[systemId] = TABLE.details(systemId);
            return detailsCache[systemId];
        }

        public Parameters(string systemClassName, QFile file) : this(systemClassName) {
            var csv = file.csv(true);
            zeroTo(csv.count(), i => Add(csv.value("name", i), csv.value("value", i)));
        }

        public T get<T>(string name) {
            load();
            requireParameter(name);
            return getDirect<T>(name);
        }

        public T get<T>(string name, T @default) {
            load();
            return has(name) ? get<T>(name) : @default;
        }

        T getDirect<T>(string name) {
            var value = data[name];
            if(typeof(T).Equals(typeof(bool))) value = TRUES.Contains(value) ? "true" : "false";
            return (T) Convert.ChangeType(value, typeof (T));
        }

        void requireParameter(string name) {
            Bomb.unless(
                data.ContainsKey(name),
                () => "parameter " + name + " not found.  parameters available: " + toShortString(data));
        }

        public void load() {
            if(loaded) return;
            loaded = true;
            LogC.verbose(() => "loading parameters");
            var extras = new Dictionary<string, string>();
            if (!has("RunMode") || isRE() || isCloudSTO()) return;
            if (isSTO()) extras = stoParameters();
            else if (isLive()) extras = liveParameters();
            else Bomb.toss("unexpected run mode " + get<int>("RunMode"));

            extras["systemId"] = getDirect<string>("systemId");
            data.Clear();
            eachKey(extras, delegate(String k) { data[k] = extras[k]; });
            LogC.verbose(() => "loaded parameters:\n" + this);
        }

        public bool isCloudSTO() {
            return isRunMode(RunMode.CLOUD_STO);
        }

        bool isRE() {
            return isRunMode(RunMode.RIGHTEDGE);
        }

        bool isLive() {
            return isRunMode(RunMode.LIVE);
        }

        public bool isSTO() {
            return isRunMode(RunMode.STO);
        }

        public void flipToCloudSTO() {
            Bomb.unless(isSTO(), () => "Can only flip from STO to CLOUD_STO");
            load();
            data["RunMode"] = "" + (int) RunMode.CLOUD_STO;
        }

        bool isRunMode(RunMode mode) {
            return has("RunMode") && get<int>("RunMode") == (int) mode;
        }

        public bool has(string s) {
            load();
            return data.ContainsKey(s);
        }

        public string systemClassName() {
            if (systemClassName_ == null) systemClassName_ = details().qClass();
            return systemClassName_;
        }

        JDetailsTable.SystemDetails details() {
            return details_;
        }

        public bool Equals(Parameters obj) {
            load();
            if (ReferenceEquals(null, obj)) return false;
            return ReferenceEquals(this, obj) || dictionaryEquals(obj.data, data);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == typeof (Parameters) && Equals((Parameters) obj);
        }

        public override int GetHashCode() {
            load();
            return data.GetHashCode();
        }

        public void OnDeserialization(object sender) {
            details_ = new Lazy<JDetails>(() => details(getDirect<int>("systemId")));
        }

        Dictionary<string, string> stoParameters() {
            try {
                var result = new Dictionary<string, string>();
                var cacheKey = stoDir() + stoId();
                if(!paramCache.ContainsKey(cacheKey)) {
                    var sto = new STO(stoDir(), stoId());
                    paramCache[cacheKey] = sto.parameters(systemName(), interval(), version());
                }
                var paramMap = (JMap) paramCache[cacheKey].get(new java.lang.Integer(runNumber()));
                foreach (var key in list<string>(paramMap.keySet())) result[key] = paramMap.get(key).ToString();
                result["RunNumber"] = getDirect<string>("RunNumber");
                result["RunMode"] = getDirect<string>("RunMode");
                return result;
            } catch (java.lang.Throwable e) {
                util.Log.err("exception caught in stoParameters()", e);
                throw Bomb.toss("java exception caught: ", e);
            }
        }

        Dictionary<string, string> liveParameters() {
            var result = liveParameters(systemName(), pvName());
            result["RunMode"] = "" + (int) RunMode.LIVE;
            return result;
        }

        public static Dictionary<string, string> liveParameters(string system, string pv) {
            var v = ParameterValuesTable.VALUES;
            var parameters = v.@params(system, new Pv(pv));
            var result = new Dictionary<string, string>();
            foreach (var key in list<string>(parameters.keySet())) result[key] = (string) parameters.get(key);
            return result;
        }

        public string systemName() {
            return details().@string(TABLE.C_SYSTEM_NAME);
        }

        string version() {
            return details().@string(TABLE.C_VERSION);
        }

        string interval() {
            return details().@string(TABLE.C_INTERVAL);
        }

        string stoDir() {
            return details().@string(TABLE.C_STO_DIR);
        }

        public string stoId() {
            return details().@string(TABLE.C_STO_ID);
        }

        public string pvName() {
            return details().@string(TABLE.C_PV_NAME);
        }

        public void insertInto(LiveSystem system) {
            foreach (var entry in data) 
                system.insertParameter(entry.Key, entry.Value);
        }

        public void logSystemCreation(SystemArguments arguments) {
            if(has("systemId"))
                LogC.consoleOut("running id(" + get<int>("systemId") + "), system(" + arguments.siv().system() + ")");
            else LogC.consoleOut("running in research mode(" + systemClassName() + ")");
            if(reDebug()) LogC.consoleOut("in DEBUG mode (slow)");
            if(isLive()) LogC.consoleOut("PV = " + pvName());
            else if (isSTO()) LogC.consoleOut("STO = " + paren(commaSep(stoDir(), stoId(), runNumber())));
            else logParameters();
        }

        public void logParameters() {
            eachKey(data, param => LogC.consoleOut(param + " = " + data[param]));
        }

        public int runNumber() {
            return get<int>("RunNumber");
        }

        public Parameters overwrite(Dictionary<string, double> toOverwrite) {
            each(toOverwrite, e => overwrite(e.Key, "" + e.Value));
            return this;
        }

        public Parameters overwrite(Dictionary<string, string> toOverwrite) {
            each(toOverwrite, e => overwrite(e.Key, e.Value));
            return this;
        }

        public Parameters overwrite(string key, string value) {
            var exists = data.ContainsKey(key);
            if (exists) {
                Bomb.when(READ_ONLY_PARAM_NAMES.Contains(key), 
                    () => "can't overwrite existing value of  " + key);
                data.Remove(key);
            }
            data[key] = value;
            return this;
        }

        public Parameters overwrite(string key, double value) {
            return overwrite(key, value.ToString());
        }

        public string curveFilePath(Siv siv, string marketName) {
            var d = new QDirectory(join(@"\", list(stoDir(), stoId(), "CurvesBin", siv.sviName("_") + "_" + marketName)));
            d.createIfMissing();
            return d.file("run_" + runNumber() + ".bin").path();
        }

        public override string ToString() {
            var b = new StringBuilder();
            b.AppendLine("Parameters:\n");
            each(sort(data.Keys), name => b.AppendLine("\t" + name + " = " + data[name]));
            return b.ToString();
        }

        public string metricsFilePath(Siv siv, string marketName) {
            var d = new QDirectory(join(@"\", list(stoDir(), stoId(), "Metrics")));
            d.createIfMissing();
            return d.file(siv.sviName("_") + "_" + marketName + ".csv").path();
        }
        #region Implementation of IEnumerable
        public IEnumerator<string> GetEnumerator() {
            return data.Keys.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator() {
            return GetEnumerator();
        }
        #endregion
        public int leadBars() {
            return get<int>("LeadBars");
        }

        public void Add(string s, double value) {
            data[s] = "" + value;
        }

        public void Add(string s, string value) {
            data[s] = value;
        }

        public void bePreloaded() {
            loaded = true;
        }

    }
}
