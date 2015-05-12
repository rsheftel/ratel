package com.fftw.positions.cache;

import com.fftw.bloomberg.PositionKey;
import com.fftw.positions.AbstractPositionAggregationStrategy;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;
import com.fftw.util.IAggregationCacheStrategy;
import com.fftw.util.IAggregationStrategy;

public class DefaultPositionAggregationStrategy implements
    IAggregationCacheStrategy<PositionKey, Position> {

    private final IAggregationStrategy<Position> positionAggregator;

    public DefaultPositionAggregationStrategy(IAggregationStrategy<Position> positionAggregator) {
        this.positionAggregator = positionAggregator;
    }

    public DefaultPositionAggregationStrategy() {
        final DefaultPositionAggregationStrategy self = this;
        
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
        return item.copy();
    }

    @Override
    public PositionKey getAggregateKeyForItem(Position item) {
        ISecurity security = item.getSecurity();

        return new PositionKey(security.getSecurityId(), security.getProductCode(), item.getAccount(), item
            .getLevel1TagName(), item.getLevel2TagName(), item.getLevel3TagName(), item.getLevel4TagName(),
            item.getPrimeBroker(), item.getOpenPosition());
    }

    @Override
    public PositionKey getAggregateKeyForKey(PositionKey key) {
        return new PositionKey(key.getSecurityId(), key.getProductCode(), key.getAccount(), key.getLevel1TagName(), key
            .getLevel2TagName(), key.getLevel3TagName(), key.getLevel4TagName(), key.getPrimeBroker(), key
            .getOnlineOpenPosition());
    }

}
