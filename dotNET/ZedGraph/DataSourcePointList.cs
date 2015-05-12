using System;
using System.Data;
using System.Reflection;
using System.Windows.Forms;

namespace ZedGraph {
    /// <summary> 
    ///  
    /// </summary> 
    /// <seealso cref="IPointList" /> 
    /// <seealso cref="IPointListEdit" /> 
    ///  
    /// <author>John Champion</author> 
    /// <version> $Revision: 3.7 $ $Date: 2007-11-05 04:33:26 $ </version> 
    [Serializable] public class DataSourcePointList : IPointList {
        readonly BindingSource _bindingSource;
        string _tagDataMember;

        //private object _dataSource = null; 
        string _xDataMember;
        string _yDataMember;
        string _zDataMember;
        #region Properties
        /// <summary> 
        /// The <see cref="BindingSource" /> object from which to get the bound data 
        /// </summary> 
        /// <remarks> 
        /// Typically, you set the <see cref="System.Windows.Forms.BindingSource.DataSource" /> 
        /// property to a reference to your database, table or list object. The 
        /// <see cref="System.Windows.Forms.BindingSource.DataMember" /> property would be set 
        /// to the name of the datatable within the 
        /// <see cref="System.Windows.Forms.BindingSource.DataSource" />, 
        /// if applicable.</remarks> 
        public BindingSource BindingSource {
            get { return _bindingSource; }
        }

        /// <summary> 
        /// The table or list object from which to extract the data values. 
        /// </summary> 
        /// <remarks> 
        /// This property is just an alias for 
        /// <see cref="System.Windows.Forms.BindingSource.DataSource" />. 
        /// </remarks> 
        public object DataSource {
            get { return _bindingSource.DataSource; }
            set { _bindingSource.DataSource = value; }
        }

        /// <summary> 
        /// The <see cref="string" /> name of the property or column from which to obtain the 
        /// X data values for the chart. 
        /// </summary> 
        /// <remarks>Set this to null leave the X data values set to <see cref="PointPairBase.Missing" /> 
        /// </remarks> 
        public string XDataMember {
            get { return _xDataMember; }
            set { _xDataMember = value; }
        }

        /// <summary> 
        /// The <see cref="string" /> name of the property or column from which to obtain the 
        /// Y data values for the chart. 
        /// </summary> 
        /// <remarks>Set this to null leave the Y data values set to <see cref="PointPairBase.Missing" /> 
        /// </remarks> 
        public string YDataMember {
            get { return _yDataMember; }
            set { _yDataMember = value; }
        }

        /// <summary> 
        /// The <see cref="string" /> name of the property or column from which to obtain the 
        /// Z data values for the chart. 
        /// </summary> 
        /// <remarks>Set this to null leave the Z data values set to <see cref="PointPairBase.Missing" /> 
        /// </remarks> 
        public string ZDataMember {
            get { return _zDataMember; }
            set { _zDataMember = value; }
        }

        /// <summary> 
        /// The <see cref="string" /> name of the property or column from which to obtain the 
        /// tag values for the chart. 
        /// </summary> 
        /// <remarks>Set this to null leave the tag values set to null. If this references string 
        /// data, then the tags may be used as tooltips using the 
        /// <see cref="ZedGraphControl.IsShowPointValues" /> option. 
        /// </remarks> 
        public string TagDataMember {
            get { return _tagDataMember; }
            set { _tagDataMember = value; }
        }

        /// <summary> 
        /// Indexer to access the specified <see cref="PointPair"/> object by 
        /// its ordinal position in the list. 
        /// </summary> 
        /// <param name="index">The ordinal position (zero-based) of the 
        /// <see cref="PointPair"/> object to be accessed.</param> 
        /// <value>A <see cref="PointPair"/> object reference.</value> 
        public PointPair this[int index] {
            get {
                if (index < 0 || index >= _bindingSource.Count) throw new ArgumentOutOfRangeException("Error: Index out of range");

                var row = _bindingSource[index];

                var x = GetDouble(row, _xDataMember, index);
                var y = GetDouble(row, _yDataMember, index);
                var z = GetDouble(row, _zDataMember, index);
                var tag = GetObject(row, _tagDataMember);

                return new PointPair(x, y, z) {Tag = tag};
            }
        }

        /// <summary> 
        /// gets the number of points available in the list 
        /// </summary> 
        public int Count {
            get { return _bindingSource != null ? _bindingSource.Count : 0; }
        }
        #endregion
        #region Constructors
        /// <summary> 
        /// Default Constructor 
        /// </summary> 
        public DataSourcePointList() {
            _bindingSource = new BindingSource();
            _xDataMember = string.Empty;
            _yDataMember = string.Empty;
            _zDataMember = string.Empty;
            _tagDataMember = string.Empty;
        }

        /// <summary> 
        /// Constructor to initialize the DataSourcePointList from an 
        /// existing <see cref="DataSourcePointList" /> 
        /// </summary> 
        public DataSourcePointList(DataSourcePointList rhs)
            : this() {
            _bindingSource.DataSource = rhs._bindingSource.DataSource;
            if (rhs._xDataMember != null) _xDataMember = (string) rhs._xDataMember.Clone();
            if (rhs._yDataMember != null) _yDataMember = (string) rhs._yDataMember.Clone();
            if (rhs._zDataMember != null) _zDataMember = (string) rhs._zDataMember.Clone();
            if (rhs._tagDataMember != null) _tagDataMember = (string) rhs._tagDataMember.Clone();
        }

        /// <summary> 
        /// Implement the <see cref="ICloneable" /> interface in a typesafe manner by just 
        /// calling the typed version of <see cref="Clone" /> 
        /// </summary> 
        /// <returns>A deep copy of this object</returns> 
        object ICloneable.Clone() {
            return Clone();
        }

        /// <summary> 
        /// Typesafe, deep-copy clone method. 
        /// </summary> 
        /// <returns>A new, independent copy of this class</returns> 
        public DataSourcePointList Clone() {
            return new DataSourcePointList(this);
        }
        #endregion
        #region Methods
        /// <summary> 
        /// Extract a double value from the specified table row or data object with the 
        /// specified column name. 
        /// </summary> 
        /// <param name="row">The data object from which to extract the value</param> 
        /// <param name="dataMember">The property name or column name of the value 
        /// to be extracted</param> 
        /// <param name="index">The zero-based index of the point to be extracted. 
        /// </param> 
        static double GetDouble(object row, string dataMember, int index) {
            if (string.IsNullOrEmpty(dataMember)) return index + 1;

            //Type myType = row.GetType();
            var drv = row as DataRowView;
            PropertyInfo pInfo = null;
            if (drv == null) pInfo = row.GetType().GetProperty(dataMember);

            object val;

            if (pInfo != null) val = pInfo.GetValue(row, null);
            else if (drv != null) val = drv[dataMember];
            else throw new Exception("Can't find DataMember '" + dataMember + "' in DataSource");

            // if ( val == null ) 
            // throw new System.Exception( "Can't find DataMember '" + dataMember + "' in DataSource" ); 

            double x;
            if (val == null || val == DBNull.Value) x = PointPairBase.Missing;
            else if (val.GetType() == typeof (DateTime)) x = ((DateTime) val).ToOADate();
            else if (val.GetType() == typeof (string)) x = index + 1;
            else x = Convert.ToDouble(val);

            return x;
        }

        /// <summary> 
        /// Extract an object from the specified table row or data object with the 
        /// specified column name. 
        /// </summary> 
        /// <param name="row">The data object from which to extract the object</param> 
        /// <param name="dataMember">The property name or column name of the object 
        /// to be extracted</param> 
        static object GetObject(object row, string dataMember) {
            if (string.IsNullOrEmpty(dataMember)) return null;

            var pInfo = row.GetType().GetProperty(dataMember);
            var drv = row as DataRowView;

            object val = null;

            if (pInfo != null) val = pInfo.GetValue(row, null);
            else if (drv != null) val = drv[dataMember];

            if (val == null) throw new Exception("Can't find DataMember '" + dataMember + "' in DataSource");

            return val;
        }
        #endregion
    }
}