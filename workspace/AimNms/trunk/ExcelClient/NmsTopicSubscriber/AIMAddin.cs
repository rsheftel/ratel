using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32;

namespace NmsRtdClient
{
    // Replace the Guid below with your own guid that
    // you generate using Create GUID from the Tools menu

    [Guid("BD530B2C-44B2-4fdc-A708-B5063A864FDB")]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    [ComVisible(true)]
    public class AIMAddin
    {
        readonly PositionCache _positionCache = PositionCache.Instance;

        [ComRegisterFunctionAttribute]
        public static void RegisterFunction(Type type)
        {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            if (key != null) key.SetValue("",Environment.SystemDirectory + @"\mscoree.dll",RegistryValueKind.String);
        }

        [ComUnregisterFunctionAttribute]
        public static void UnregisterFunction(Type type)
        {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }

        private static string GetSubKeyName(Type type, string subKeyName)
        {
            var s = new StringBuilder();
            s.Append(@"CLSID\{");
            s.Append(type.GUID.ToString().ToUpper());
            s.Append(@"}\");
            s.Append(subKeyName);

            return s.ToString();
        }

        /// <summary>
        /// Get all the securities for a book/account.
        /// </summary>
        /// <param name="startsWith"></param>
        /// <param name="updateFlag">used to force a refresh on an event</param>
        /// <returns></returns>
        public object AIM_AllSecurities(string startsWith, object updateFlag)
        {
            // Build a unique list of all securities
            IDictionary<string, string[]> book = new Dictionary<string, string[]>();
            _positionCache.FindAllOnlineFieldValues(startsWith, new[] {"securityId", "level1TagName" }, book);
            _positionCache.FindAllBatchFieldValues(startsWith, new[] {"securityId", "level1TagName"}, book);

            var cellValues = new object[book.Count, 2];
            var i = 0;

            foreach (var entry in book)
            {
                var columnValues = entry.Value;
                cellValues[i, 0] = columnValues[0];
                cellValues[i, 1] = columnValues[1];
                i++;
            }
            return cellValues;
        }

        /// <summary>
        /// =RTNow("FRQ:10S")
        /// =RTNow("FRQ:15M")
        /// =RTNow("FRQ:2H")
        /// 
        /// Default is FRQ:5S
        /// </summary>
        /// <param name="pattern"></param>
        /// <returns></returns>
        public object RTNow1(object pattern) {
            return "NA";
        }


    }
}
