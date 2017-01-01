/*
 * The MIT License
 *
 * Copyright (c) 2016-17 Nishant Pathak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *
 */


package com.nishant.mocket_demo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.network.mocket.MocketException;
import com.network.mocket.builder.client.Client;
import com.network.mocket.builder.client.ClientBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MocketClient {
  private static final String TAG = MocketClient.class.getSimpleName();
  private static MocketClient ourInstance = new MocketClient();

  private ExecutorService executorService;

  private volatile Client<LatLng> client;

  private volatile boolean isInitialized;

  static MocketClient getInstance() {
    return ourInstance;
  }

  private MocketClient() {
    executorService = Executors.newSingleThreadExecutor();
    isInitialized = false;
  }

  void init() {
    executorService.execute(() -> {
      try {
        client = new ClientBuilder<LatLng>()
          .host(BuildConfig.SERVER_HOST, BuildConfig.SERVER_PORT)
          .ensureDelivery(false)
      //    .addHandler(new LatLangStreamHandler())
          .build();
        isInitialized = true;
      } catch (MocketException e) {
        e.printStackTrace();
      }
    });
  }

  void pushLatLngToServer(@NonNull LatLng location) {
    Objects.requireNonNull(location, "Location cannot be null");
    if (isInitialized) {
      try {
        client.write(location);
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }
    } else {
      Log.d(TAG, "Not initialized yet");
    }
  }

  void shutDown() {
    client.shutDown();
    isInitialized = false;
    executorService.shutdown();
  }
}
