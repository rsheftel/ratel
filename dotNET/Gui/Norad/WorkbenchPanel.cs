using System;
using System.Collections.Generic;
using System.Data;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Forms.Integration;
using AvalonDock;
using file;
using Gui.Controls;
using Gui.Util;
using Microsoft.Windows.Controls;
using Microsoft.Windows.Controls.Primitives;
using Q.Research;
using Q.Simulator;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using Symbol=Q.Trading.Symbol;

namespace Gui.Norad {
    class FilePersistedTextBox : TextBox {
        readonly QFile storage;

        public FilePersistedTextBox(QFile storage) {
            this.storage = storage;
            if (storage.exists()) Text = storage.text();
        }

        public void save() {
            storage.overwrite(Text);
        }
    }

    public class WorkbenchPanel : QControl, ResearchGUI {
        readonly TextBox idBox = new TextBox { MaxLines = 1, Height = 20 };
        readonly TextBox numberBox = new TextBox { MaxLines = 1, Height = 20 };
        readonly DatePicker startDatePicker = datePicker();
        readonly DatePicker endDatePicker = datePicker();
        readonly FilePersistedTextBox nameBox = new FilePersistedTextBox(new QFile(@"c:\logs\lastResearchName.txt"));

        readonly TextBox marketsBox = new TextBox {AcceptsReturn = true};
        readonly TextBox slippageBox = new TextBox();
        readonly DataGrid parameterGrid = makeParameterGrid();

        readonly DataTable parameters_ = new DataTable();
        public readonly QButton runButton;
        public readonly QButton liveButton;
        readonly Button loadButton;
        readonly Button saveButton;
        readonly Button loadSystemButton;
        int resultCount;
        DockablePane resultsPane;
        DockPanel numberBoxWithLabel;
        bool showPlots_ = true;
        readonly CheckBox showPlots;
        readonly CheckBox runInNativeCurrencyBox;
        bool runInNativeCurrency_;

        public WorkbenchPanel() {
            runButton = new QButton("Historical", ()=> new Researcher(this).run(false));
            liveButton = new QButton("Live", () => {
                endDatePicker.SelectedDate = null;
                new Researcher(this).run(true);
            });
            loadSystemButton = new QButton("Load System", () => Researcher.loadSystem(this));
            loadButton = new QButton("Load Settings", () => load(false));
            saveButton = new QButton("Save Settings", saveSettings);
            showPlots = new QCheckBox("With Plots", true, isChecked => showPlots_ = isChecked) {
                FlowDirection = FlowDirection.RightToLeft,
                VerticalContentAlignment = VerticalAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center
            };
            runInNativeCurrencyBox = new QCheckBox("  Run In Native Currency    ", false, isChecked => runInNativeCurrency_ = isChecked) {
                FlowDirection = FlowDirection.RightToLeft,
                VerticalContentAlignment = VerticalAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center,
                HorizontalAlignment = HorizontalAlignment.Left
            };
            setLayout();
            buildParametersTable();
            load(true);
        }

        void buildParametersTable() {
            parameters_.Columns.Add(new DataColumn("Name", typeof (string)));
            parameters_.Columns.Add(new DataColumn("Value", typeof (string)));
            parameterGrid.ItemsSource = parameters_.DefaultView;
        }

        void setLayout() {
            var outer = downPanel();
            Content = outer;
            addRunSetupPane(outer);
            var lower = rightPanel();
            outer.Children.Add(lower);
            addMarketsParameters(lower);
            addResults(lower);
        }

        void addRunSetupPane(Panel resizingPanel) {
            var pane = dockableChild(resizingPanel);
            ResizingPanel.SetResizeHeight(pane, 45);
            pane.Items.Add(new DockableContent {
                Name = "Setup", Title = "Run Setup", Content = runSetupControls()
            });
        }

