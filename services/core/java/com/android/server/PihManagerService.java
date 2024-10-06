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

package com.android.server;

import static android.os.Process.SYSTEM_UID;
import static com.android.internal.util.PropImitationHooks.PACKAGE_GMS;
import static com.android.internal.util.PropImitationHooks.PROCESS_GMS_UNSTABLE;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;

import com.android.internal.util.IPihManager;
import com.android.internal.R;
import com.android.server.SystemService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO:
 * - move gms add account activity listener here
 * - add apis for get/set keybox
 */
public class PihManagerService extends SystemService {

    private static final String TAG = "PihManager";
    private static final String SERVICE_NAME = "pih_manager";

    // private static final File CERTIFIED_PROPS_FILE =
    //         File(SystemServiceManager.ensureSystemDir(), "certified_props.json")

    private final Object mLock = new Object();
    private String mCertifiedProps = "{}";

    public PihManagerService(Context context) {
        super(context);
    }

    @Override
    public void onStart() {
        Slog.v(TAG, "Starting PihManager service");
        loadCertifiedProps();
        publishBinderService(SERVICE_NAME, mService);
    }

    @Override
    public void onBootPhase(int phase) {
        // noop
    }

    private void loadCertifiedProps() {
        byte[] jsonBytes;
        try {
            jsonBytes = getContext().getResources().openRawResource(
                    R.raw.certified_build_props).readAllBytes();
        } catch (IOException e) {
            Slog.e(TAG, "failed to read json!", e);
            return;
        }

        mCertifiedProps = new String(jsonBytes, StandardCharsets.UTF_8);

        // String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        // if (TextUtils.isEmpty(jsonString)) {
        //     dlog("certified_props.json is empty!");
        //     return;
        // }

        // try {
        //     mCertifiedProps = new JSONObject(jsonString);
        // } catch (JSONException e) {
        //     Slog.e(TAG, "failed to parse json!", e);
        // }
    }

    // private void writeCertifiedProps() {
    //     try {
    //         Files.write(CERTIFIED_PROPS_FILE.toPath(), mCertifiedProps.toString().getBytes());
    //     } catch (Exception e) {
    //         Slog.e(TAG, "failed to write json!", e);
    //     }
    // }

    private void restartGms() {
        final int gmsUid;
        try {
            gmsUid = getContext().getPackageManager().getApplicationInfo(PACKAGE_GMS, 0).uid;
        } catch (Exception e) {
            Slog.e(TAG, "restartGms failed: unable to get gms uid", e);
            return;
        }

        try {
            ActivityManager.getService().killApplicationProcess(PROCESS_GMS_UNSTABLE, gmsUid);
        } catch (RemoteException e) {
            Slog.e(TAG, "restartGms failed", e);
        }
    }

    private final IPihManager.Stub mService = new IPihManager.Stub() {
        @Override
        public String getCertifiedPropertiesJson() {
            return mCertifiedProps;
        }

        @Override
        public void setCertifiedPropertiesJson(String props) {
            if (Binder.getCallingUid() != SYSTEM_UID) {
                throw new SecurityException("Caller must be system");
            }

            if (TextUtils.isEmpty(props)) {
                Log.e(TAG, "setCertifiedPropertiesJson: cannot be null or empty");
                return;
            }

            try {
                new JSONObject(props);
            } catch (JSONException e) {
                Slog.e(TAG, "setCertifiedPropertiesJson: invalid json!", e);
            }

            synchronized (mLock) {
                if (mCertifiedProps.equals(props)) {
                    dlog("certified props are already equal");
                    return;
                }

                dlog("certified props set: " + props);
                mCertifiedProps = props;
                restartGms();
            }
        }
    };

    private static void dlog(String msg) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Slog.d(TAG, msg);
        }
    }
}
