package com.fftw.positions.cache;

import malbec.util.FuturesSymbolUtil;

import com.fftw.bloomberg.PositionKey;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.positions.AbstractPositionAggregationStrategy;
import com.fftw.positions.IFuturesSecurity;
import com.fftw.positions.ISecurity;
import com.fftw.positions.Position;
import com.fftw.util.IAggregationCacheStrategy;
import com.fftw.util.IAggregationStrategy;

public class SecurityRootPositionAggregationStrategy implements
    IAggregationCacheStrategy<PositionKey, Position> {

    private final IAggregationStrategy<Position> positionAggregator;

    public SecurityRootPositionAggregationStrategy(IAggregationStrategy<Position> positionAggregator) {
        this.positionAggregator = positionAggregator;
    }

    public SecurityRootPositionAggregationStrategy() {
        final SecurityRootPositionAggregationStrategy self = this;
        
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

        ISecurity security = copy.getSecurity();

        if (security instanceof IFuturesSecurity) {
            IFuturesSecurity futures = (IFuturesSecurity) security.copy();
            futures.setUseSecurityRoot(true);
            copy.setSecurity(futures);
        }

        //copy.setOpenPosition(BigDecimal.ZERO);
        
        return copy;
    }

    @Override
    public PositionKey getAggregateKeyForItem(Position item) {
        ISecurity security = item.getSecurity();

        String securityId = security.getSecurityId();
        
        if (security.getProductCode() != BBProductCode.Equity && security.getProductCode() != BBProductCode.Mortgage) {
            securityId = FuturesSymbolUtil.extractSymbolRoot(securityId);
        } 

        return new PositionKey(securityId, security.getProductCode(), item.getAccount(), item
            .getLevel1TagName(), item.getLevel2TagName(), item.getLevel3TagName(), item.getLevel4TagName(),
            item.getPrimeBroker(), null);
    }

    @Override
    public PositionKey getAggregateKeyForKey(PositionKey key) {
        // XXX we might be able to use the Produce Code to reduce the number of calls
        String security = FuturesSymbolUtil.extractSymbolRoot(key.getSecurityId());

        return new PositionKey(security, key.getProductCode(), key.getAccount(), key.getLevel1TagName(), key
            .getLevel2TagName(), key.getLevel3TagName(), key.getLevel4TagName(), key.getPrimeBroker(), null);
    }

}