        object runSetupControls() {
            var grid = new QGrid();
            grid.addColumn(150); // name
            grid.addColumn(100);  // load
            grid.addColumn(100);  // save
            grid.addColumn(20);
            grid.addColumn(115); // system id // take 20 or 30 or 40
            grid.addColumn(115); // run number // take same amount as system id
            grid.addColumn(100); // load system
            grid.addColumn(20);
            grid.addColumn(150);  //startDate
            grid.addColumn(150); // end Date
            grid.addColumn(10);
            grid.addColumn(90); // Plots checkbox
            grid.addColumn(10);
            grid.addColumn(80); // run button (historical)
            grid.addColumn(60); // live button // word Live is about 25 so 30 + border 10 + 20 for checkbox need 90
            grid.add(withLabel("Name:", nameBox), 0);
            grid.add(loadButton, 1);
            grid.add(saveButton, 2);
            grid.add(withLabel("System ID", idBox), 4);
            numberBoxWithLabel = withLabel("RunNumber", numberBox);
            numberBoxWithLabel.IsEnabled = false;
            grid.add(numberBoxWithLabel, 5);
            grid.add(loadSystemButton, 6);
            grid.add(withLabel("Start Date:", startDatePicker), 8);
            grid.add(withLabel("End Date:", endDatePicker), 9);
            grid.add(showPlots, 11);
            grid.add(runButton, 13);
            grid.add(liveButton, 14);
            return grid;
        }

        
        void addMarketsParameters(Panel lower) {
            var resizingPanel = new ResizingPanel {Orientation = Orientation.Vertical};
            ResizingPanel.SetResizeWidth(resizingPanel, 160);
            lower.Children.Add(resizingPanel);
            addMarkets(resizingPanel);
            addParameters(resizingPanel);
            addSwitchesPanel(resizingPanel);
        }
        
        void addSwitchesPanel(Panel outer) {
            var pane = dockableChild(outer);
            ResizingPanel.SetResizeHeight(pane, 100);
            var switches = new QDockPanel();
            pane.Items.Add(new DockableContent {
                Name = "AdditionalSwitches", Title = "Additional Switches", Content = switches 
            });
            switches.add(label("Slippage Calculator"), Dock.Top);
            switches.add(slippageBox, Dock.Top);
            switches.add(runInNativeCurrencyBox, Dock.Top);
        }

        void addMarkets(Panel outer) {
            var pane = dockableChild(outer);
            ResizingPanel.SetResizeHeight(pane, 300);
            pane.Items.Add(new DockableContent {
                Name = "Markets", Title = "Markets", Content = marketsBox
            });
        }
        
        void addParameters(Panel outer) {
            var pane = dockableChild(outer);
            pane.Items.Add(new DockableContent {
                Name = "Parameters", Title = "Parameters", Content = parameterGrid
            });
        }


        static DataGrid makeParameterGrid() {
            var grid = new DataGrid {AutoGenerateColumns = false}; // binding?
            grid.Columns.Add(new DataGridTextColumn {
                Header = "Name", Width=new DataGridLength(100), 
                FontWeight = FontWeights.Bold, Binding = new Binding("Name")
            });
            grid.Columns.Add(new DataGridTextColumn {
                Header = "Value", Width=new DataGridLength(50), 
                FontWeight = FontWeights.Bold, Binding = new Binding("Value")
            });
            return grid;
        }

        void addResults(Panel outer) {
            resultsPane = dockableChild(outer);
            resultsPane.Items.Add(new DockableContent{ Name = "AwaitingResults", Title = "Awaiting Results"});
        }

        public void disableRunButton() {
            runOnGuiThread(() => { runButton.IsEnabled = false; liveButton.IsEnabled = false;});
        }

        public void enableRunButton() {
            runOnGuiThread(() => { runButton.IsEnabled = true; liveButton.IsEnabled = true; });
        }

        public List<string> markets() {
            return Objects.list(Regex.Split(marketsBox.Text.Trim(), @"\s+"));
        }

        public string slippageCalculator() {
            return slippageBox.Text.Trim();
        }

        public Parameters parameters() {
            var result = new Parameters();
            foreach (DataRow row in parameters_.Rows) result.Add((string) row["Name"], double.Parse((string) row["Value"]));
            result.bePreloaded();
            LogC.verbose(() => "presenting preloaded parameters " + result);
            return result;
        }

        public DateTime? startDate() {
            return startDatePicker.SelectedDate;
        }

        public DateTime? endDate() {
            return endDatePicker.SelectedDate;
        }

