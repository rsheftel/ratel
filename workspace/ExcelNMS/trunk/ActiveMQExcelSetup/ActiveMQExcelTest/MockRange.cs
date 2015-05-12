using System;
using System.Collections;
using System.Collections.Generic;
using Microsoft.Office.Interop.Excel;

namespace ActiveMQExcelTest {
    class MockRange : Range
    {
        bool _isRow;
        bool _isColumn;

        int _rowStart;
        int _columnStart;

        internal MockRange(object value, int row, int column) {
            _rowStart = row;
            Row = row;
            _columnStart = column;
            Column = column;

            if (value.GetType() == typeof(object[,])) {
                _isRow = true;
                _isColumn = true;
            }

            Value2 = value;
        }

        public MockRange(ICollection<string> values, int row, int column, bool isRow) {
            _rowStart = row;
            Row = row;
            _columnStart = column;
            Column = column;

            _isRow = true;
            _isColumn = true;
            
            var lowerBounds = new[] { 1, 1 };

            var lengths = isRow ? new[] { 1, values.Count } : new[] {values.Count, 1 };

            // create the array in Excel format and convert to what we need
            var myArray = Array.CreateInstance(typeof(object), lengths, lowerBounds);
            var value2 = (object[,])myArray;

            if (isRow) {
                var i = 1;
                foreach (var value in values) {
                    value2[1, i] = value;
                    i++;                
                }
            } else {
                var i = 1;
                foreach (var value in values) {
                    value2[i, 1] = value;
                    i++;
                }
            }

            Value2 = value2;
        }

        object Range.get_Item(object RowIndex, object ColumnIndex)
        {
            throw new NotImplementedException();
        }

        void Range.set_Item(object rowIndex, object columnIndex, object a)
        {
            throw new NotImplementedException();
        }

        public Range Columns
        {
            get
            {
                var columns = new MockRange(Value2, Row, Column) { _isRow = false, _isColumn = true};

                return columns;
            }
        }

        public int Count
        {
            get {
                if (Value2.GetType() == typeof(object[,])) {
                    var localValue = (object[,]) Value2;
                    if (_isRow && _isColumn) {
                        return localValue.GetLength(0) * localValue.GetLength(1);
                    }

                    if (_isRow) {
                        // number of rows
                        return localValue.GetLength(0);
                    }

                    if (_isColumn) {
                        // number of columns
                        return localValue.GetLength(1);
                    }

                } else if (Value2.GetType() == typeof(string)) {
                    return 1;
                }
                return -1;
            }
        }
        public Range Rows
        {
            get {
                var rows = new MockRange(Value2, Row, Column) {_isColumn = false, _isRow = true};

                return rows;
            }
        }

        public object get_Value(object rangeValueDataType)
        {
            var valueType = (XlRangeValueDataType)rangeValueDataType;
            switch (valueType) {
                case XlRangeValueDataType.xlRangeValueDefault:
                    return Value2;
            }

            return null;
        }
        #region manually defined
        public Characters get_Characters(object start, object length) {
            throw new NotImplementedException();
        }
        public Range get_End(XlDirection Direaction) {
            throw new NotImplementedException();
        }

  
        public Range get_Offset(object RowOffset, object ColumnOffset) {
            throw new NotImplementedException();
        }
        public Range get_Range(object Cell1, object Cell2) {
            throw new NotImplementedException();
        }
        public Range get_Resize(object RowSize, object ColumnSize) {
            throw new NotImplementedException();
        }

        public void set_Value(object RangeValueDataType, object a) {
            throw new NotImplementedException();
        }
        #endregion
        public object Activate() {
            throw new NotImplementedException();
        }

        public object AdvancedFilter(XlFilterAction Action, object CriteriaRange, object CopyToRange, object Unique) {
            throw new NotImplementedException();
        }

        public object ApplyNames(object Names, object IgnoreRelativeAbsolute, object UseRowColumnNames, object OmitColumn, object OmitRow, XlApplyNamesOrder Order, object AppendLast) {
            throw new NotImplementedException();
        }

        public object ApplyOutlineStyles() {
            throw new NotImplementedException();
        }

        public string AutoComplete(string String) {
            throw new NotImplementedException();
        }

        public object AutoFill(Range Destination, XlAutoFillType Type) {
            throw new NotImplementedException();
        }

        public object AutoFilter(object Field, object Criteria1, XlAutoFilterOperator Operator, object Criteria2, object VisibleDropDown) {
            throw new NotImplementedException();
        }

        public object AutoFit() {
            throw new NotImplementedException();
        }

