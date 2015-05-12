using System;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32;
using System.Collections;

namespace NmsRtdClient
{
    // Replace the Guid below with your own guid that
    // you generate using Create GUID from the Tools menu

    [Guid("BD530B2C-44B2-4fdc-A708-B5063A864FDB")]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    [ComVisible(true)]
    public class AIMAddin
    {
        readonly PositionCache positionCache = PositionCache.Instance;

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
        public Object AIM_AllSecurities(String startsWith, Object updateFlag)
        {
            // Build a unique list of all securities
            IDictionary book = new Hashtable();
            positionCache.FindAllOnlineFieldValues(startsWith, new[] { "securityId", "level1TagName", "normOpenPosition" }, book);
            positionCache.FindAllBatchFieldValues(startsWith, new[] { "securityId", "level1TagName", "normOpenPosition" }, book);

            var cellValues = new Object[book.Count, 3];
            var i = 0;

            foreach (DictionaryEntry entry in book)
            {
                var columnValues = (string[])entry.Value;
                cellValues[i, 0] = columnValues[0];
                cellValues[i, 1] = columnValues[1];
                cellValues[i, 2] = columnValues[2];
                i++;
            }
            return cellValues;
        }
    }
}
