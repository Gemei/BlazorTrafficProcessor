package com.gdssecurity.providers;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;
import burp.api.montoya.ui.editor.extension.WebSocketMessageEditorProvider;
import com.gdssecurity.editors.BTPWebSocketMessageEditor;

public class BTPWebSocketMessageEditorProvider implements WebSocketMessageEditorProvider {

    private MontoyaApi _montoya;

    /**
     * Construct a BTPWebSocketMessageEditorProvider
     * @param api - an instance of the Montoya API
     */
    public BTPWebSocketMessageEditorProvider(MontoyaApi api) {
        this._montoya = api;
    }

    /**
     * Returns a newly created ExtensionProvidedWebSocketMessageEditor for each BlazorPack message.
     * @param creationContext          What mode the created editor should implement.
     * @return the newly created editor object
     */
    @Override
    public ExtensionProvidedWebSocketMessageEditor provideMessageEditor(EditorCreationContext creationContext) {
        return new BTPWebSocketMessageEditor(this._montoya, creationContext.editorMode());
    }
}
