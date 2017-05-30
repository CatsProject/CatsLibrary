package com.opensource.cats.catslibrary;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by C on 2017-05-25.
 */

public class CatsSendWearable extends Thread {
    private String path;
    private String message;
    private GoogleApiClient googleApiClient;


    CatsSendWearable(String p, String msg, GoogleApiClient g) {
        path = p;
        message = msg;
        googleApiClient = g;
    }

    public void run() {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, message.getBytes())
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    if (!sendMessageResult.getStatus().isSuccess()) {
                                        Log.e("TAG", "Failed to send message with status code: "
                                                + sendMessageResult.getStatus().getStatusCode());
                                    } else
                                        Log.v("TAG", "Connected Wearable");
                                }
                            });
                }
            }
        });
    }
}
