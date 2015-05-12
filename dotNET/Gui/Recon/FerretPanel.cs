using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Gui.Controls;
using Gui.Util;
using Q.Recon;

namespace Gui.Recon {
    public class FerretPanel : QControl, FerretControlGui {
        readonly Label statusLabel;
        readonly QButton dma;
        readonly QButton ticket;
        readonly QButton staged;
        readonly FerretControl control;

        public FerretPanel() {
            control = new FerretControl(this, FerretControl.incomingStatus());
            var grid = new QGrid();
            Content = grid;
            grid.addColumns(4);
            
            statusLabel = new Label {Content = "Currently in Unknown mode", FontSize = 24, VerticalAlignment = VerticalAlignment.Center};
            dma = new QButton("DMA", ()=> control.onButtonPressed("DMA")) { Background = Brushes.SpringGreen, HorizontalAlignment = HorizontalAlignment.Stretch, MinWidth = 250, FontSize = 24, IsEnabled = false };
            ticket = new QButton("Ticket", ()=> control.onButtonPressed("Ticket")) { Background = Brushes.Yellow, HorizontalAlignment = HorizontalAlignment.Stretch, MinWidth = 250, FontSize = 24, IsEnabled = false };
            staged = new QButton("Stage", ()=> control.onButtonPressed("Stage")) { Background = Brushes.Red, HorizontalAlignment = HorizontalAlignment.Stretch, MinWidth = 250, FontSize = 24, IsEnabled = false };

            grid.add(statusLabel, 0);
            grid.add(dma, 1);
            grid.add(ticket, 2);
            grid.add(staged, 3);

        }


        public void setStatus(string newStatus) {
            runOnGuiThread(() => statusLabel.Content = "Currently in " + newStatus + " mode");
        }

        public void setEnabled(bool dmaEnabled, bool ticketEnabled, bool stagedEnabled) {
            runOnGuiThread(() => {
                               dma.IsEnabled = dmaEnabled;
                               ticket.IsEnabled = ticketEnabled;
                               staged.IsEnabled = stagedEnabled;
                           });
        }
    }
}