        public void reportResults(Researcher researcher) {
            DockablePane charts = null;
            var info = researcher.runInfo;
            var simulator = researcher.simulator;
            var tabName = info.parameters.systemName();
            var stoId = info.parameters.stoId() ?? "NA";
            if(!stoId.Equals("NA") && info.parameters.has("RunNumber")) tabName += " " + stoId + " " + Objects.paren("Run " + info.parameters.runNumber());
            else {
                var pvName = info.parameters.pvName() ?? "NA";
                if (!pvName.Equals("NA")) tabName += " " + pvName;
            }
            runOnGuiThread(() => charts = addEmptyResultsTab(tabName));
            Objects.wait(() => charts != null);
            runOnGuiThread(() => addMetricsTab(simulator, charts));
            runOnGuiThread(() => addRunInfoTab(info, charts));
            runOnGuiThread(() => addPositionsTab(researcher, charts));
            runOnGuiThread(() => addTradesTab(simulator, charts, researcher.trades));
            runOnGuiThread(() => addEquityTab(simulator, charts));
            if (!showPlots_) return;

            var collectibles = simulator.collectibles();
            var hasSymbolSystems = Objects.exists(collectibles, collectible => collectible is SymbolSystem);
            Objects.each(collectibles, collectible => {
                                     if(hasSymbolSystems && collectible is Symbol) return;
                                     addChart(researcher, collectible, charts);
                                 });
        }

        void addEquityTab(Simulator simulator, ItemsControl charts) {
            charts.Items.Add(new DockableContent {Title = title("Equity"), Content = new WindowsFormsHost {Child = new EquityPlot(simulator) }});
        }

        string title(string name) {
            return name + ":" + resultCount;
        }

        void addRunInfoTab(SystemRunInfo info, ItemsControl charts) {
            var parametersGrid = new QDataTableGrid();
            charts.Items.Add(new DockableContent {Title = title("RunInfo"), Content = parametersGrid});
            var parametersTable = new QDataTable {
                {"name", typeof (string)},
                {"value", typeof (string)}
            };
            Action<string, string> addRow = (name, value) => {
                                                var row = parametersTable.NewRow();
                                                row["name"] = name;
                                                row["value"] = value;
                                                parametersTable.add(row);
                                            };
            Objects.each(info.parameters, name => addRow(name, info.parameters.get<string>(name)));
            addRow("", "");
            addRow("start date", Objects.ymdHuman(info.startDate, ""));
            addRow("end date", Objects.ymdHuman(info.endDate, ""));
            addRow("slippage calculator", info.slippageCalculatorName);
            addRow("run in native currency", info.runInNativeCurrency + "");
            parametersGrid.populateFromDataTable(parametersTable);
        }

        void addTradesTab(Simulator simulator, ItemsControl charts, IEnumerable<Trade> trades) {
            var dataGrid = new QDataTableGrid();
            var table = new QDataTable {
                {"symbol", typeof(string)},
                {"time", typeof(DateTime)},
                {"description", typeof(string)},
                {"direction", typeof(string)},
                {"size", typeof(int)},
                {"price", typeof(double)},
                {"details", typeof(string)},
                {"fxRate", typeof(double)}
            };
            var panel = new QDockPanel();
            charts.Items.Add(new DockableContent {Title = title("Trades"), Content = panel});
            var controlGrid = new QGrid();
            controlGrid.addColumns(2);
            controlGrid.add(withLabel("S_ymbol", new QComboBox("ALL", selected => table.filter("symbol", selected), markets())), 0);
            controlGrid.add(withLabel("_Direction", new QComboBox("ALL", selected => table.filter("direction", selected), Objects.list("buy", "sell"))), 1);
            panel.add(controlGrid, Dock.Top);
            panel.add(dataGrid, Dock.Bottom);
            Converter<Trade, DataRow> newTradeRow = trade => {
                                                        var row = table.NewRow();
                                                        row["symbol"] = trade.order().symbol.name;
                                                        row["time"] = trade.time;
                                                        row["description"] = trade.description;
                                                        row["direction"] = trade.direction.longShort("buy", "sell");
                                                        row["size"] = trade.size;
                                                        row["price"] = trade.price;
                                                        row["details"] = "        " + trade.order().details;
                                                        row["fxRate"] = trade.fxRate;
                                                        return row;
                                                    };
            Objects.each(Objects.reverse(trades), trade => table.add(newTradeRow(trade)));
            dataGrid.populateFromDataTable(table);
            dataGrid.AutoGeneratedColumns += rightJustifyColumns;
            simulator.addNewTradeListener((position, trade) => runOnGuiThread(() => table.addAtStart(newTradeRow(trade))));
        }

