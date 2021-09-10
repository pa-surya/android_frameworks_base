/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs;

import static com.android.systemui.qs.dagger.QSFragmentModule.QS_FOOTER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.qs.dagger.QSScope;
import com.android.systemui.settings.UserTracker;
import com.android.systemui.util.ViewController;

import com.android.settingslib.wifi.WifiStatusTracker;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Controller for {@link QSFooterView}.
 */
@QSScope
public class QSFooterViewController extends ViewController<QSFooterView> implements QSFooter {

    private final UserTracker mUserTracker;
    private final QSPanelController mQsPanelController;
    private final QuickQSPanelController mQuickQSPanelController;
    private final FooterActionsController mFooterActionsController;
    private final PageIndicator mPageIndicator;
    private final WifiStatusTracker mWifiTracker;
    private final Context mContext;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mWifiTracker.handleBroadcast(intent);
            onWifiStatusUpdated();
        }
    };

    @Inject
    QSFooterViewController(QSFooterView view,
            UserTracker userTracker,
            QSPanelController qsPanelController,
            QuickQSPanelController quickQSPanelController,
            @Named(QS_FOOTER) FooterActionsController footerActionsController,
            Context context) {
        super(view);
        mUserTracker = userTracker;
        mQsPanelController = qsPanelController;
        mQuickQSPanelController = quickQSPanelController;
        mFooterActionsController = footerActionsController;
        mContext = context;
        mPageIndicator = mView.findViewById(R.id.footer_page_indicator);
        mWifiTracker = new WifiStatusTracker(context, context.getSystemService(WifiManager.class),
                context.getSystemService(NetworkScoreManager.class),
                context.getSystemService(ConnectivityManager.class),
                        this::onWifiStatusUpdated);
    }

    @Override
    protected void onInit() {
        super.onInit();
        mFooterActionsController.init();
    }

    @Override
    protected void onViewAttached() {
        mView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    mView.updateExpansion();
                    mFooterActionsController.updateAnimator(right - left,
                            mQuickQSPanelController.getNumQuickTiles());
                }
        );

        mQsPanelController.setFooterPageIndicator(mPageIndicator);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, filter);
        mWifiTracker.fetchInitialState();
        mWifiTracker.setListening(true);
        onWifiStatusUpdated();

        mView.updateEverything();
    }

    @Override
    protected void onViewDetached() {
        setListening(false);
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void setVisibility(int visibility) {
        mView.setVisibility(visibility);
    }

    @Override
    public void setExpanded(boolean expanded) {
        mFooterActionsController.setExpanded(expanded);
        mView.setExpanded(expanded);
    }

    @Override
    public void setExpansion(float expansion) {
        mView.setExpansion(expansion);
        mFooterActionsController.setExpansion(expansion);
    }

    @Override
    public void setListening(boolean listening) {
        mFooterActionsController.setListening(listening);
    }

    @Override
    public void setKeyguardShowing(boolean keyguardShowing) {
        mView.setKeyguardShowing();
        mFooterActionsController.setKeyguardShowing();
    }

    /** */
    @Override
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        mView.setExpandClickListener(onClickListener);
    }

    @Override
    public void disable(int state1, int state2, boolean animate) {
        mView.disable(state2);
        mFooterActionsController.disable(state2);
    }

    private void onWifiStatusUpdated() {
        mView.setIsWifiConnected(mWifiTracker.connected);
        mView.setWifiSsid(mWifiTracker.ssid);
    }
}
