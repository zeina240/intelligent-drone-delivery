package org.ai.intelligent_delivery_drone_planner.main_gui.builders;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;
import org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI;

import java.text.DecimalFormat;
import java.util.List;

import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.*;

public class TablesBuilder {
	private static final DecimalFormat df = new DecimalFormat("0.00");
	
	public static TableView<City> buildCityTable() {
		TableView<City> cityTable = new TableView<>();
		cityTable.setId("city-table");
		cityTable.setPrefHeight(300);
		
		TableColumn<City, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
		nameCol.setReorderable(false);
		
		TableColumn<City, Double> xCol = createDoubleColumn("X", City::getX);
		TableColumn<City, Double> yCol = createDoubleColumn("Y", City::getY);
		TableColumn<City, Double> tempCol = createDoubleColumn("Temp", City::getTemp);
		TableColumn<City, Double> humidityCol = createDoubleColumn("Humidity", City::getHumidity);
		TableColumn<City, Double> windCol = createDoubleColumn("Wind", City::getWindSpeed);
		
		nameCol.setSortable(false);
		xCol.setSortable(false);
		yCol.setSortable(false);
		tempCol.setSortable(false);
		humidityCol.setSortable(false);
		humidityCol.setSortable(false);
		
		xCol.getStyleClass().add("numeric");
		yCol.getStyleClass().add("numeric");
		tempCol.getStyleClass().add("numeric");
		humidityCol.getStyleClass().add("numeric");
		windCol.getStyleClass().add("numeric");
		
		TableColumn<City, String> safeCol = new TableColumn<>("Safe");
		safeCol.setCellValueFactory(data -> {
			int status = data.getValue().getSafeToFlyValue();
			String label = switch (status) {
				case 0 -> "Safe";
				case 1 -> "Unsafe";
				default -> "N/A";
			};
			return new SimpleStringProperty(label);
		});
		
		cityTable.getColumns().addAll(List.of(
				nameCol, xCol, yCol, tempCol, humidityCol, windCol, safeCol
		));
		cityTable.setItems(FXCollections.observableArrayList(getCities()));
		return cityTable;
	}
	
	private static TableColumn<City, Double> createDoubleColumn(String title, Callback<City, Double> getter) {
		TableColumn<City, Double> col = new TableColumn<>(title);
		col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.call(data.getValue())));
		col.setCellFactory(_ -> new TableCell<>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				if (empty || value == null) {
					setText(null);
				} else {
					setText(df.format(value));
				}
			}
		});
		return col;
	}
	
	public static void updateCitiesTable() {
		Platform.runLater(() -> citiesTable.getItems().setAll(getCities()));
	}
	
	public static TableView<String> buildTrackingTable() {
		TableView<String> trackingTable = new TableView<>();
		trackingTable.setId("tracking-table");
		trackingTable.setPrefHeight(300);
		
		TableColumn<String, String> trackingColumn = new TableColumn<>("Tracking Info");
		trackingColumn.getStyleClass().add("table-column");
		trackingColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
		trackingColumn.prefWidthProperty().bind(trackingTable.widthProperty());
		trackingColumn.setReorderable(false);
		trackingColumn.setSortable(false);
		trackingTable.getColumns().add(trackingColumn);
		trackingTable.getItems().addAll(MainGUI.tracking);
		return trackingTable;
	}
	
	public static void updateTrackingTable() {
		Platform.runLater(() -> trackingTable.getItems().setAll(MainGUI.tracking));
	}
}
