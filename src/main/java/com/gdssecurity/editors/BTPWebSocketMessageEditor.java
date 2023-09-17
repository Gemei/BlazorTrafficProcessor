package com.gdssecurity.editors;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.WebSocketMessage;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorMode;
import com.gdssecurity.MessageModel.GenericMessage;
import com.gdssecurity.helpers.ArraySliceHelper;
import com.gdssecurity.helpers.BTPConstants;
import com.gdssecurity.helpers.BlazorHelper;
import org.json.JSONArray;
import org.json.JSONException;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Class to implement the "BTP" editor tab for WebSocket messages
 */
public class BTPWebSocketMessageEditor implements ExtensionProvidedWebSocketMessageEditor {

    private MontoyaApi _montoya;
    private ByteArray websocketByteArray;
    private RawEditor editor;
    private BlazorHelper blazorHelper;
    private Logging logging;

    /**
     * Constructs a new BTPWebSocketMessageEditor object
     * @param api - an instance of the Montoya API
     * @param editorMode - options for the editor object
     */
    public BTPWebSocketMessageEditor(MontoyaApi api, EditorMode editorMode) {
        this._montoya = api;
        this.editor = this._montoya.userInterface().createRawEditor();
        this.blazorHelper = new BlazorHelper(this._montoya);
        this.logging = this._montoya.logging();
    }

    /**
     * Converts a JSON message to BlazorPack, called when the "Raw" tab is clicked
     * Just return the existing message body if editor not modified, re-serialize if editor is modified
     * @return - an ByteArray object containing the BlazorPacked WebSocket message
     */
    @Override
    public ByteArray getMessage() {
        byte[] body;
        if (this.editor.isModified()) {
            int bodyOffset = this.blazorHelper.getBodyOffset(this.editor.getContents().getBytes());
            body = ArraySliceHelper.getArraySlice(this.editor.getContents().getBytes(), bodyOffset, this.editor.getContents().length());
        } else {
            body = this.websocketByteArray.getBytes();
        }
        if (body == null | body.length == 0) {
            this.logging.logToError("[-] getRequest: The selected editor body is empty/null.");
            return null;
        }
        JSONArray messages;
        byte[] newBody;
        try {
            messages = new JSONArray(new String(body));
            newBody = this.blazorHelper.blazorPack(messages);
        } catch (JSONException e) {
            this.logging.logToError("[-] getRequest - JSONExcpetion while parsing JSON array: " + e.getMessage());
            return null;
        } catch (Exception e) {
            this.logging.logToError("[-] getRequest - Unexpected exception while getting the request: " + e.getMessage());
            return null;
        }
        /* TO-DO FIX ISSUE IN MODIFYING MESSAGES */
        this.websocketByteArray = ByteArray.byteArray(newBody);
        return this.websocketByteArray;
    }

    /**
     * Converts a given BlazorPack message to JSON, called when the "BTP" tab is clicked
     * @param message - The message to deserialize from BlazorPack to JSON
     */
    @Override
    public void setMessage(WebSocketMessage message) {
        this.websocketByteArray = ByteArray.byteArray(message.payload().getBytes());
        byte[] body = message.payload().getBytes();
        ArrayList<GenericMessage> messages = this.blazorHelper.blazorUnpack(body);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        try {
            String jsonStrMessages = this.blazorHelper.messageArrayToString(messages);
            outstream.write(jsonStrMessages.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.logging.logToError("[-] setRequestResponse - IOException while writing bytes to buffer: " + e.getMessage());
            return;
        } catch (JSONException e) {
            this.logging.logToError("[-] setRequestResponse - JSONException while parsing JSON array: " + e.getMessage());
            return;
        } catch (Exception e) {
            this.logging.logToError("[-] setRequestResponse - Unexpected exception: " + e.getMessage());
            return;
        }
        this.websocketByteArray = ByteArray.byteArray(outstream.toString().getBytes());
        this.editor.setContents(websocketByteArray);
    }

    /**
     * Checks to see if the "BTP" tab should appear on a given websockets message
     * @param message - the HTTP request/response pair object to check.
     * @return true if it should be enabled, false otherwise
     */
    @Override
    public boolean isEnabledFor(WebSocketMessage message) {
        if (message == null || message.payload() == null || message.payload().length() <= 4) {
            return false;
        }
        return true;
    }

    /**
     * Gets the caption for the editor tab
     * @return "BTP" - BlazorTrafficProcessor
     */
    @Override
    public String caption() {
        return BTPConstants.CAPTION;
    }

    /**
     * Gets the UI component for the editor tab
     * @return the editor's UI component
     */
    @Override
    public Component uiComponent() {
        return this.editor.uiComponent();
    }

    /**
     * Get the selected data within the editor
     * @return the editor's selection object
     */
    @Override
    public Selection selectedData() {
        return this.editor.selection().get();
    }

    /**
     * Check if the editor has been modified. If not, the getHttpRequest function is not called.
     * @return true if modified, false otherwise
     */
    @Override
    public boolean isModified() {
        return this.editor.isModified();
    }
}
