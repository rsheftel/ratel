using System;
using System.Drawing;
using System.Drawing.Printing;
using System.Windows.Forms;

namespace ZedGraph {
    partial class ZedGraphControl {
        #region Printing
        /// <summary>
        /// Gets or sets the <see cref="System.Drawing.Printing.PrintDocument" /> instance
        /// that is used for all of the context menu printing functions.
        /// </summary>
        public PrintDocument PrintDocument {
            get {
                // Add a try/catch pair since the users of the control can't catch this one
                try {
                    if (_pdSave == null) {
                        _pdSave = new PrintDocument();
                        _pdSave.PrintPage += Graph_PrintPage;
                    }
                } catch (Exception exception) {
                    MessageBox.Show(exception.Message);
                }

                return _pdSave;
            }
            set { _pdSave = value; }
        }

        /// <summary>
        /// Handler for the "Page Setup..." context menu item.   Displays a
        /// <see cref="PageSetupDialog" />.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        protected void MenuClick_PageSetup(object sender, EventArgs e) {
            DoPageSetup();
        }

        /// <summary>
        /// Handler for the "Print..." context menu item.   Displays a
        /// <see cref="PrintDialog" />.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        protected void MenuClick_Print(object sender, EventArgs e) {
            DoPrint();
        }

        /// <summary>
        /// Rendering method used by the print context menu items
        /// </summary>
        /// <param name="sender">The applicable <see cref="PrintDocument" />.</param>
        /// <param name="e">A <see cref="PrintPageEventArgs" /> instance providing
        /// page bounds, margins, and a Graphics instance for this printed output.
        /// </param>
        void Graph_PrintPage(object sender, PrintPageEventArgs e) {
            var pd = sender as PrintDocument;

            var mPane = MasterPane;
            var isPenSave = new bool[mPane.PaneList.Count + 1];
            var isFontSave = new bool[mPane.PaneList.Count + 1];
            isPenSave[0] = mPane.IsPenWidthScaled;
            isFontSave[0] = mPane.IsFontsScaled;
            for (var i = 0; i < mPane.PaneList.Count; i++) {
                isPenSave[i + 1] = mPane[i].IsPenWidthScaled;
                isFontSave[i + 1] = mPane[i].IsFontsScaled;
                if (!_isPrintScaleAll) continue;
                mPane[i].IsPenWidthScaled = true;
                mPane[i].IsFontsScaled = true;
            }

            var saveRect = mPane.Rect;
            var newSize = mPane.Rect.Size;
            if (_isPrintFillPage && _isPrintKeepAspectRatio) {
                var xRatio = e.MarginBounds.Width / newSize.Width;
                var yRatio = e.MarginBounds.Height / newSize.Height;
                var ratio = Math.Min(xRatio, yRatio);

                newSize.Width *= ratio;
                newSize.Height *= ratio;
            } else if (_isPrintFillPage) newSize = e.MarginBounds.Size;

            mPane.ReSize(
                e.Graphics,
                new RectangleF(
                    e.MarginBounds.Left,
                    e.MarginBounds.Top,
                    newSize.Width,
                    newSize.Height));
            mPane.Draw(e.Graphics);

            using (var g = CreateGraphics()) mPane.ReSize(g, saveRect);
                //g.Dispose();

            mPane.IsPenWidthScaled = isPenSave[0];
            mPane.IsFontsScaled = isFontSave[0];
            for (var i = 0; i < mPane.PaneList.Count; i++) {
                mPane[i].IsPenWidthScaled = isPenSave[i + 1];
                mPane[i].IsFontsScaled = isFontSave[i + 1];
            }
        }

        /// <summary>
        /// Display a <see cref="PageSetupDialog" /> to the user, allowing them to modify
        /// the print settings for this <see cref="ZedGraphControl" />.
        /// </summary>
        public void DoPageSetup() {
            var pd = PrintDocument;

            // Add a try/catch pair since the users of the control can't catch this one
            try {
                if (pd != null) {
                    //pd.PrintPage += new PrintPageEventHandler( GraphPrintPage );
                    var setupDlg = new PageSetupDialog {Document = pd};

                    if (setupDlg.ShowDialog() == DialogResult.OK) {
                        pd.PrinterSettings = setupDlg.PrinterSettings;
                        pd.DefaultPageSettings = setupDlg.PageSettings;

                        // BUG in PrintDocument!!!  Converts in/mm repeatedly
                        // http://support.microsoft.com/?id=814355
                        // from http://www.vbinfozine.com/tpagesetupdialog.shtml, by Palo Mraz
                        //if ( System.Globalization.RegionInfo.CurrentRegion.IsMetric )
                        //{
                        //	setupDlg.Document.DefaultPageSettings.Margins = PrinterUnitConvert.Convert(
                        //	setupDlg.Document.DefaultPageSettings.Margins,
                        //	PrinterUnit.Display, PrinterUnit.TenthsOfAMillimeter );
                        //}
                    }
                }
            } catch (Exception exception) {
                MessageBox.Show(exception.Message);
            }
        }

        /// <summary>
        /// Display a <see cref="PrintDialog" /> to the user, allowing them to select a
        /// printer and print the <see cref="MasterPane" /> contained in this
        /// <see cref="ZedGraphControl" />.
        /// </summary>
        public void DoPrint() {
            // Add a try/catch pair since the users of the control can't catch this one
            try {
                var pd = PrintDocument;

                if (pd != null) {
                    //pd.PrintPage += new PrintPageEventHandler( Graph_PrintPage );
                    var pDlg = new PrintDialog {Document = pd};
                    if (pDlg.ShowDialog() == DialogResult.OK) pd.Print();
                }
            } catch (Exception exception) {
                MessageBox.Show(exception.Message);
            }
        }

        /// <summary>
        /// Display a <see cref="PrintPreviewDialog" />, allowing the user to preview and
        /// subsequently print the <see cref="MasterPane" /> contained in this
        /// <see cref="ZedGraphControl" />.
        /// </summary>
        public void DoPrintPreview() {
            // Add a try/catch pair since the users of the control can't catch this one
            try {
                var pd = PrintDocument;

                if (pd != null) {
                    var ppd = new PrintPreviewDialog {Document = pd};
                    //pd.PrintPage += new PrintPageEventHandler( Graph_PrintPage );
                    ppd.Show(this);
                }
            } catch (Exception exception) {
                MessageBox.Show(exception.Message);
            }
        }
        #endregion
    }
}