        void addPositionsTab(Researcher researcher, ItemsControl charts) {
            var simulator = researcher.simulator;
            var positionsPane = new DockablePane();
            var dataGrid = new QDataTableGrid();
            var table = new QDataTable("symbol", "entryTime", "exitTime", "direction") {
                {"initialSize", typeof (int)},
                {"pnl", typeof (double)}, 
                {"barsHeld", typeof (int)},
                {"numTrades", typeof(int)},
                {"positionHIDDEN", typeof (Position)}
            };
            var panel = new QDockPanel();
            positionsPane.Items.Add(new DockableContent { Title = title("Position List"), Content = panel });
            charts.Items.Add(new DockableContent {Title = title("Positions"), Content = positionsPane});
            var controlGrid = new QGrid();
            controlGrid.addColumns(3);
            controlGrid.add(withLabel("S_ymbol", new QComboBox("ALL", selected => table.filter("symbol", selected), markets())), 0);
            controlGrid.add(withLabel("_Direction", new QComboBox("ALL", selected => table.filter("direction", selected), Objects.list("long", "short"))), 1);
            controlGrid.add(withLabel("_P&L", new QComboBox("ALL", selected => table.filter<double>("pnl", value => pnlMatchesSelection(selected, value)), Objects.list("winning", "losing"))), 2);
            panel.add(controlGrid, Dock.Top);
            panel.add(dataGrid, Dock.Bottom);

            Action<DataRow, Position> updateRow = (row, position) => {
                                                      row["symbol"] = position.symbol.name;
                                                      row["entryTime"] = Objects.ymdHuman(position.entry().time);
                                                      row["exitTime"] = position.isClosed() ? Objects.ymdHuman(position.exitTrade().time) : "Open";
                                                      row["direction"] = position.entry().direction.ToString();
                                                      row["initialSize"] = position.entry().size;
                                                      row["pnl"] = position.isClosed() ? position.pnl(true, simulator.runInNativeCurrency()) : simulator.pnlForPosition(position);
                                                      row["barsHeld"] = position.barsHeld();
                                                      row["numTrades"] = position.trades().Count;
                                                      row["positionHIDDEN"] = position;
                                                  };            
            Converter<Position, DataRow> newRow = position => {
                                                      var row = table.NewRow();
                                                      updateRow(row, position);
                                                      return row;
                                                  };
            Objects.each(Objects.reverse(researcher.positions), position => table.add(newRow(position)));
            dataGrid.populateFromDataTable(table);
            dataGrid.AutoGeneratedColumns += rightJustifyColumns;
            simulator.addNewTradeListener((position, trade) => runOnGuiThread(() => {
                                                                                  if(position.isEntry(trade)) table.addAtStart(newRow(position));
                                                                                  else updateRow(table.firstRow("positionHIDDEN", position), position);
                                                                              }));
            addFilteredHistogram(positionsPane, table, "Average P&L (Bar To End)", new TradePathHistogramControl(researcher, true, true));
            addFilteredHistogram(positionsPane, table, "Cumulative P&L (Bar To End)", new TradePathHistogramControl(researcher, false, true));
            addFilteredHistogram(positionsPane, table, "Average P&L (Start To Bar)", new TradePathHistogramControl(researcher, true, false));
            addFilteredHistogram(positionsPane, table, "Cumulative P&L (Start To Bar)", new TradePathHistogramControl(researcher, false, false));
            addFilteredHistogram(positionsPane, table, "Pnl Distribution", new PositionHistogramControl(researcher, position => position.pnl(true, researcher.simulator.runInNativeCurrency())));
            addFilteredHistogram(positionsPane, table, "Entry Size Distribution", new PositionHistogramControl(researcher, position => Objects.first(position.trades()).size));
            addFilteredHistogram(positionsPane, table, "Bars Held Distribution", new PositionHistogramControl(researcher, position => position.barsHeld()));
        }

        void addFilteredHistogram<T>(ItemsControl positionsPane, QDataTable table, string titleText, T tradePath) where T : System.Windows.Forms.Control, FilteredPositionsPlot {
            positionsPane.Items.Add(new DockableContent { Title = title(titleText), Content = new WindowsFormsHost {Child = tradePath}});
            table.filterChanged += rows => tradePath.updatePlot(Objects.convert(rows, row => (Position) row["positionHIDDEN"]));
        }

        static bool pnlMatchesSelection(IEquatable<string> selected, double value) {
            if(selected.Equals("ALL")) return true;
            if(selected.Equals("losing")) return value < 0;
            return value > 0;
        }

