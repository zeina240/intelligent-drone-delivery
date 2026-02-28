package org.ai.intelligent_delivery_drone_planner.main_gui.popups;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.initialCostLabel;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.optimizedCostLabel;

public class CostsPopup {
	private static Popup popup;
	
	public static void showCosts(BorderPane root, Stage stage) {
		if (popup != null && popup.isShowing()) {
			popup.hide();
		}
		
		initialCostLabel = new Label("");
		optimizedCostLabel = new Label("");
		initialCostLabel.setId("dash-cost-label");
		optimizedCostLabel.setId("dash-cost-label");
		
		HBox costLabelsBox = new HBox(20);
		costLabelsBox.setAlignment(Pos.CENTER_LEFT);
		costLabelsBox.setStyle("-fx-background-color: #ffffffee; "
				+ "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");
		costLabelsBox.getChildren().addAll(initialCostLabel, optimizedCostLabel);
		
		popup = new Popup();
		popup.getContent().add(costLabelsBox);
		popup.setAutoHide(false);
		
		Runnable showAgain = () -> {
			if (stage.isShowing() && !stage.isIconified()) {
				Bounds bounds = root.localToScreen(root.getBoundsInLocal());
				double x = bounds.getMinX() + 800;
				double y = bounds.getMinY() + 3;
				popup.show(stage, x, y);
			}
		};
		
		Platform.runLater(showAgain);
		stage.iconifiedProperty().addListener((_, _, isNowMinimized) -> {
			if (!isNowMinimized) {
				Platform.runLater(showAgain);
			} else {
				popup.hide();
			}
		});
		
		stage.showingProperty().addListener((_, _, isNowShowing) -> {
			if (isNowShowing) {
				Platform.runLater(showAgain);
			} else {
				popup.hide();
			}
		});
	}
}