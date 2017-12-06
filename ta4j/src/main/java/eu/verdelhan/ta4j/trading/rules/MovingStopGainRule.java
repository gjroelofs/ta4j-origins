/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eu.verdelhan.ta4j.trading.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.SMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

/**
 * A stop-gain rule.
 * <p>
 * Satisfied when the close price reaches the gain threshold.
 */
public class MovingStopGainRule extends AbstractRule {

    /** The close price indicator */
    private ClosePriceIndicator closePrice;
    
    /** The gain ratio threshold (e.g. 1.03 for 3%) */
    private Decimal gainRatioThreshold;
    
    private SMAIndicator movingAverage;

    /**
     * Constructor.
     * @param closePrice the close price indicator
     * @param gainPercentage the gain percentage
     */
    public MovingStopGainRule(ClosePriceIndicator closePrice, Decimal gainPercentage, int frames) {
        this.closePrice = closePrice;
        this.gainRatioThreshold = Decimal.HUNDRED.plus(gainPercentage).dividedBy(Decimal.HUNDRED);
        this.movingAverage = new SMAIndicator(closePrice, frames);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no gain
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            
            if(currentTrade == null) return satisfied;
            if(currentTrade.getEntry() == null && currentTrade.getExit() == null) return satisfied;
            
//            if (currentTrade.isOpened()) {
	            Order target = currentTrade.getExit();
	            if(target == null) target = currentTrade.getEntry();
	            
                Decimal entryPrice = target.getPrice();
                
                Decimal targetPrice = closePrice.getValue(index);
                targetPrice = movingAverage.getValue(index).min(targetPrice);	
                
                Decimal threshold = entryPrice.multipliedBy(gainRatioThreshold);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = targetPrice.isGreaterThanOrEqual(threshold);
                } else {
                    satisfied = targetPrice.isLessThanOrEqual(threshold);
                }
//            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