        public object AutoFormat(XlRangeAutoFormat Format, object Number, object font, object Alignment, object Border, object Pattern, object width) {
            throw new NotImplementedException();
        }

        public object AutoOutline() {
            throw new NotImplementedException();
        }

        public object BorderAround(object LineStyle, XlBorderWeight Weight, XlColorIndex ColorIndex, object Color) {
            throw new NotImplementedException();
        }

        public object Calculate() {
            throw new NotImplementedException();
        }

        public object CheckSpelling(object CustomDictionary, object IgnoreUppercase, object AlwaysSuggest, object SpellLang) {
            throw new NotImplementedException();
        }

        public object Clear() {
            throw new NotImplementedException();
        }

        public object ClearContents() {
            throw new NotImplementedException();
        }

        public object ClearFormats() {
            throw new NotImplementedException();
        }

        public object ClearNotes() {
            throw new NotImplementedException();
        }

        public object ClearOutline() {
            throw new NotImplementedException();
        }

        public Range ColumnDifferences(object Comparison) {
            throw new NotImplementedException();
        }

        public object Consolidate(object Sources, object Function, object TopRow, object LeftColumn, object CreateLinks) {
            throw new NotImplementedException();
        }

        public object Copy(object Destination) {
            throw new NotImplementedException();
        }

        public int CopyFromRecordset(object Data, object MaxRows, object MaxColumns) {
            throw new NotImplementedException();
        }

        public object CopyPicture(XlPictureAppearance Appearance, XlCopyPictureFormat Format) {
            throw new NotImplementedException();
        }

        public object CreateNames(object top, object left, object bottom, object right) {
            throw new NotImplementedException();
        }

        public object CreatePublisher(object Edition, XlPictureAppearance Appearance, object ContainsPICT, object ContainsBIFF, object ContainsRTF, object ContainsVALU) {
            throw new NotImplementedException();
        }

        public object Cut(object Destination) {
            throw new NotImplementedException();
        }

        public object DataSeries(object Rowcol, XlDataSeriesType Type, XlDataSeriesDate Date, object Step, object Stop, object Trend) {
            throw new NotImplementedException();
        }

        public object Delete(object Shift) {
            throw new NotImplementedException();
        }

        public object DialogBox() {
            throw new NotImplementedException();
        }

        public object EditionOptions(XlEditionType Type, XlEditionOptionsOption Option, object name, object Reference, XlPictureAppearance Appearance, XlPictureAppearance ChartSize, object Format) {
            throw new NotImplementedException();
        }

        public object FillDown() {
            throw new NotImplementedException();
        }

        public object FillLeft() {
            throw new NotImplementedException();
        }

        public object FillRight() {
            throw new NotImplementedException();
        }

        public object FillUp() {
            throw new NotImplementedException();
        }

        public Range Find(object What, object After, object LookIn, object LookAt, object SearchOrder, XlSearchDirection SearchDirection, object MatchCase, object MatchByte, object SearchFormat) {
            throw new NotImplementedException();
        }

        public Range FindNext(object After) {
            throw new NotImplementedException();
        }

        public Range FindPrevious(object After) {
            throw new NotImplementedException();
        }

        public object FunctionWizard() {
            throw new NotImplementedException();
        }

        public bool GoalSeek(object Goal, Range ChangingCell) {
            throw new NotImplementedException();
        }

        public object Group(object Start, object End, object By, object Periods) {
            throw new NotImplementedException();
        }

        public void InsertIndent(int InsertAmount) {
            throw new NotImplementedException();
        }

        public object Insert(object Shift, object CopyOrigin) {
            throw new NotImplementedException();
        }

        public object Justify() {
            throw new NotImplementedException();
        }

        public object ListNames() {
            throw new NotImplementedException();
        }

        public void Merge(object Across) {
            throw new NotImplementedException();
        }

        public void UnMerge() {
            throw new NotImplementedException();
        }

        public object NavigateArrow(object TowardPrecedent, object ArrowNumber, object LinkNumber) {
            throw new NotImplementedException();
        }

        public IEnumerator GetEnumerator() {
            throw new NotImplementedException();
        }

        public string NoteText(object text, object start, object length) {
            throw new NotImplementedException();
        }

        public object Parse(object ParseLine, object Destination) {
            throw new NotImplementedException();
        }

        public object _PasteSpecial(XlPasteType Paste, XlPasteSpecialOperation Operation, object SkipBlanks, object Transpose) {
            throw new NotImplementedException();
        }

        public object _PrintOut(object From, object To, object Copies, object Preview, object ActivePrinter, object PrintToFile, object Collate) {
            throw new NotImplementedException();
        }

