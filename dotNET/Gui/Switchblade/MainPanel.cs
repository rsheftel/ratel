using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows.Controls;
using AvalonDock;
using Gui.Controls;
using Gui.Util;
using Microsoft.Windows.Controls.Primitives;
using Q.Simulator;
using Q.Trading;
using Q.Trading.Results;
using systemdb.metadata;
using Portfolio=Q.Trading.Results.Portfolio;
using O=Q.Util.Objects;

namespace Gui.Switchblade {
    public class MainPanel : QControl {
        readonly QTextBox idBox;

        readonly QComboBox marketBox;

        SystemDetailsTable.SystemDetails details;
        QDataGrid<RunMetrics, RunMetrics> datagrid;
        STOMetricResults allMetrics;

        public MainPanel() {
            marketBox =  new QComboBox("ALL", marketSelected, true);
            idBox = new QTextBox(systemIdSelected);
            setLayout();

        }

        void setLayout() {
            var outer = downPanel();
            Content = outer;
            addRunSetupPane(outer);
            addSurfacePane(outer);
        }

        void addSurfacePane(Panel resizingPanel) {
            var pane = dockableChild(resizingPanel);
            datagrid = new QDataGrid<RunMetrics, RunMetrics>(loadRow, unloadRow, view => view);
            datagrid.Sorting += (s, e) => {
                allMetrics.sortBy((string) ((DataGridColumnHeader)e.Column.Header).Content);
                e.Column.SortDirection = ListSortDirection.Ascending;
                e.Handled = true;
            };
            pane.Items.Add(new DockableContent {
                Name = "Surface", Title = "Surface", Content = datagrid
            });
        }

        void unloadRow(RunMetrics runMetrics) {
            allMetrics.clearCache(runMetrics);
        }

        void loadRow(RunMetrics runMetrics) {
            datagrid.ifVisible(runMetrics, () => allMetrics.populateValues(runMetrics));    
        }

        void addRunSetupPane(Panel resizingPanel) {
            var pane = dockableChild(resizingPanel);
            ResizingPanel.SetResizeHeight(pane, 45);
            pane.Items.Add(new DockableContent {
                Name = "Setup", Title = "Setup", Content = setupControls()
            });
        }

        object setupControls() {
            var grid = new QGrid();
            grid.addColumn(115); // system id // take 20 or 30 or 40
            grid.addColumn(150); // market
            grid.add(withLabel("System ID", idBox), 0);
            grid.add(withLabel("Market", marketBox), 1);
            return grid;
        }

        void marketSelected(string market) {
            var sto = new sto.STO(details);
            allMetrics = new STOMetricResults(STOMetricResults.hamsterMetricsFile(systemId(), sto.metricFile(market)), STOMetricResults.hamsterParametersFile(systemId(), sto.paramsFile()));
            datagrid.populate(allMetrics);
        }

        int systemId() {
            return int.Parse(idBox.Text);
        }

        void systemIdSelected(string idString) {
            int id;
            if (!int.TryParse(idString, out id)) return;
            if(!SystemDetailsTable.DETAILS.isValid(id)) return;
            var newDetails = SystemDetailsTable.DETAILS.details(id);
            if(!newDetails.hasValidStoDir()) return;
            details = newDetails;
            populateMarkets(id);
        }

        void populateMarkets(int id) {
            List<Symbol> symbols;
            List<Portfolio> portfolios;
            STO.populateSymbolsPortfolios(details, out symbols, out portfolios);
            var names = O.list(O.convert(portfolios, p => p.name));
            names.AddRange(O.convert(symbols, s => s.name));
            marketBox.populateWithAll(names, false);
        }
    }

    internal class QTextBox : TextBox {
        public QTextBox(Action<string> onTextChanged) {
            TextChanged += (s, e) => onTextChanged(Text);
            MaxLines = 1;
            Height = 20;
        }
    }
}