/*
 * Copyright 2016, Leanplum, Inc. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.leanplum;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.leanplum.internal.LeanplumManifestHelper;
import com.leanplum.internal.Log;
import com.leanplum.internal.Util;

import java.util.Collections;

/**
 * Leanplum provider for work with Firebase.
 *
 * @author Anna Orlova
 */
class LeanplumFcmProvider extends LeanplumCloudMessagingProvider {

  public String getRegistrationId() {
    return FirebaseInstanceId.getInstance().getToken();
  }

  public boolean isInitialized() {
    return true;
  }

  @Override
  public boolean isManifestSetup() {
    Context context = Leanplum.getContext();
    if (context == null) {
      return false;
    }

    if (!BuildConfig.DEBUG) {
      return true;
    }
    // Firebase can only be setup through gradle, so we don't have to check manually
    // whether manifest is properly setup. We will only check our own services.
    try {
      boolean hasPushFirebaseMessagingService = LeanplumManifestHelper.checkComponent(
          LeanplumManifestHelper.ApplicationComponent.SERVICE,
          LeanplumPushFirebaseMessagingService.class.getName(), false, null,
          Collections.singletonList(LeanplumManifestHelper.MESSAGING_EVENT), null);

      boolean hasPushFirebaseListenerService = LeanplumManifestHelper.checkComponent(
          LeanplumManifestHelper.ApplicationComponent.SERVICE,
          LeanplumPushFcmListenerService.class.getName(), false, null,
          Collections.singletonList(LeanplumManifestHelper.INSTANCE_ID_EVENT), null);

      boolean hasRegistrationService = LeanplumManifestHelper.checkComponent(
          LeanplumManifestHelper.ApplicationComponent.SERVICE,
          LeanplumPushRegistrationService.class.getName(), false, null, null, null);

      if (hasPushFirebaseMessagingService && hasPushFirebaseListenerService && hasRegistrationService) {
        Log.i("Firebase Messaging is setup correctly.");
        return true;
      }
    } catch (Throwable t) {
      Util.handleException(t);
    }
    Log.e("Failed to setup Firebase Messaging, check your manifest configuration.");
    return true;
  }

  public void unregister() {
    try {
      FirebaseInstanceId.getInstance().deleteInstanceId();
      Log.i("Application was unregistered from FCM.");
    } catch (Exception e) {
      Log.e("Failed to unregister from FCM.");
    }
  }
}
