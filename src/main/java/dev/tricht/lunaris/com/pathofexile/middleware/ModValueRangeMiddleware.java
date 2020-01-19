package dev.tricht.lunaris.com.pathofexile.middleware;

import dev.tricht.lunaris.com.pathofexile.request.Query;
import dev.tricht.lunaris.com.pathofexile.request.StatFilter;
import dev.tricht.lunaris.com.pathofexile.request.Value;
import dev.tricht.lunaris.item.Item;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ModValueRangeMiddleware implements TradeMiddleware {

    private int percentage;
    private boolean setMax;

    public ModValueRangeMiddleware(int percentage, boolean setMax) {
        this.percentage = percentage;
        this.setMax = setMax;
    }
    
    @Override
    public void handle(Item item, Query query) {
        if (query.getStats().size() < 1) {
            return;
        }

        for(StatFilter filter : query.getStats().get(0).getFilters()) {
            if (filter.getValue() == null) {
                continue;
            }

            if (filter.getValue().getMin() == null) {
                continue;
            }

            Value value = filter.getValue();

            Double originalValue = value.getMin();

            if (Math.abs(originalValue) <= 3.0) {
                value.setMin(originalValue * (1.0 * (100 - percentage) / 100));
                if (setMax) {
                    value.setMax(originalValue * (1.0 * (100 + percentage) / 100));
                }
                continue;
            }

            value.setMin((double) (int) (originalValue * (1.0 * (100 - percentage) / 100)));
            if(setMax) {
                value.setMax((double) (int) (originalValue * (1.0 * (100 + percentage) / 100)));
            }
        }
    }


}
