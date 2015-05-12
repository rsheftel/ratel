using System;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32;
using Q.Messaging;
using Q.Trading;
using Q.Util;
using systemdb.data;
using tsdb;
using util;
using O = Q.Util.Objects;
using Symbol=systemdb.data.Symbol;

namespace QExcel {
    [Guid("9EC3A3FB-231D-4dc9-BAA9-72DAD71EC363")]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    [ComVisible(true)]
    public class Tsdb {
        
        public object businessDaysAgo(int count, string dateString, string center) {
            return O.date(Dates.businessDaysAgo(count, O.jDate(dateString), center));
        }

        public object retrieveOneTimeSeriesByName(string seriesName, string source, string start, string end) {
            try {
                var ss = seriesSource(seriesName, source);
                var range = Range.range(start, end);
                var observations = ss.observations(range);
                var result = new object[observations.size(), 2]; // datetime, double
                var i = 0;
                foreach (java.util.Date d in observations) {
                    result[i, 0] = O.date(d);
                    result[i, 1] = observations.value(d);
                    i++;
                }
                return result;
            } catch (Exception e) {
                var message = "failed to retrieve " + seriesName + " " + source + " " + start + " " + end;
                return loggedError(message, e);
            } 
        }

        static object loggedError(string message, Exception e) {
            LogC.setErr(@"c:\QExcel.log");
            LogC.err(message, e);
            message += ".  check " + LogC.errFile() + " to see stack trace.";
            LogC.setErr(null);
            return message;
        }

        public object retrieveOneValueByTimeSeries(string seriesName, string source, string date) {
            try {
                return seriesSource(seriesName, source).observationValue(O.jDate(date));
            } catch (Exception e) {
                var message = "failed to retrieve " + seriesName + " " + source + " initializedValue for " + date;
                return loggedError(message, e);
            }           
        }

        public object liveParameters(string system, string pv) {
            try {
                var parameters = Parameters.liveParameters(system, pv);
                var result = new object[parameters.Count, 2];
                var i = 0;
                O.each(parameters, e => {
                    // name value
                    result[i, 0] = e.Key;
                    result[i, 1] = e.Value;
                    i++;
                 });
                return result;
            }
            catch (Exception e) {
                return loggedError("failed to retrieve live parameters for " + system + " " + pv, e);
            }
        }

        public object retrieveOneSymbol(string marketName, string start, string end) {
            try {
                var bars = rBars(marketName, start, end);
                var result = new object[bars.open.Length, 7];
                O.zeroTo(bars.open.Length, i => {
                    // date open high low close interest volume
                    result[i, 0] = O.date(bars.dates[i]);
                    result[i, 1] = bars.open[i];
                    result[i, 2] = bars.high[i];
                    result[i, 3] = bars.low[i];
                    result[i, 4] = bars.close[i];
                    result[i, 5] = bars.openInterest[i];
                    result[i, 6] = bars.volume[i];
                });
                return result;
            }
            catch (Exception e) {
                return loggedError("failed to retrieve " + marketName + " between " + start + " " + end, e);
            }
        }

        public object retrieveOneSymbolClose(string marketName, string start, string end) {
            try {
                var bars = rBars(marketName, start, end);
                var result = new object[bars.open.Length, 2];
                O.zeroTo(
                    bars.open.Length,
                    i => {
                        // date close
                        result[i, 0] = O.date(bars.dates[i]);
                        result[i, 1] = bars.close[i];
                    });
                return result;
            }
            catch (Exception e) {
                return loggedError("failed to retrieve " + marketName + " between " + start + " " + end, e);
            }
        }

        static RBarData rBars(string marketName, string start, string end) {
            return new Symbol(marketName).rBars(Range.range(start, end));
        }

        public object liveDescription(string symbolName) {
            try {
                var s = new Symbol(symbolName);
                var description = s.jmsLive();
                var result = new object[3];
                result[0] = description.source;
                result[1] = description.template;
                result[2] = description.topicName;
                return result;
            } catch (Exception e) {
                return loggedError("failed to retrieve live description for symbol " + symbolName, e);
            }
        }

        public object publish(string topicName, string[,] keyValues)
        {
            try {
                new Topic(topicName).send(keyValues);
                return true;
            } catch (Exception e) {
                return loggedError("failed to publish message on topic " + topicName, e);
            }
        }

        public object publishOne(string topicName, string key, string value)
        {
            try {
                new Topic(topicName).send(key, value);
                return true;
            } catch (Exception e) {
                return loggedError("failed to publish message on topic " + topicName, e);
            }
        }

        public object getFieldValue(string topicName, string key) {
            try {
                return new Topic(topicName).get<object>(key);
            } catch (Exception e) {
                return loggedError("failed to get initializedValue from topic " + topicName, e);
            }
        }

        public object sqlCell(string sql) {
            try {
                return db.Db.@string(sql);
            } catch (Exception e) {
                return loggedError("failed to get sqlString using sql\r\n" + sql + "\r\n", e);
            }
        }
        
        public void err(string message) {
            LogC.setErr(@"c:\QExcel.log");
            LogC.err(message);
            LogC.setErr(null);
        }


        static SeriesSource seriesSource(string seriesName, string source) {
            return new TimeSeries(seriesName).with(new DataSource(source));
        }
         
        
        [ComRegisterFunctionAttribute]
        public static void RegisterFunction(Type type) {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            if (key != null) 
                key.SetValue("", Environment.SystemDirectory + @"\mscoree.dll", RegistryValueKind.String);
        }

        [ComUnregisterFunctionAttribute]
        public static void UnregisterFunction(Type type) {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }

        private static string GetSubKeyName(Type type, string subKeyName) {
            var s = new StringBuilder();
            s.Append(@"CLSID\{");
            s.Append(type.GUID.ToString().ToUpper());
            s.Append(@"}\");
            s.Append(subKeyName);
            return s.ToString();
        }

    }


}
