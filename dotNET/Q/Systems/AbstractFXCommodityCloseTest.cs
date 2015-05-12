using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Spuds.Core;
using Q.Trading;
using Q.Util;

namespace Q.Systems {
    [TestFixture]
    public class AbstractFXCommodityCloseTest : AbstractTrendingCloseTest<FXCommodityClose> {
        Bar lastBar;
        DateTime time = Objects.date("2009/04/28");

        SymbolSpud<double> signal() {
            return (SymbolSpud<double>) symbolSystem.signal;
        }

        public override void setUp() {
            base.setUp();
            lastBar = null;
            signal().enterTestMode();
            Objects.zeroTo(
                arguments().leadBars,
                i => {
                    signal().add(time, 8.0);
                    processBar(6, 6, 6, 6, time);
                    time = time.AddDays(1);
                    noOrders();
                });
        }

        protected void close(double tri, double signalValue) {
            if(lastBar != null)
                processBar(lastBar);
            signal().add(time, signalValue);
            lastBar = new Bar(tri, tri, tri, tri, time);
            processClose(lastBar);
            time = time.AddDays(1);
        }

        protected override Parameters parameters() {
            return base.parameters().overwrite(
                new Dictionary<string, double> {
                    {"MADays", 5.0},
                    {"RiskDollars", 1000000},
                    {"nATR", 4.0},
                    {"ATRLen", 5.0},
                    {"BollingerBandBarsBack", 4.0},
                    {"BollingerBandDeviations", 1.5},
                    {"MaxBarsHeld", 10},
                    {"signal", 1}}
                );
        }

        protected override int leadBars() {
            return 5;
        }
    }
}