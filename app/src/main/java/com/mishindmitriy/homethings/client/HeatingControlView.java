package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

/**
 * Created by Dmitry on 30.09.17.
 */
@StateStrategyType(SingleStateStrategy.class)
public interface HeatingControlView extends MvpView {
    void updateHeatingData(HeatingData monitoringData);
}