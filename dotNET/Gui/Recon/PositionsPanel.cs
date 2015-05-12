using System;
using System.Data;
using System.Windows.Media;
using Gui.Controls;
using Gui.Util;
using jms;
using Q.Recon;
using Q.Util;

namespace Gui.Recon {
    internal class PositionsPanel :QControl, PositionTrackerGUI{
        readonly PositionTracker positionTracker;
        readonly QDataTableGrid positionGrid;

        readonly Timers<DataRow> goRed = new Timers<DataRow>();

        public PositionsPanel() {
            var panel = new QDockPanel();
            Content = panel;
            positionGrid = new QDataTableGrid(loadPositionRow, unloadPositionRow);
            panel.add(positionGrid);
            LogC.info("starting position tracker");
            positionTracker = new PositionTracker(this, 
                new QTopic("Redi.Positions.00182087-T.*", PositionTracker.DEFAULT_RECONCILIATION_BROKER, true), 
                new QTopic("Aim.Positions.QMF.*", PositionTracker.DEFAULT_RECONCILIATION_BROKER, true)
            );
            Loaded += (sender, args) => positionTracker.initialize();
        }

        public void setTable(DataTable newTable) {
            positionGrid.populateFromDataTable(newTable);
        }

        public void setStatus(DataRow row, PositionTracker.Status status) {
            Action<Brush> colorRow = color => positionGrid.makeColor(row, color);
            switch (status) {
                case PositionTracker.Status.MATCHED: colorRow( Brushes.SpringGreen); break;
                case PositionTracker.Status.UNMATCHED: 
                    colorRow( Brushes.Yellow);
                    var time = Objects.date((string) row[PositionTracker.LASTUPDATED_COL]);
                    var redTime = time.AddSeconds(3);
                    goRed.replace(row, redTime, () => colorRow(Brushes.Red));
                    break;
                default: Bomb.toss("unkown status " + status); break;
            }
        }

        void loadPositionRow(DataRow row) {
            setStatus(row, positionTracker.reconciliationStatus( (string) row[PositionTracker.BLOOMBERGID_COL]));
        }

        void unloadPositionRow(DataRow row) {
            goRed.remove(row);
        }
    }
}