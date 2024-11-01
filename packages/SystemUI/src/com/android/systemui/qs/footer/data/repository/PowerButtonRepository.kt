/*
 * Copyright (C) 2024 Paranoid Android
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

package com.android.systemui.qs.footer.data.repository

import android.os.UserHandle
import android.provider.Settings
import com.android.systemui.common.coroutine.ChannelExt.trySendWithFailureLogging
import com.android.systemui.common.coroutine.ConflatedCallbackFlow.conflatedCallbackFlow
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.qs.dagger.QSFlagsModule.PM_LITE_ENABLED
import com.android.systemui.statusbar.policy.KeyguardStateController
import com.android.systemui.util.settings.SystemSettings
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface PowerButtonRepository {
    /**
     * Whether to show the power button in footer actions.
     */
    val showPowerButton: Flow<Boolean>
}

@SysUISingleton
class PowerButtonRepositoryImpl
@Inject
constructor(
    keyguardStateController: KeyguardStateController,
    systemSettings: SystemSettings,
    @Named(PM_LITE_ENABLED) powerButtonEnabled: Boolean,
) : PowerButtonRepository {

    override val showPowerButton: Flow<Boolean> =
        if (powerButtonEnabled) {
            conflatedCallbackFlow {
                fun updateState() {
                    val shouldHide = !keyguardStateController.isUnlocked() &&
                        keyguardStateController.isMethodSecure() &&
                        systemSettings.getIntForUser(
                            Settings.System.LOCKSCREEN_ENABLE_POWER_MENU,
                            1,
                            UserHandle.USER_CURRENT
                        ) == 0

                    trySendWithFailureLogging(!shouldHide, TAG)
                }

                val callback = object : KeyguardStateController.Callback {
                    override fun onUnlockedChanged() {
                        updateState()
                    }
                }

                keyguardStateController.addCallback(callback)
                updateState()
                awaitClose {
                    keyguardStateController.removeCallback(callback)
                }
            }
        } else {
            flowOf(false)
        }

    companion object {
        private const val TAG = "PowerButtonRepositoryImpl"
    }
}
