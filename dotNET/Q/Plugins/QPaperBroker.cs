using System;
using System.Collections.Generic;
using PaperBroker;
using RE = RightEdge.Common;

namespace Q.Plugins {
    public class QPaperBroker : RE.IService, RE.ISimBroker {
        private readonly PaperTrader innerBroker;
        public QPaperBroker() {
            innerBroker = new PaperTrader();
        }

        public string ServiceName() {
            return "Quantys Paper Broker";
        }

        public string Author() {
            return "Malbec Quantys Fund";
        }

        public string Description() {
            return "Quantys Paper Broker";
        }

        public string CompanyName() {
            return "Malbec Quantys Fund";
        }

        public string Version() {
            return "1.0";
        }

        public string id() {
            return "{EB6304EE-14F2-4cd4-9147-AC8B02B5B1C6}";
        }

        public bool NeedsServerAddress() {
            return innerBroker.NeedsServerAddress();
        }

        public bool NeedsPort() {
            return innerBroker.NeedsPort();
        }

        public bool NeedsAuthentication() {
            return innerBroker.NeedsAuthentication();
        }

        public bool SupportsMultipleInstances() {
            return innerBroker.SupportsMultipleInstances();
        }

        public RE.IBarDataRetrieval GetBarDataInterface() {
            return innerBroker.GetBarDataInterface();
        }

        public RE.ITickRetrieval GetTickDataInterface() {
            return innerBroker.GetTickDataInterface();
        }

        public RE.IBroker GetBrokerInterface() {
            return this;
        }

        public bool HasCustomSettings() {
            return innerBroker.HasCustomSettings();
        }

        public bool ShowCustomSettingsForm(ref RE.SerializableDictionary<string, string> settings) {
            return innerBroker.ShowCustomSettingsForm(ref settings);
        }

        public bool Initialize(RE.SerializableDictionary<string, string> settings) {
            return innerBroker.Initialize(settings);
        }

        public bool Connect(RE.ServiceConnectOptions connectOptions) {
            return innerBroker.Connect(connectOptions);
        }

        public bool Disconnect() {
            return innerBroker.Disconnect();
        }

        public string GetError() {
            return innerBroker.GetError();
        }

        public string ServerAddress {
            get { return innerBroker.ServerAddress; }
            set { innerBroker.ServerAddress = value; }
        }
        public int Port {
            get { return innerBroker.Port; }
            set { innerBroker.Port = value; }
        }
        public string UserName {
            get { return innerBroker.UserName; }
            set { innerBroker.UserName = value; }
        }
        public string Password {
            get { return innerBroker.Password; }
            set { innerBroker.Password = value; }
        }
        public bool BarDataAvailable {
            get { return innerBroker.BarDataAvailable; }
        }
        public bool TickDataAvailable {
            get { return innerBroker.TickDataAvailable; }
        }
        public bool BrokerFunctionsAvailable {
            get { return innerBroker.BrokerFunctionsAvailable; }
        }

        public void Dispose() {
            innerBroker.Dispose();
        }

        public void SimBar(RE.NewBarInfo info) {
            // I think this should call SimClose, but I don't have a good test without TradeInsideBars set.
            innerBroker.SimBar(info);
        }

        public void SimOpen(RE.NewBarInfo info) {
            innerBroker.SimOpen(info);
        }

        public void SimClose(RE.NewBarInfo info) {
            innerBroker.SimBar(info);
        }

        void tick(RE.Symbol s, double price, DateTime time) {
            SimTick(s, new RE.TickData {size = 0, time = time, price = price, tickType = RE.TickType.Trade});
        }

        public void SimHighLow(RE.NewBarInfo info) {
            //innerBroker.SimHighLow(info);
            foreach (var s in info.bars.Keys) {
                var bar = info.bars[s];
                if (bar.Open - bar.Low <= bar.High - bar.Open) {
                    tick(s, bar.Low, bar.PriceDateTime.AddMinutes(1));
                    tick(s, bar.High, bar.PriceDateTime.AddMinutes(2));
                }
                else {
                    tick(s, bar.High, bar.PriceDateTime.AddMinutes(1));
                    tick(s, bar.Low, bar.PriceDateTime.AddMinutes(2));
                }
            }
        }

        public void SimTick(RightEdge.Common.Symbol symbol, RE.TickData tick) {
            innerBroker.SimTick(symbol, tick);
        }

        public void SetBuyingPower(double value) {
            innerBroker.SetBuyingPower(value);
        }

        public void SetAccountInfo(RE.IAccountInfo accountInfo) {
            innerBroker.SetAccountInfo(accountInfo);
        }

        public RE.ReturnCode Deposit(double amount) {
            return innerBroker.Deposit(amount);
        }

        public RE.ReturnCode Withdraw(double amount) {
            return innerBroker.Withdraw(amount);
        }

        public void SetAccountState(RE.BrokerAccountState state) {
            innerBroker.SetAccountState(state);
        }

        public bool SubmitOrder(RE.BrokerOrder order, out string orderId) {
            return innerBroker.SubmitOrder(order, out orderId);
        }

        public bool CancelOrder(string orderId) {
            return innerBroker.CancelOrder(orderId);
        }

        public bool CancelAllOrders() {
            return innerBroker.CancelAllOrders();
        }

        public double GetBuyingPower() {
            return innerBroker.GetBuyingPower();
        }

        public double GetMargin() {
            return innerBroker.GetMargin();
        }

        public double GetShortedCash() {
            return innerBroker.GetShortedCash();
        }

        public List<RE.BrokerOrder> GetOpenOrders() {
            return innerBroker.GetOpenOrders();
        }

        public RE.BrokerOrder GetOpenOrder(string id) {
            return innerBroker.GetOpenOrder(id);
        }

        public int GetShares(RE.Symbol symbol) {
            return innerBroker.GetShares(symbol);
        }

        public void AddOrderUpdatedDelegate(RE.OrderUpdatedDelegate orderUpdated) {
            innerBroker.AddOrderUpdatedDelegate(orderUpdated);
        }

        public void RemoveOrderUpdatedDelegate(RE.OrderUpdatedDelegate orderUpdated) {
            innerBroker.RemoveOrderUpdatedDelegate(orderUpdated);
        }

        public void AddPositionAvailableDelegate(RE.PositionAvailableDelegate positionAvailable) {
            innerBroker.AddPositionAvailableDelegate(positionAvailable);
        }

        public void RemovePositionAvailableDelegate(RE.PositionAvailableDelegate positionAvailable) {
            innerBroker.RemovePositionAvailableDelegate(positionAvailable);
        }

        public RE.IService GetService() {
            return this;
        }

        public bool IsLiveBroker() {
            return innerBroker.IsLiveBroker();
        }
    }
}
