package cn.gxust.ui;

import cn.gxust.ui.skin.MaskerPaneSkin;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;

public class MaskerPane extends Control {

    private String stylesheet;

    public MaskerPane() { getStyleClass().add("masker-pane"); } //$NON-NLS-1$

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1.0); //$NON-NLS-1$
    public final DoubleProperty progressProperty() { return progress; }
    public final double getProgress() { return progress.get(); }
    public final void setProgress(double progress) { this.progress.set(progress); }

    // -- Progress Node
    private final ObjectProperty<Node> progressNode = new SimpleObjectProperty<Node>() {
        {
            ProgressIndicator node = new ProgressIndicator();
            node.progressProperty().bind(progress);
            setValue(node);
        }

        @Override public String getName() { return "progressNode"; } //$NON-NLS-1$
        @Override public Object getBean() { return MaskerPane.this; }
    };
    public final ObjectProperty<Node> progressNodeProperty() { return progressNode; }
    public final Node getProgressNode() { return progressNode.get();}
    public final void setProgressNode(Node progressNode) { this.progressNode.set(progressNode); }

    // -- Progress Visibility
    private final BooleanProperty progressVisible = new SimpleBooleanProperty(this, "progressVisible", true); //$NON-NLS-1$
    public final BooleanProperty progressVisibleProperty() { return progressVisible; }
    public final boolean getProgressVisible() { return progressVisible.get(); }
    public final void setProgressVisible(boolean progressVisible) { this.progressVisible.set(progressVisible); }

    // -- Text
    private final StringProperty text = new SimpleStringProperty(this, "text", "Please Wait..."); //$NON-NLS-1$
    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String text) { this.text.set(text); }

    @Override protected Skin<?> createDefaultSkin() { return new MaskerPaneSkin(this); }

    @Override  public String getUserAgentStylesheet() { return getUserAgentStylesheet(MaskerPane.class, "/css/maskerpane.css"); } //$NON-NLS-1$

    protected final String getUserAgentStylesheet(Class<?> clazz, String fileName) {

        if (stylesheet == null) {
            stylesheet = clazz.getResource(fileName).toExternalForm();
        }

        return stylesheet;
    }
}