        void addMetricsTab(Simulator results, ItemsControl charts) {
            var grid = new QDataTableGrid();
            charts.Items.Add(new DockableContent {Title = title("Metrics"), Content = grid});
            grid.populateFromDataTable(results.metrics().table());
            grid.AutoGeneratedColumns += rightJustifyColumns;
        }

        static void rightJustifyColumns(object sender, EventArgs e ) {
            var grid = (QDataTableGrid) sender;
            var style = new Style(typeof (TextBlock));
            style.Setters.Add(new Setter(TextBlock.TextAlignmentProperty, TextAlignment.Right));
            var headerStyle = new Style(typeof (DataGridColumnHeader));
            headerStyle.Setters.Add(new Setter(HorizontalContentAlignmentProperty, HorizontalAlignment.Right));
            Objects.zeroTo(grid.Columns.Count, i => {
                                             if(i == 0) return;
                                             var column = ((DataGridTextColumn) grid.Columns[i]);
                                             column.ElementStyle = style;
                                             column.HeaderStyle = headerStyle;
                                         });
        }

        void addChart(Researcher researcher, Collectible collectible, ItemsControl charts) {
            var simulator = researcher.simulator;
            var bars = collectible.barsMaybe();
            if(bars == null) return;
            var chartAdded = false;
            runOnGuiThread(() => {
                               var chart = new BarSpudGraph(bars, collectible, this);
                               Objects.each(researcher.plots.get(collectible), chart.add);
                               chart.add(Objects.accept(researcher.positions, collectible.collects), Objects.accept(researcher.trades, collectible.collects), simulator);
                               chart.addEquity(simulator);
                               chart.moveBarsToBack();
                               chart.resetYAxis();
                               var symbolPane = new DockablePane();
                               charts.Items.Add(new DockableContent {Title = title(collectible.name), Content = symbolPane});
                               symbolPane.Items.Add(new DockableContent { Title = title(collectible.name + " Plots"), Content = new WindowsFormsHost {Child = chart }});
                               var dataGrid = new QDataTableGrid();
                               var dataTable = chart.makeDataTable();
                               dataGrid.populateFromDataTable(dataTable);
                               symbolPane.Items.Add(new DockableContent { Title = title(collectible.name + " Data"), Content = dataGrid});
                               chartAdded = true;
                           });
            Objects.wait(400,50, ()=> chartAdded);
        }

        DockablePane addEmptyResultsTab(string name) {
            var charts = new DockablePane();
            if (resultCount == 0) resultsPane.Items.Clear();
            resultCount++;
            resultsPane.Items.Add(new DockableContent {
                Name = "Results" + resultCount, Title = title(name), Content = charts
            });
            resultsPane.SelectedIndex = resultsPane.Items.Count - 1;
            return charts;
        }

        public void loadSettings() {
            load(false);
        }

        void load(bool isStarting) {
            Researcher.load(this, isStarting);
            nameBox.save();
        }

        public void saveSettings() {
            nameBox.save();
            Researcher.save(this);
        }

        public string name() {
            return nameBox.Text;
        }

        public void setMarkets(IEnumerable<string> markets) {
            marketsBox.Text = Objects.join("\n", markets);
        }

        public void setParameters(Parameters parameters) { 
            parameters_.Clear();
            parameters.load();
            Objects.each(parameters, name => {
                                         var row = parameters_.NewRow(); 
                                         row.SetField(0, name); 
                                         row.SetField(1, parameters.get<string>(name)); 
                                         parameters_.Rows.Add(row);
                                     });
        }

        public void setStartDate(DateTime? date) {
            startDatePicker.SelectedDate = date;
        }

        public void setEndDate(DateTime? date) {
            endDatePicker.SelectedDate = date;
        }

        public string systemId() {
            return idBox.Text;
        }

        public string runNumber() {
            return numberBox.Text;
        }

        public void setRunNumberEnabled(bool enable) {
            numberBoxWithLabel.IsEnabled = enable;
            if (!enable) numberBox.Clear();
        }

        public void setSystemId(string id) {
            idBox.Text = id;
        }

        public void setRunNumber(string number) {
            numberBox.Text = number;
        }

        public void setSlippageCalculator(string newName) {
            slippageBox.Text = newName;
        }

        public void setRunInNativeCurrency(bool newRunInNativeCurrency) {
            runInNativeCurrencyBox.IsChecked = newRunInNativeCurrency;
        }

        public bool runInNativeCurrency() {
            return runInNativeCurrency_;
        }
    }
}