package com.fftw.positions;

import com.fftw.util.IAggregationStrategy;

public abstract class AbstractPositionAggregationStrategy implements IAggregationStrategy<Position> {

    @Override
    public Position aggregate(Position augend, Position addend) {
        Position augendCopy = convertToAggregate(augend);
        Position addendCopy = convertToAggregate(addend);
        
        if (!canAggregate(augendCopy, addendCopy)) {
            throw new IllegalArgumentException("Positions cannot be aggregated, keys do not match");
        }

        return augendCopy.aggregate(addendCopy);
    }

    @Override
    public boolean canAggregate(Position augend, Position addend) {
        return augend.canAggregate(addend);
    }

}
