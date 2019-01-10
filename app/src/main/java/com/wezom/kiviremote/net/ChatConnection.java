/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.wezom.kiviremote.net;

import android.os.StrictMode;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.GotAspectEvent;
import com.wezom.kiviremote.bus.ReconnectEvent;
import com.wezom.kiviremote.common.Action;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.gson.ListAdapter;
import com.wezom.kiviremote.net.model.ConnectionMessage;
import com.wezom.kiviremote.net.model.OpenSettings;
import com.wezom.kiviremote.net.model.ServerEvent;
import com.wezom.kiviremote.net.model.SocketConnectionModel;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChatConnection {
    private static final String OPEN_SETTINGS = "OPEN_SETTINGS";
    private static final String KEYBOARD_NOT_SET = "KEYBOARD_NOT_SET";
    private static final String SHOW_KEYBOARD = "SHOW_KEYBOARD";
    private static final String HIDE_KEYBOARD = "HIDE_KEYBOARD";
    private static final String VOLUME = "VOLUME";
    private static final String PONG = "PONG";
    private static final String DISCONNECT = "DISCONNECT";

    private ChatServer mChatServer;
    private ChatClient mChatClient;
    private Gson gson;
    private Socket mSocket;
    private int mPort = -1;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public ChatConnection() {
        mChatServer = new ChatServer();
        gson = new GsonBuilder().registerTypeHierarchyAdapter(List.class, new ListAdapter()).create();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void tearDown() {
        if (mChatClient != null) {
            mChatClient.tearDown();
        }

        if (mChatServer != null) {
            mChatServer.tearDown();
        }
    }

    public void dispose() {
        if (mChatClient != null)
            mChatClient.dispose();
        tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(SocketConnectionModel msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }

    public void openSettings() {
        if (mChatClient != null) {
            mChatClient.sendMessage(new OpenSettings(OPEN_SETTINGS));
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    private void setLocalPort(int port) {
        mPort = port;
    }

    private void updateMessages(String msg) {
        if (msg.length() > 150)
            Timber.d("Updating message: " + msg.substring(0, 50));
        else
        Timber.d("Updating message: " + msg);


        ServerEvent serverEvent = gson.fromJson(msg, ServerEvent.class);
        boolean keyboardNotSet = false;
        boolean showKeyboard = false;
        boolean hideKeyboard = false;
        int volume = -1;

        if (serverEvent != null) {
            if (serverEvent.getEvent() != null)
                switch (serverEvent.getEvent()) {
                    case KEYBOARD_NOT_SET:
                        keyboardNotSet = true;
                        break;
                    case SHOW_KEYBOARD:
                        showKeyboard = true;
                        break;
                    case HIDE_KEYBOARD:
                        hideKeyboard = true;
                        break;
                    case VOLUME:
                        volume = serverEvent.getVolume();
                        break;
                    default:
                        Timber.d("Unknown event has been received");
                        return;
                }


            if (serverEvent.getAspectMessage() != null && serverEvent.getAvailableAspectValues() != null) {
                AspectHolder.INSTANCE.setAvailableSettings(serverEvent.getAvailableAspectValues());
                AspectHolder.INSTANCE.setMessage(serverEvent.getAspectMessage());
                RxBus.INSTANCE.publish(new GotAspectEvent(serverEvent.getAspectMessage(), serverEvent.getAvailableAspectValues()));
            } else
            RxBus.INSTANCE.publish(new ConnectionMessage(msg,
                    !keyboardNotSet,
                    serverEvent.getApps(),
                    showKeyboard,
                    hideKeyboard,
                    volume,
                    TextUtils.equals(serverEvent.getEvent(), DISCONNECT)));
        } else Timber.d("Server event is null");
    }

    private synchronized void setSocket(Socket socket) {
        Timber.d("setSocket being called.");
        if (socket == null) {
            Timber.d("Setting a null socket.");
        }
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Timber.e(e);
            }
        }

        mSocket = socket;
    }

    private synchronized Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;

        ChatServer() {
            disposables.add(new ServerThread()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }

        public void tearDown() {
            if (!disposables.isDisposed()) {
                disposables.dispose();
            }

            try {
                if (mServerSocket != null)
                    mServerSocket.close();
            } catch (IOException e) {
                Timber.e(e, "Error when closing server socket.");
            }
        }

        class ServerThread extends Observable {

            @Override
            protected void subscribeActual(Observer observer) {
                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an isAvailable one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());

                    while (!Thread.currentThread().isInterrupted()) {
                        Timber.d("ServerSocket Created, awaiting connection");
                        try {
                            setSocket(mServerSocket.accept());
                        } catch (SocketException e) {
                            mServerSocket = new ServerSocket(0);
                            setLocalPort(mServerSocket.getLocalPort());
                        }

                        Timber.d("Connected.");
                        if (mChatClient == null) {
                            if (getSocket() != null) {
                                int port = getSocket().getPort();
                                InetAddress address = getSocket().getInetAddress();
                                connectToServer(address, port);
                            } else {
                                mServerSocket = new ServerSocket(0);
                                setLocalPort(mServerSocket.getLocalPort());

                                if (getSocket() != null) {
                                    int port = getSocket().getPort();
                                    InetAddress address = getSocket().getInetAddress();
                                    connectToServer(address, port);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Timber.e(e, "Error creating ServerSocket: ", e);
                    Timber.e(e, e.getMessage());
                }
            }
        }
    }

    private class ChatClient {
        private final InetAddress mAddress;
        private final int port;
        private PrintWriter out;
        private Disposable pingTimer;
        private int previousState = -1;
        private int currentState = -1;

        private PrintWriter pingStream;
        private int pingBuffer = 0;

        ChatClient(InetAddress address, int port) {

            Timber.d("Creating chatClient");
            this.mAddress = address;
            this.port = port;

            disposables.add(new SendingThread()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe());
        }

        public void dispose() {
            Timber.d("Disposing");
            if (pingTimer != null && !pingTimer.isDisposed())
                pingTimer.dispose();
        }

        void pingServer() throws IOException {
            if (currentState != -1) {
                previousState = currentState;
            }

            currentState = mAddress.isReachable(5000) ? 1 : 0;
            Timber.d("Is reachable: " + (currentState == 1));

            switch (currentState) {
                case 1:
                    if (previousState == 0) {
                        RxBus.INSTANCE.publish(new ReconnectEvent());
                        Timber.d("Reconnecting");
                        dispose();
                        return;
                    }
                    RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(true));
                    break;
                case 0:
                    RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(false));
                    break;
                default:
                    break;
            }
        }

        void sendPing() throws IOException {
            if (pingStream == null)
                pingStream = new PrintWriter(new OutputStreamWriter(getSocket().getOutputStream()));
            pingStream.println();
            if (pingStream.checkError()) {
                pingBuffer++;
                Timber.d("Ping failed without exception");
            } else {
                pingBuffer = 0;
                if (currentState != -1) {
                    previousState = currentState;
                }
            }

            if (pingBuffer > 1) {
                Timber.d("Error threshold has been reached, disposing");
                RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(false));
                dispose();
            }
        }

        void sendMessage(Object model) {
            String msg = gson.toJson(model);
            try {
                Socket socket = getSocket();
                if (socket != null && !socket.isClosed()) {
                    if (out == null)
                        out = new PrintWriter(new OutputStreamWriter(getSocket().getOutputStream()));
                    out.println(msg);
                    out.flush();
                }
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
            }
            Timber.d("Client sent message: " + msg);
        }

        public void tearDown() {
            try {
                if (getSocket() != null)
                    getSocket().close();
            } catch (Exception ioe) {
                Timber.e(ioe, "Error when closing server socket: " + ioe.getMessage());
            }
        }

        class SendingThread extends Observable {
            @Override
            protected void subscribeActual(Observer observer) {
                try {
                    setSocket(new Socket(mAddress, port));
                    Timber.d("Client-side socket initialized.");

                    pingTimer = Observable
                            .interval(3, TimeUnit.SECONDS)
                            .subscribe(t -> {
                                        Timber.d("Client sent ping");
                                        pingServer();
                                        sendPing();
                                    },
                                    e -> {
                                        Timber.e(e, "Ping failed: " + e.getMessage());
                                        RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(false));
                                        if (!pingTimer.isDisposed())
                                            pingTimer.dispose();
                                        Crashlytics.logException(e);
                                    });

                    disposables.add(new ReceivingThread()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe());

                    sendMessage(new SocketConnectionModel().setAction(Action.REQUEST_APPS));

                } catch (UnknownHostException e) {
                    Timber.d("Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Timber.e(e, "Initializing socket failed: " + e.getMessage());
                    dispose();
                    RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(false));
                    Crashlytics.logException(e);
                }
            }
        }

        class ReceivingThread extends Observable {
            @Override
            protected void subscribeActual(Observer observer) {
                Timber.d("mSocket status: " + (getSocket() != null) + " " + getSocket().isClosed());
                try (BufferedReader input = new BufferedReader(new InputStreamReader(
                        getSocket().getInputStream()))) {

                    while (!Thread.currentThread().isInterrupted()) {
                        String messageStr;
                        while ((messageStr = input.readLine()) != null) {
                            if (messageStr.length() > 50)
                                Timber.d("Read from server: " + messageStr.substring(0, 50));
                            else
                                Timber.d("Read from server: " + messageStr);
                            updateMessages(messageStr);
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e, "Server loop error: " + e.getMessage());
                }
            }
        }
    }
}
