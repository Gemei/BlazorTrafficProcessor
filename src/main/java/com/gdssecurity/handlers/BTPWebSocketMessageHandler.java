package com.gdssecurity.handlers;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.*;

import static burp.api.montoya.websocket.Direction.CLIENT_TO_SERVER;

public class BTPWebSocketMessageHandler implements ProxyMessageHandler {

    private MontoyaApi _montoya;
    private Logging _logging;

    /**
     * We are only interested in these Blazor functions, as they are susceptible to tampering
     */
    private enum BlazorFunctions {
        BeginInvokeDotNetFromJS,
        ReceiveJSDataChunk,
        ReceiveByteArray,
        StartCircuit,
        ConnectCircuit,
        RenderBatch
    }

    /**
     * Constructor for the websockets message handler object
     * @param montoyaApi - an instance of the Burp Montoya APIs
     */
    public BTPWebSocketMessageHandler(MontoyaApi montoyaApi) {
        this._montoya = montoyaApi;
        this._logging = montoyaApi.logging();
    }

    /**
     * Invoked when a text message is received from either the client or server. This gives the extension the ability to modify the message before it is processed by Burp.
     * Note: not utilized for this handler
     * @param interceptedTextMessage - An object holding the HTTP request right before it is sent
     * @return - an un-modified request
     */
    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        return TextMessageReceivedAction.continueWith(interceptedTextMessage);
    }

    /**
     * Invoked when a text message is about to be sent to either the client or server. This gives the extension the ability to modify the message before it is sent.
     * Note: not utilized for this handler
     * @param interceptedTextMessage - An object holding the HTTP request right before it is sent
     * @return - an un-modified request
     */
    @Override
    public TextMessageToBeSentAction handleTextMessageToBeSent(InterceptedTextMessage interceptedTextMessage) {
        return TextMessageToBeSentAction.continueWith(interceptedTextMessage);
    }

    /**
     * Handle the highlighting of messages when a binary message is received from either the client or server. This gives the extension the ability to modify the message before it is processed by Burp.
     * Note: only used for highlighting certain client to server requests, no processing logic present
     * @param interceptedBinaryMessage - An object holding the captured HTTP request
     * @return - the intercepted request with an added highlight for requests that use BlazorPack
     */
    @Override
    public BinaryMessageReceivedAction handleBinaryMessageReceived(InterceptedBinaryMessage interceptedBinaryMessage) {
        //TODO allow the user to change or add their own filters in the UI

        // If you want to highlight all blazor functions remove below if statements, and uncomment below line
        // interceptedBinaryMessage.annotations().setHighlightColor(HighlightColor.CYAN);
        //if (interceptedBinaryMessage.payload().length() >= 4 && interceptedBinaryMessage.direction().equals(CLIENT_TO_SERVER))
        if(interceptedBinaryMessage.payload().indexOf(BlazorFunctions.RenderBatch.name()) != -1) {
            interceptedBinaryMessage.annotations().setHighlightColor(HighlightColor.CYAN);
        }
        return BinaryMessageReceivedAction.continueWith(interceptedBinaryMessage);
    }

    /**
     * Handle the highlighting of messages when a binary message is about to be sent to either the client or server. This gives the extension the ability to modify the message before it is sent.
     * Note: only used for highlighting certain client to server requests, no processing logic present
     * @param interceptedBinaryMessage - An object holding the captured HTTP request
     * @return - the intercepted request with an added highlight for requests that use BlazorPack
     */
    @Override
    public BinaryMessageToBeSentAction handleBinaryMessageToBeSent(InterceptedBinaryMessage interceptedBinaryMessage) {
        //TODO allow the user to change or add their own filters in the UI

        // If you want to highlight all blazor functions remove below if statements, and uncomment below line
        // interceptedBinaryMessage.annotations().setHighlightColor(HighlightColor.CYAN);
        //if (interceptedBinaryMessage.payload().length() >= 4 && interceptedBinaryMessage.direction().equals(CLIENT_TO_SERVER))
        if(interceptedBinaryMessage.payload().indexOf(BlazorFunctions.BeginInvokeDotNetFromJS.name()) != -1 ||
                interceptedBinaryMessage.payload().indexOf(BlazorFunctions.ReceiveJSDataChunk.name()) != -1 ||
                interceptedBinaryMessage.payload().indexOf(BlazorFunctions.ReceiveByteArray.name()) != -1 ||
                interceptedBinaryMessage.payload().indexOf(BlazorFunctions.StartCircuit.name()) != -1 ||
                interceptedBinaryMessage.payload().indexOf(BlazorFunctions.ConnectCircuit.name()) != -1
        ){
            interceptedBinaryMessage.annotations().setHighlightColor(HighlightColor.CYAN);
        }
        return BinaryMessageToBeSentAction.continueWith(interceptedBinaryMessage);
    }
}
