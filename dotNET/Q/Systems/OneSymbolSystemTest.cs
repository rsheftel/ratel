using System;
using System.Collections.Generic;
using NUnit.Framework;
using Q.Messaging;
using Q.Trading;
using Q.Trading.Results;
using Q.Util;
using O = Q.Util.Objects;

namespace Q.Systems {
    [TestFixture]
    public abstract class OneSymbolSystemTest<S> : OneSystemTest<IndependentSymbolSystems<S>> where S : SymbolSystem {
        private Symbol symbol_;
        protected S symbolSystem;

        public override void setUp() {
            base.setUp();
            symbolSystem = symbolSystemFromBridge();
        }

        protected override void initializeSymbols() {
            Symbol.clearCache();
            symbol_ = initializeSymbol();
        }

        S symbolSystemFromBridge() {
            return Bomb.missing(system().systems_, symbol_);
        }

        protected virtual Symbol initializeSymbol() {
            return new Symbol("RE.TEST.TY.1C", 1000);
        }

        public Symbol symbol() {
            return symbol_;
        }

        protected void fill(int index, double fillPrice) {
            fill(symbol_, index, fillPrice);
        }

        protected List<Order> orders() {
            return orders(symbol_);
        }
        
        protected new void hasOrders(params Order[] expecteds) {
            hasOrders(symbol_, expecteds);
        }

        protected override SystemArguments arguments() {
            var testParams = parameters();
            return arguments(testParams);
        }

        protected SystemArguments arguments(Parameters testParams) {
            return new SystemArguments(O.list(symbol()), new List<Portfolio>(), testParams, leadBars());
        }

        protected virtual void processBar(double open, double high, double low, double close) {
            processBar(open, high, low, close, default(DateTime));
        }

        protected void processClose(double open, double high, double low, double close) {
            processClose(open, high, low, close, default(DateTime));
        }

        protected void processBar(double open, double high, double low, double close, DateTime time) {
            var bar = new Bar(open, high, low, close, time);
            bridge().cutBar(time);
            processBar(bar);
        }

        protected void processClose(double open, double high, double low, double close, DateTime time) {
            var bar = new Bar(open, high, low, close, time);
            bridge().cutBar(time);
            processClose(bar);
        }

        protected void processBar(Bar bar) {
            processBar(O.dictionaryOne(symbol(), bar));
        }
        
        protected void processClose(Bar bar) {
            processClose(O.dictionaryOne(symbol(), bar));
        }

        protected void processTick(double price) {
            processTick(price, date("1980/01/01"));
        }

        protected void processTick(double price, DateTime time) {
            processTick(symbol(), price, time);
        }

        protected PublishCounter positionPublishCounter() {
            return positionPublishCounter(symbol());
        }

        protected Order buy(string description, OrderDetails details, long size, OrderDuration duration) {
            return symbol().buy(description, details, size, duration).placed();
        }
        protected Order sell(string description, OrderDetails details, long size, OrderDuration duration) {
            return symbol().sell(description, details, size, duration).placed();
        }

        protected List<Position> positions() {
            return symbolSystem.positions();
        }        
        
        protected void hasPosition(double amount) {
            AreEqual(amount, position(symbol()).amount);
        }

        protected Position position() {
            return system().position(symbol());
        }
    }
}