        public object PrintPreview(object EnableChanges) {
            throw new NotImplementedException();
        }

        public object RemoveSubtotal() {
            throw new NotImplementedException();
        }

        public bool Replace(object What, object Replacement, object LookAt, object SearchOrder, object MatchCase, object MatchByte, object SearchFormat, object ReplaceFormat) {
            throw new NotImplementedException();
        }

        public Range RowDifferences(object Comparison) {
            throw new NotImplementedException();
        }

        public object Run(object Arg1, object Arg2, object Arg3, object Arg4, object Arg5, object Arg6, object Arg7, object Arg8, object Arg9, object Arg10, object Arg11, object Arg12, object Arg13, object Arg14, object Arg15, object Arg16, object Arg17, object Arg18, object Arg19, object Arg20, object Arg21, object Arg22, object Arg23, object Arg24, object Arg25, object Arg26, object Arg27, object Arg28, object Arg29, object Arg30) {
            throw new NotImplementedException();
        }

        public object Select() {
            throw new NotImplementedException();
        }

        public object Show() {
            throw new NotImplementedException();
        }

        public object ShowDependents(object Remove) {
            throw new NotImplementedException();
        }

        public object ShowErrors() {
            throw new NotImplementedException();
        }

        public object ShowPrecedents(object Remove) {
            throw new NotImplementedException();
        }

        public object Sort(object Key1, XlSortOrder Order1, object Key2, object Type, XlSortOrder Order2, object Key3, XlSortOrder Order3, XlYesNoGuess Header, object OrderCustom, object MatchCase, XlSortOrientation orientation, XlSortMethod SortMethod, XlSortDataOption DataOption1, XlSortDataOption DataOption2, XlSortDataOption DataOption3) {
            throw new NotImplementedException();
        }

        public object SortSpecial(XlSortMethod SortMethod, object Key1, XlSortOrder Order1, object Type, object Key2, XlSortOrder Order2, object Key3, XlSortOrder Order3, XlYesNoGuess Header, object OrderCustom, object MatchCase, XlSortOrientation orientation, XlSortDataOption DataOption1, XlSortDataOption DataOption2, XlSortDataOption DataOption3) {
            throw new NotImplementedException();
        }

        public Range SpecialCells(XlCellType Type, object Value) {
            throw new NotImplementedException();
        }

        public object SubscribeTo(string Edition, XlSubscribeToFormat Format) {
            throw new NotImplementedException();
        }

        public object Subtotal(int GroupBy, XlConsolidationFunction Function, object TotalList, object Replace, object PageBreaks, XlSummaryRow SummaryBelowData) {
            throw new NotImplementedException();
        }

        public object Table(object RowInput, object ColumnInput) {
            throw new NotImplementedException();
        }

        public object TextToColumns(object Destination, XlTextParsingType DataType, XlTextQualifier TextQualifier, object ConsecutiveDelimiter, object Tab, object Semicolon, object Comma, object Space, object Other, object OtherChar, object FieldInfo, object DecimalSeparator, object ThousandsSeparator, object TrailingMinusNumbers) {
            throw new NotImplementedException();
        }

        public object Ungroup() {
            throw new NotImplementedException();
        }

        public Comment AddComment(object text) {
            throw new NotImplementedException();
        }

        public void ClearComments() {
            throw new NotImplementedException();
        }

        public void SetPhonetic() {
            throw new NotImplementedException();
        }

        public object PrintOut(object From, object To, object Copies, object Preview, object ActivePrinter, object PrintToFile, object Collate, object PrToFileName) {
            throw new NotImplementedException();
        }

        public void Dirty() {
            throw new NotImplementedException();
        }

        public void Speak(object SpeakDirection, object SpeakFormulas) {
            throw new NotImplementedException();
        }

        public object PasteSpecial(XlPasteType Paste, XlPasteSpecialOperation Operation, object SkipBlanks, object Transpose) {
            throw new NotImplementedException();
        }

        public Application Application {
            get { throw new NotImplementedException(); }
        }
        public XlCreator Creator {
            get { throw new NotImplementedException(); }
        }
        public object Parent {
            get { throw new NotImplementedException(); }
        }
        public object AddIndent {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }

        public string get_Address(object RowAbsolute, object ColumnAbsolute, XlReferenceStyle ReferenceStyle, object External, object RelativeTo) {
            throw new NotImplementedException();
        }

        public string get_AddressLocal(object RowAbsolute, object ColumnAbsolute, XlReferenceStyle ReferenceStyle, object External, object RelativeTo) {
            throw new NotImplementedException();
        }

