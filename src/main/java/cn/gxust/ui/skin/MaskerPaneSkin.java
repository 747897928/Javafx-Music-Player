package cn.gxust.ui.skin;

import cn.gxust.ui.MaskerPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MaskerPaneSkin extends SkinBase<MaskerPane> {

    public MaskerPaneSkin(MaskerPane maskerPane) {
        super(maskerPane);
        getChildren().add(createMasker(maskerPane));
    }

    private StackPane createMasker(MaskerPane maskerPane) {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        vBox.getStyleClass().add("masker-center"); //$NON-NLS-1$

        vBox.getChildren().add(createLabel());
        vBox.getChildren().add(createProgressIndicator());

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(vBox);

        StackPane glass = new StackPane();
        glass.setAlignment(Pos.CENTER);
        glass.getStyleClass().add("masker-glass"); //$NON-NLS-1$
        glass.getChildren().add(hBox);

        return glass;
    }

    private Label createLabel() {
        Label text = new Label();
        text.textProperty().bind(getSkinnable().textProperty());
        text.getStyleClass().add("masker-text"); //$NON-NLS-1$
        return text;
    }

    private Label createProgressIndicator() {
        Label graphic = new Label();
        graphic.setGraphic(getSkinnable().getProgressNode());
        graphic.visibleProperty().bind(getSkinnable().progressVisibleProperty());
        graphic.getStyleClass().add("masker-graphic"); //$NON-NLS-1$
        return graphic;
    }
}

