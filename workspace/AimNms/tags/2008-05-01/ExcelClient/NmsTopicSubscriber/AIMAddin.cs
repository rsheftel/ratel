using System;
using System.Reflection;
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
        public AIMAddin()
        {
        }

        [ComRegisterFunctionAttribute]
        public static void RegisterFunction(Type type)
        {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            RegistryKey key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            key.SetValue("",Environment.SystemDirectory + @"\mscoree.dll",RegistryValueKind.String);
        }

        [ComUnregisterFunctionAttribute]
        public static void UnregisterFunction(Type type)
        {
            Registry.ClassesRoot.DeleteSubKey(GetSubKeyName(type, "Programmable"), false);
        }

        private static string GetSubKeyName(Type type, string subKeyName)
        {
            StringBuilder s = new StringBuilder();
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
            PositionCache.findAllOnlineFieldValues(startsWith, new String[] {"securityId", "level1TagName" }, book);
            PositionCache.findAllBatchFieldValues(startsWith, new String[] {"securityId", "level1TagName"}, book);

            Object[,] cellValues = new Object[book.Count, 2];
            int i = 0;

            foreach (DictionaryEntry entry in book)
            {
                String[] columnValues = (String[])entry.Value;
                cellValues[i, 0] = columnValues[0];
                cellValues[i, 1] = columnValues[1];
                i++;
            }
            return cellValues;
        }
    }
}
