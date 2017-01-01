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

package com.mocket.server;

import com.mocket.core.LatLong;
import com.mocket.core.LatLangStreamHandler;
import com.network.mocket.MocketException;
import com.network.mocket.builder.server.Server;
import com.network.mocket.builder.server.ServerBuilder;
import com.network.mocket.helper.Pair;

import java.net.SocketAddress;

public class MocketServer {
  private final static int serverPort = 8080;
  private static Server<LatLong> server;

  public static void main(String... args) throws MocketException {
    server = new ServerBuilder<>()
      .port(serverPort)
      .addHandler(new LatLangStreamHandler())
      .build();
    try {
      while (true) {
        Pair<SocketAddress, LatLong> readSocketAddressPair = server.read();
        if (readSocketAddressPair != null) {
          System.out.println(
            "Got location from: " +
              readSocketAddressPair.getFirst() +
              ", as: " +
              readSocketAddressPair.getSecond());
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
}