        public Areas Areas {
            get { throw new NotImplementedException(); }
        }
        public Borders Borders {
            get { throw new NotImplementedException(); }
        }
        public Range Cells {
            get {
                // is this cheating?
                return this;
            }
        }
        public int Column
        {
            get; private set;
        }
        public object ColumnWidth {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
   
        public Range CurrentArray {
            get { throw new NotImplementedException(); }
        }
        public Range CurrentRegion {
            get { throw new NotImplementedException(); }
        }
        public object this[object RowIndex, object ColumnIndex] {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }

        public Range Dependents {
            get { throw new NotImplementedException(); }
        }
        public Range DirectDependents {
            get { throw new NotImplementedException(); }
        }
        public Range DirectPrecedents {
            get { throw new NotImplementedException(); }
        }
        public Range EntireColumn {
            get { throw new NotImplementedException(); }
        }
        public Range EntireRow {
            get { throw new NotImplementedException(); }
        }
        public Font Font {
            get { throw new NotImplementedException(); }
        }
        public object Formula {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object FormulaArray {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public XlFormulaLabel FormulaLabel {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object FormulaHidden {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object FormulaLocal {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object FormulaR1C1 {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object FormulaR1C1Local {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object HasArray {
            get { throw new NotImplementedException(); }
        }
        public object HasFormula {
            get { throw new NotImplementedException(); }
        }
        public object Height {
            get { throw new NotImplementedException(); }
        }
        public object Hidden {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object HorizontalAlignment {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object IndentLevel {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Interior Interior {
            get { throw new NotImplementedException(); }
        }
        public object Left {
            get { throw new NotImplementedException(); }
        }
        public int ListHeaderRows {
            get { throw new NotImplementedException(); }
        }
        public XlLocationInTable LocationInTable {
            get { throw new NotImplementedException(); }
        }
        public object Locked {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Range MergeArea {
            get { throw new NotImplementedException(); }
        }
        public object MergeCells {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object Name {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Range Next {
            get { throw new NotImplementedException(); }
        }
        public object NumberFormat {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object NumberFormatLocal {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object Orientation {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object OutlineLevel {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public int PageBreak {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public PivotField PivotField {
            get { throw new NotImplementedException(); }
        }
        public PivotItem PivotItem {
            get { throw new NotImplementedException(); }
        }
        public PivotTable PivotTable {
            get { throw new NotImplementedException(); }
        }
        public Range Precedents {
            get { throw new NotImplementedException(); }
        }
        public object PrefixCharacter {
            get { throw new NotImplementedException(); }
        }
        public Range Previous {
            get { throw new NotImplementedException(); }
        }
        public QueryTable QueryTable {
            get { throw new NotImplementedException(); }
        }
        public int Row
        { get; private set; }
        public object RowHeight {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }

        public object ShowDetail {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object ShrinkToFit {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public SoundNote SoundNote {
            get { throw new NotImplementedException(); }
        }
        public object Style {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object Summary {
            get { throw new NotImplementedException(); }
        }
        public object Text {
            get { return Value2; }
        }
        public object Top {
            get { throw new NotImplementedException(); }
        }
        public object UseStandardHeight {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object UseStandardWidth {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Validation Validation {
            get { throw new NotImplementedException(); }
        }
        public object Value2 {
            get; set;
        }
        public object VerticalAlignment {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public object Width {
            get { throw new NotImplementedException(); }
        }
        public Worksheet Worksheet {
            get { throw new NotImplementedException(); }
        }
        public object WrapText {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Comment Comment {
            get { throw new NotImplementedException(); }
        }
        public Phonetic Phonetic {
            get { throw new NotImplementedException(); }
        }
        public FormatConditions FormatConditions {
            get { throw new NotImplementedException(); }
        }
        public int ReadingOrder {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public Hyperlinks Hyperlinks {
            get { throw new NotImplementedException(); }
        }
        public Phonetics Phonetics {
            get { throw new NotImplementedException(); }
        }
        public string ID {
            get { throw new NotImplementedException(); }
            set { throw new NotImplementedException(); }
        }
        public PivotCell PivotCell {
            get { throw new NotImplementedException(); }
        }
        public Errors Errors {
            get { throw new NotImplementedException(); }
        }
        public SmartTags SmartTags {
            get { throw new NotImplementedException(); }
        }
        public bool AllowEdit {
            get { throw new NotImplementedException(); }
        }
        public ListObject ListObject {
            get { throw new NotImplementedException(); }
        }
        public XPath XPath {
            get { throw new NotImplementedException(); }
        }
    }
}