using System;
using Q.Spuds.Core;
using Q.Spuds.Indicators;
using Q.Trading;

namespace Q.Systems {
    public class TDSequential : SymbolSystem {
        readonly int setupLength;
        readonly int version;
        readonly int waitForFlip;
        double setupInPlace;
        double stopLevel;
        readonly Spud<double> setupCount;
        int tdCountdown;
        double cancellationLevel;
        readonly int countdownLength;

        public TDSequential(QREBridgeBase bridge, Symbol symbol) : base(bridge, symbol) {
            setupLength = parameter<int>("SetupLength");
            version = parameter<int>("Version");
            waitForFlip = parameter<int>("WaitForFlipOnEntry");
            countdownLength = parameter<int>("CountdownLength");
            setupCount = new TDSetup(bars);
            setupInPlace = 0;
            stopLevel = 0;
            tdCountdown = 0;
            bars.close.prepare();
        }
        
        public override bool runOnClose() {
            return true;
        }

        protected override void onFilled(Position position, Trade trade) {
            if(!trade.description.Contains("Entry")) return;
            var dollarStop = Math.Abs(bar.close - stopLevel) * trade.size * bigPointValue();
            addDynamicExit(new DollarTrailingStop(position, bars.close, dollarStop, "TrailingStop", bridge), false); // true?
        }

        protected override void onNewBar() {
            return;
        }

        protected override void onNewTick(Bar partialBar, Tick tick) {
            return;
        }

        protected override void onClose() {
                     
            //LogC.debug("setupInPlace = " + setupInPlace);
            //LogC.debug("setupCount = " + setupCount[0]);

            switch (version) {
                case 1:
                    if(Math.Abs(setupCount) >= setupLength) {
                        setupInPlace = setupCount;
                        stopLevel = getStopLevelSetup();
                    }
                    if(hasPosition()) {
                        if (position().direction() * -setupInPlace >= 0) return;
                    }
                    if (setupInPlace == 0) return;
                    if(waitForFlip == 1) {   
                        if (setupInPlace < 0 && bullishPriceFlip()) placeSetupTrades();
                        if (setupInPlace > 0 && bearishPriceFlip()) placeSetupTrades();
                        return;
                    }
                    placeSetupTrades();
                    break;
            
                case 2: 
                    if (Math.Abs(setupCount) >= setupLength) {
                        if(Math.Sign(setupCount) != Math.Sign(setupCount) && noPosition()) {
                            tdCountdown = 0;
                            setupInPlace = setupCount;
                        }

                        setupInPlace = setupCount;
                        if (tdCountdown == 0)
                            cancellationLevel = setupCount > 0 ? historicalMin(Math.Abs(setupCount)) : historicalMax(Math.Abs(setupCount));
                    }
                    //LogC.debug("Check1 setupInPlace = " +setupInPlace);
                    //LogC.debug("Check1 cancellationLevel = " + cancellationLevel);
                    if (setupInPlace > 0) {
                        if (noPosition() && bar.high < cancellationLevel) {
                            tdCountdown = 0;
                            setupInPlace = 0;
                        }
                        else if(bar.close > bars[2].high) tdCountdown += 1;
                        
                        if(tdCountdown == 1) stopLevel = 0;
                        stopLevel = Math.Max(bar.high + (bar.high - bar.low),stopLevel);
                        
                    }

                    if (setupInPlace < 0) {
                        if (noPosition() && bar.low > cancellationLevel) {
                            tdCountdown = 0;
                            setupInPlace = 0;
                        } else if (bar.close < bars[2].low) tdCountdown += 1;
                        
                        if(tdCountdown == 1) stopLevel = 100000000;
                        stopLevel = Math.Min(bar.low - (bar.high - bar.low), stopLevel);
                        //LogC.debug("tempStopLevel = " + stopLevel);
                        
                    }
                    //LogC.debug("tdCountdown = " + tdCountdown);
                    if(waitForFlip == 1) {
                        if (tdCountdown >= countdownLength) {
                            if ((hasPosition() && (setupInPlace * position().direction() > 0)) || noPosition()) {
                                if (setupInPlace > 0 && bearishPriceFlip()) placeCountdownTrades();
                                if (setupInPlace < 0 && bullishPriceFlip()) placeCountdownTrades();
                            }
                        }
                    } else
                        if(tdCountdown >= countdownLength) 
                            if((hasPosition() && (setupInPlace * position().direction() > 0)) || noPosition()) placeCountdownTrades();
                    break;
                }
            }
        void placeSetupTrades() {
            placeTrades();
            setupInPlace = 0;
        }

        void placeCountdownTrades() {
            //LogC.debug("inPlaceCDTrades.setupInPlace = " + setupInPlace);
            //LogC.debug("inPlaceCDTrades.tdCountdown = " + tdCountdown);
            //LogC.debug("inPlaceCDTrades.hasPosition = " + hasPosition());
            placeTrades();
            tdCountdown = 0;
            setupInPlace = 0;
        }

        void placeTrades() {
            if(setupInPlace > 0) {
                if (hasPosition()) placeOrder(position().exitLong("LongExitS&R", market(), oneBar()));
                placeOrder(symbol.sell("ShortEntry", market(), tradeSize(), oneBar()));
            }
            else {
                if (hasPosition()) placeOrder(position().exitShort("ShortExitS&R", market(), oneBar()));
                placeOrder(symbol.buy("LongEntry", market(), tradeSize(), oneBar()));
            }
        }

        double historicalMin(double length) {
            double output = 1000000000;
            for(var num = 0; num < length; num++) {
                if(bars[num].low < output) output = bars[num].low;
            }
            return (output);
        }

        double historicalMax(double length) {
            double output = 0;
            for(var num = 0; num < length; num++) {
                if(bars[num].high > output) output = bars[num].high;
            }
            return (output);
        }

        double getStopLevelSetup() {
            double tempStopPrice;
            double stopPrice;

            if(setupInPlace < 0) {
                stopPrice = 1000000000;
                var i = (int) (Math.Abs(setupInPlace) - 1);
                
                while (i >= 0) {
                    tempStopPrice = bars[i].low - (bars[i].high - bars[i].low);
                    if(tempStopPrice < stopPrice) stopPrice = tempStopPrice;
                    i--;
                }
            }
            else {
                stopPrice = 0;
                var i = (int) (setupInPlace - 1);

                while (i >= 0) {
                    tempStopPrice = bars[i].high + (bars[i].high - bars[i].low);
                    if(tempStopPrice > stopPrice) stopPrice = tempStopPrice;
                    i--;
                }
            }
            return stopPrice;
        }

        long tradeSize() {
            var risk = parameter<double>("Risk");
            var initEquity = parameter<double>("InitEquity");
            //LogC.debug("stopLevel = " + stopLevel);
            //LogC.debug("risk = " + risk * initEquity);
            //LogC.debug("bar.close = " + bar.close);
            //LogC.debug("BPV = " + bigPointValue());
            return (long) ((risk * initEquity) / (Math.Max(slippage() * 2, Math.Abs(bar.close - stopLevel)) * bigPointValue()));
        }

        bool bullishPriceFlip() {
            return ((bar.close > bars[4].close) && (bars[1].close < bars[5].close));
        }

        bool bearishPriceFlip() {
            return((bar.close < bars[4].close) && (bars[1].close > bars[5].close));
        }
    }
}