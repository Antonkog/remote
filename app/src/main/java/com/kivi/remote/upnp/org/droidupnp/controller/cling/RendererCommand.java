/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kivi.remote.upnp.org.droidupnp.controller.cling;

import com.kivi.remote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;
import com.kivi.remote.upnp.org.droidupnp.model.cling.RendererState;
import com.kivi.remote.upnp.org.droidupnp.model.cling.TrackMetadata;
import com.kivi.remote.upnp.org.droidupnp.model.cling.didl.ClingDIDLItem;
import com.kivi.remote.upnp.org.droidupnp.model.upnp.IRendererCommand;
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.PlaylistItem;
import org.fourthline.cling.support.model.item.TextItem;
import org.fourthline.cling.support.model.item.VideoItem;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable, IRendererCommand {

    private static final String TAG = "RendererCommand";

    private final RendererState rendererState;
    private final ControlPoint controlPoint;

    public Thread thread;
    boolean pause = false;
    private final IUPnPServiceController controller;

    public RendererCommand(IUPnPServiceController controller, ControlPoint controlPoint, RendererState rendererState) {
        this.controller = controller;
        this.rendererState = rendererState;
        this.controlPoint = controlPoint;

        thread = new Thread(this);
        pause = true;
    }

    @Override
    public void finalize() {
        this.pause();
    }

    @Override
    public void pause() {
        Timber.v("Interrupt");
        pause = true;
        thread.interrupt();
    }

    @Override
    public void resume() {
        Timber.v("Resume");
        pause = false;
        if (!thread.isAlive())
            thread.start();
        else
            thread.interrupt();
    }

    public static Service getRenderingControlService(IUPnPServiceController controller) {
        if (controller.getSelectedRenderer() == null)
            return null;

        return controller.getSelectedRenderer().getDevice().findService(
                new UDAServiceType("RenderingControl"));
    }

    public static Service getAVTransportService(IUPnPServiceController controller) {
        if (controller.getSelectedRenderer() == null)
            return null;

        return controller.getSelectedRenderer().getDevice().findService(
                new UDAServiceType("AVTransport"));
    }

    @Override
    public void commandPlay() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new Play(service) {
            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success playing ! ");

                Timber.d("InstanceID: " + invocation.getInput("InstanceID"));
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to play ! " + arg2);
            }
        });
    }

    @Override
    public void commandStop() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new Stop(service) {
            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success stopping ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to stop ! " + arg2);
            }
        });
    }

    @Override
    public void commandPause() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new Pause(service) {
            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success pausing ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to pause ! " + arg2);
            }
        });
    }

    @Override
    public void commandToggle() {
        RendererState.State state = rendererState.getState();
        if (state == RendererState.State.PLAY) {
            commandPause();
        } else {
            commandPlay();
        }
    }

    @Override
    public void commandSeek(String relativeTimeTarget) {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new Seek(service, relativeTimeTarget) {
            // TODO fix it, what is relativeTimeTarget ? :)

            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success seeking !");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to seek ! " + arg2);
            }
        });
    }

    @Override
    public void setVolume(final int volume) {
        Service service = getRenderingControlService(controller);
        if (service == null)
            return;

        controlPoint.execute(new SetVolume(service, volume) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Timber.v("Success to set mute");
                rendererState.setVolume(volume);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to set mute ! " + arg2);
            }
        });
    }

    @Override
    public void setMute(final boolean mute) {
        Service service = getRenderingControlService(controller);
        if (service == null)
            return;


        controlPoint.execute(new SetMute(service, mute) {
            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success setting mute status ! ");
                rendererState.setMute(mute);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to set mute status ! " + arg2);
            }
        });
    }

    @Override
    public void toggleMute() {
        setMute(!rendererState.isMute());
    }

    public void setURI(String uri, TrackMetadata trackMetadata) {
        Timber.i("Set uri to " + uri);
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new SetAVTransportURI(service, uri, trackMetadata.getXML()) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Timber.i("URI has been successfully set!");
                Timber.d("URI metadata: " + trackMetadata.getXML());
                commandPlay();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to set URI ! " + arg2);
            }
        });
    }

    @Override
    public void launchItem(final IDIDLItem item) {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        DIDLObject obj = ((ClingDIDLItem) item).getObject();
        if (!(obj instanceof Item))
            return;

        Item upnpItem = (Item) obj;

        String type = "";
        if (upnpItem instanceof AudioItem)
            type = "audioItem";
        else if (upnpItem instanceof VideoItem)
            type = "videoItem";
        else if (upnpItem instanceof ImageItem)
            type = "imageItem";
        else if (upnpItem instanceof PlaylistItem)
            type = "playlistItem";
        else if (upnpItem instanceof TextItem)
            type = "textItem";

        // TODO genre && artURI
        final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getTitle(),
                upnpItem.getCreator(), "", "", upnpItem.getFirstResource().getValue(),
                "object.item." + type);

        Timber.i("TrackMetadata : " + trackMetadata.toString());

        // Stop playback before setting URI
        controlPoint.execute(new Stop(service) {
            @Override
            public void success(ActionInvocation invocation) {
                Timber.v("Success stopping ! ");
                callback();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to stop ! " + arg2);
                callback();
            }

            public void callback() {
                setURI(item.getURI(), trackMetadata);
            }
        });

    }

    // Update

    public void updateMediaInfo() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new GetMediaInfo(service) {
            @Override
            public void received(ActionInvocation arg0, MediaInfo arg1) {
                Timber.d("Receive media info ! " + arg1);
                rendererState.setMediaInfo(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to get media info ! " + arg2);
            }
        });
    }

    public void updatePositionInfo() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new GetPositionInfo(service) {
            @Override
            public void received(ActionInvocation arg0, PositionInfo arg1) {
                Timber.d("Received position info: " + arg1);
                rendererState.setPositionInfo(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Failed to get position info: " + arg2);
            }
        });
    }

    public void updateTransportInfo() {
        Service service = getAVTransportService(controller);
        if (service == null)
            return;

        controlPoint.execute(new GetTransportInfo(service) {
            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to get position info ! " + arg2);
            }

            @Override
            public void received(ActionInvocation arg0, TransportInfo arg1) {
                Timber.d("Receive position info ! " + arg1);
                rendererState.setTransportInfo(arg1);
            }
        });
    }

    @Override
    public void updateVolume() {
        Service service = getRenderingControlService(controller);
        if (service == null)
            return;

        controlPoint.execute(new GetVolume(service) {
            @Override
            public void received(ActionInvocation arg0, int arg1) {
                Timber.d("Receive mute ! " + arg1);
                rendererState.setVolume(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to get mute ! " + arg2);
            }
        });
    }

    void updateMute() {
        Service service = getRenderingControlService(controller);
        if (service == null)
            return;


        controlPoint.execute(new GetMute(service) {
            @Override
            public void received(ActionInvocation arg0, boolean arg1) {
                Timber.d("Receive mute status ! " + arg1);
                rendererState.setMute(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                Timber.w("Fail to get mute status ! " + arg2);
            }
        });
    }

    @Override
    public void updateFull() {
        updateMediaInfo();
        updatePositionInfo();
        updateVolume();
        updateMute();
        updateTransportInfo();
    }

    @Override
    public void run() {
        // LastChange lastChange = new LastChange(new AVTransportLastChangeParser(),
        // AVTransportVariable.CurrentTrackMetaData.class);

        // SubscriptionCallback callback = new SubscriptionCallback(getRenderingControlService(), 600) {
        //
        // @Override
        // public void established(GENASubscription sub)
        // {
        // Timber.e(e, "Established: " + sub.getSubscriptionId());
        // }
        //
        // @Override
        // public void failed(GENASubscription sub, UpnpResponse response, Exception ex, String msg)
        // {
        // Timber.e(e, createDefaultFailureMessage(response, ex));
        // }
        //
        // @Override
        // public void ended(GENASubscription sub, CancelReason reason, UpnpResponse response)
        // {
        // // Reason should be null, or it didn't end regularly
        // }
        //
        // @Override
        // public void eventReceived(GENASubscription sub)
        // {
        // Timber.e(e, "Event: " + sub.getCurrentSequence().getValue());
        // Map<String, StateVariableValue> values = sub.getCurrentValues();
        // StateVariableValue status = values.get("Status");
        // if (status != null)
        // Timber.e(e, "Status is: " + status.toString());
        // }
        //
        // @Override
        // public void eventsMissed(GENASubscription sub, int numberOfMissedEvents)
        // {
        // Timber.e(e, "Missed events: " + numberOfMissedEvents);
        // }
        // };

        // controlPoint.execute(callback);

        while (true)
            try {
                int count = 0;
                while (true) {
                    if (!pause) {
                        count++;
                        updatePositionInfo();

                        if ((count % 3) == 0) {
                            updateVolume();
                            updateMute();
                            updateTransportInfo();
                        }

                        if ((count % 6) == 0) {
                            updateMediaInfo();
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Timber.i("State updater interrupt, new state " + ((pause) ? "pause" : "running"));
            }
    }

    @Override
    public void updateStatus() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePosition() {
        // TODO Auto-generated method stub

    }
}
