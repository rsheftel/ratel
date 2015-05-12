package quickfix.examples.executor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RandomMarketDataProvider implements IMarketDataProvider {

    private double initialMarketPrice;
    private final ScheduledExecutorService quoteGenerator = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, BidAsk> bidAsk = new HashMap<String, BidAsk>();

    public RandomMarketDataProvider(double initialMarketPrice) {
        this.initialMarketPrice = initialMarketPrice;

        quoteGenerator.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                createTick();
            }

            private void createTick() {
                Random generator = new Random();

                synchronized (bidAsk) {
                    for (Map.Entry<String, BidAsk> entry : bidAsk.entrySet()) {
                        double movement = generator.nextDouble();
                        int direction = generator.nextInt(2);
                        BidAsk prices = entry.getValue();
                        // only move the price down if we stay positive
                        if (direction == 1 && prices.ask > (2.0d * movement)) {
                            prices.ask -= movement;
                            prices.bid -= movement;
//                            String priceDisplay = "(" + prices +")";
//                            System.err.println("Moving tick down for " + entry.getKey() + priceDisplay);
                        } else {
                            prices.ask += movement;
                            prices.bid += movement;
//                            String priceDisplay = "(" + prices +")";
//                            System.err.println("Moving tick up for " + entry.getKey() + priceDisplay);
                        }
                    }
                }
            }
        }, 2000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public double getAsk(String symbol) {
        synchronized (bidAsk) {
            BidAsk prices = bidAsk.get(symbol);

            if (prices == null) {
                prices = new BidAsk();
                prices.ask = initialMarketPrice + .5;
                prices.bid = initialMarketPrice - .5;
                bidAsk.put(symbol, prices);
            }

            return prices.ask;
        }
    }

    @Override
    public double getBid(String symbol) {
        synchronized (bidAsk) {
            BidAsk prices = bidAsk.get(symbol);

            if (prices == null) {
                prices = new BidAsk();
                prices.ask = initialMarketPrice + .5;
                prices.bid = initialMarketPrice - .5;
                bidAsk.put(symbol, prices);
            }

            return prices.bid;
        }
    }

    private static class BidAsk {
        private DecimalFormat myFormatter = new DecimalFormat("#.###");
        
        private double ask;
        private double bid;

        public String toString() {
            return myFormatter.format(ask)+":" + myFormatter.format(bid); 
        }
    }
}
