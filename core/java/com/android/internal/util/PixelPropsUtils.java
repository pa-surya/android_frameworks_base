/*
 * Copyright (C) 2020 The Pixel Experience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    private static final String TAG = "PixelPropsUtils";
    private static final boolean DEBUG = false;

    private static final Map<String, Object> propsToChange;
    private static final Map<String, Object> propsToChangeP6;
    private static final Map<String, Object> propsToChangeP5;
    private static final Map<String, ArrayList<String>> propsToKeep;
    private static final String[] extraPackagesToChange = {
        "com.android.chrome",
        "com.android.vending"
    };
    private static final String[] packagesToChangeP5 = {
        "com.google.android.tts",
        "com.google.android.googlequicksearchbox",
        "com.google.android.apps.inputmethod.latin"
    };
    private static final String[] packagesToKeep = {
        "com.google.android.GoogleCamera",
        "com.google.android.GoogleCamera.Cameight",
        "com.google.android.GoogleCamera.Go",
        "com.google.android.GoogleCamera.Urnyx",
        "com.google.android.GoogleCameraAsp",
        "com.google.android.GoogleCameraCVM",
        "com.google.android.GoogleCameraEng",
        "com.google.android.GoogleCameraEng2",
        "com.google.android.MTCL83",
        "com.google.android.UltraCVM",
        "com.google.android.apps.cameralite"
    };

    static {
        propsToKeep = new HashMap<>();
        propsToChange = new HashMap<>();
        propsToChange.put("BRAND", "google");
        propsToChange.put("MANUFACTURER", "Google");
        propsToChange.put("IS_DEBUGGABLE", false);
        propsToChange.put("IS_ENG", false);
        propsToChange.put("IS_USERDEBUG", false);
        propsToChange.put("IS_USER", true);
        propsToChange.put("TYPE", "user");
        propsToChangeP5 = new HashMap<>();
        propsToChangeP5.put("DEVICE", "redfin");
        propsToChangeP5.put("PRODUCT", "redfin");
        propsToChangeP5.put("MODEL", "Pixel 5");
        propsToChangeP5.put("FINGERPRINT", "google/redfin/redfin:12/SQ1A.211205.008/7888514:user/release-keys");
        propsToChangeP6 = new HashMap<>();
        propsToChangeP6.put("DEVICE", "raven");
        propsToChangeP6.put("PRODUCT", "raven");
        propsToChangeP6.put("MODEL", "Pixel 6 Pro");
        propsToChangeP6.put("FINGERPRINT", "google/raven/raven:12/SQ1D.211205.017/7955197:user/release-keys");
    }

    public static void setProps(String packageName) {
        if (packageName == null){
            return;
        }
        if ((packageName.startsWith("com.google.") && !Arrays.asList(packagesToKeep).contains(packageName))
                || Arrays.asList(extraPackagesToChange).contains(packageName)) {
            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            if (Arrays.asList(packagesToChangeP5).contains(packageName)) {
                propsToChange.putAll(propsToChangeP5);
            } else {
                propsToChange.putAll(propsToChangeP6);
            }
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)){
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
        }
    }

    private static void setPropValue(String key, Object value){
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }
}
