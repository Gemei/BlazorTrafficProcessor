package com.gdssecurity.handlers;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreationHandler;

public class BTPWebSocketCreationHandler implements ProxyWebSocketCreationHandler {

    private MontoyaApi _montoya;
    private Logging _logging;

    /**
     * Constructor for the websockets creation handler object
     * @param montoyaApi - an instance of the Burp Montoya APIs
     */
    public BTPWebSocketCreationHandler(MontoyaApi montoyaApi) {
        this._montoya = montoyaApi;
        this._logging = montoyaApi.logging();
    }

    /**
     * This function is called when a new websockets connections is created.
     * @param webSocketCreation The websockets connection.
     */
    @Override
    public void handleWebSocketCreation(ProxyWebSocketCreation webSocketCreation) {
        webSocketCreation.proxyWebSocket().registerProxyMessageHandler(new BTPWebSocketMessageHandler(this._montoya));
    }
}
