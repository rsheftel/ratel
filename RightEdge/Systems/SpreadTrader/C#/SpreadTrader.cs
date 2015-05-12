using System;
using System.Drawing;
using System.Collections.Generic;
using RightEdge.Common;
using RightEdge.Indicators;

public class SystemMain : SystemBase
{
    public override void Startup()
    {
        // Perform initialization or set system wide options here
		
		// Make sure we have at least two symbols loaded
		if (SystemData.Symbols.Count < 2)
		{
			throw new RightEdgeError("Need two symbols to run a spread trader");
		}

	}

    public override void NewSymbolBar(Symbol symbol, BarData bar)
    {
		Symbol symbol1 = SystemData.Symbols[0];
		Symbol symbol2 = SystemData.Symbols[1];

		// Get the current prices for both symbols
		double close1 = BarUtils.LastValidBar(SystemData.BarCollections[symbol1]).Close;
		double close2 = BarUtils.LastValidBar(SystemData.BarCollections[symbol2]).Close;
		
		// make sure we only process the loop once per bar
		if (symbol1 == symbol)
		{
			// Use the "SpreadThreshold" optimization variable configured in the System Properties window
			if ((close1 * 3) - (close2 * 2) <= SystemData.SystemParameters["SpreadThreshold"])
			{
				OpenPosition(symbol1, PositionType.Long, OrderType.Market, 0, 3);
				OpenPosition(symbol2, PositionType.Short, OrderType.Market, 0, 2);
			}
		}

		// Analyze each position for profit
		double totalProfit = 0.0;
		foreach(Position position in PositionManager.GetOpenPositions())
		{
			totalProfit += position.CurStats.RealizedProfit;
		}

		if (totalProfit >= SystemData.SystemParameters["ProfitThreshold"])
		{
			// Loop through one last time and close all positions
			foreach(Position pos in PositionManager.GetOpenPositions())
			{
				PositionManager.ClosePosition(pos.PosID);
			}
		}

		//	This line of code runs the actions you have set up in in the Project Form
		Actions.RunActions(symbol);
    }
}
