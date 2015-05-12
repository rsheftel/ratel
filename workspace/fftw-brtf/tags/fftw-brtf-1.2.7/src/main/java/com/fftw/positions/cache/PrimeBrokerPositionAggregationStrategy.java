package com.fftw.positions.cache;

import com.fftw.bloomberg.PositionKey;
import com.fftw.positions.AbstractPositionAggregationStrategy;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;
import com.fftw.util.IAggregationCacheStrategy;
import com.fftw.util.IAggregationStrategy;

public class PrimeBrokerPositionAggregationStrategy implements
    IAggregationCacheStrategy<PositionKey, Position> {

    private final IAggregationStrategy<Position> positionAggregator;

    public PrimeBrokerPositionAggregationStrategy(IAggregationStrategy<Position> positionAggregator) {
        this.positionAggregator = positionAggregator;
    }

    public PrimeBrokerPositionAggregationStrategy() {
        final PrimeBrokerPositionAggregationStrategy self = this;
        
        positionAggregator = new AbstractPositionAggregationStrategy() {

            @Override
            public Position convertToAggregate(Position item) {
                return self.convertToAggregate(item);
            }
            
        };
    }
    @Override
    public Position aggregate(Position augend, Position addend) {
        return positionAggregator.aggregate(augend, addend);
    }

    @Override
    public boolean canAggregate(Position augend, Position addend) {
        return positionAggregator.canAggregate(augend, addend);
    }

    @Override
    public Position convertToAggregate(Position item) {
        Position copy = item.copy();
        copy.setLevel1TagName(null);
        copy.setLevel2TagName(null);
        copy.setLevel3TagName(null);
        copy.setLevel4TagName(null);

        return copy;
    }

    @Override
    public PositionKey getAggregateKeyForItem(Position item) {
        ISecurity security = item.getSecurity();


        return new PositionKey(security.getSecurityId(), security.getProductCode(), item.getAccount(), null,
            null, null, null, item.getPrimeBroker(), null);
    }

    @Override
    public PositionKey getAggregateKeyForKey(PositionKey key) {
        return new PositionKey(key.getSecurityId(), key.getProductCode(), key.getAccount(), null, null, 
            null, null, key.getPrimeBroker(), null);
    }

}
