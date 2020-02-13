package com.kivi.remote.upnp.fi.iki.elonen;

import java.io.IOException;

import timber.log.Timber;

public class ServerRunner {
    public static void run(Class serverClass) {
        try {
            executeInstance((NanoHTTPD) serverClass.newInstance());
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        } catch (IOException ioe) {
            System.exit(-1);
        }

        Timber.d("Server started, Hit Enter to stop.\n");

        try {
            System.in.read();
        } catch (Throwable ignored) {
        }

        server.stop();
        Timber.d("Server stopped.\n");
    }
}
