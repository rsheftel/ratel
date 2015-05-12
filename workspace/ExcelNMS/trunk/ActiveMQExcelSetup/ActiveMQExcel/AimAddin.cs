using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32;

namespace ActiveMQExcel {
    /// <summary>
    /// Excel Functions to deal with AIM positions.
    /// </summary>
    [ComVisible(true)]
    [GuidAttribute("CA5152A6-1D30-4300-AEFC-B49CD51D34DB")]
    public interface IAimFunctions
    {
        //[InterfaceType(ComInterfaceType.InterfaceIsDual)]
        object AIM_AllSecurities(string startsWith, object updateFlag);
    }

    /// <summary>
    /// Implements the logic for the AIM positions within Excel
    /// Extensibility.IDTExtensibility2 - implementing this might give us access to the
    /// entire spreasheet
    /// //[Guid("BD530B2C-44B2-4fdc-A708-B5063A864FDB")] original 
    /// </summary>
    [Guid("4110449D-1961-4b04-979D-BFA00824ABA8")]
    [ClassInterface(ClassInterfaceType.None)]
    [ComVisible(true)]
    public class AimAddin : IAimFunctions
    {
        //[ComSourceInterfaces(typeof(IAimFunctions))]
        readonly PositionCache _positionCache = PositionCache.Instance;

        #region COM Registration
        [ComRegisterFunction]
        public static void RegisterFunction(Type type)
        {
            Registry.ClassesRoot.CreateSubKey(GetSubKeyName(type, "Programmable"));
            var key = Registry.ClassesRoot.OpenSubKey(GetSubKeyName(type, "InprocServer32"), true);
            if (key != null) key.SetValue("",Environment.SystemDirectory + @"\mscoree.dll",RegistryValueKind.String);
        }

        [ComUnregisterFunction]
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
        #endregion

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
            return Sort(cellValues);
        }

        public static Array Sort(object[,] objects)
        {
            var keys = ExtractDimension(objects, 0);
            var values = ExtractDimension(objects, 1);
            Array.Sort(keys, values);

            return CombineArrays(keys, values);
        }

        static Array CombineArrays(object[] keys, object[] values)
        {
            var combinedArray = new object[keys.Length, 2];

            for (var i = keys.GetLowerBound(0); i < keys.GetLength(0); i++) {
                combinedArray[i, 0] = keys[i];
                combinedArray[i, 1] = values[i];
            }

            return combinedArray;
        }

        static object[] ExtractDimension(object[,] sourceArray, int dimension)
        {
            var dimensionValues = new object[sourceArray.GetLength(0)];

            for (var i = sourceArray.GetLowerBound(0); i < sourceArray.GetLength(0); i++) {
                dimensionValues[i] = sourceArray[i, dimension];
            }

            return dimensionValues;
        }
    }
}