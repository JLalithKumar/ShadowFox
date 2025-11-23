import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class InventoryController {

    @FXML private TableView<InventoryItem> table;
    @FXML private TableColumn<InventoryItem, String> nameCol;
    @FXML private TableColumn<InventoryItem, String> barcodeCol;
    @FXML private TableColumn<InventoryItem, Double> priceCol;
    @FXML private TableColumn<InventoryItem, Integer> qtyCol;

    @FXML private TextField nameField;
    @FXML private TextField barcodeField;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;

    private ObservableList<InventoryItem> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        barcodeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBarcode()));
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));

        table.setItems(list);
    }

    @FXML
    public void addItem() {
        String name = nameField.getText();
        String barcode = barcodeField.getText();
        double price = Double.parseDouble(priceField.getText());
        int qty = Integer.parseInt(qtyField.getText());

        list.add(new InventoryItem(name, barcode, price, qty));

        clearFields();
    }

    @FXML
    public void updateItem() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item != null) {
            item.setName(nameField.getText());
            item.setBarcode(barcodeField.getText());
            item.setPrice(Double.parseDouble(priceField.getText()));
            item.setQuantity(Integer.parseInt(qtyField.getText()));
            table.refresh();
        }
    }

    @FXML
    public void deleteItem() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item != null) {
            list.remove(item);
        }
    }

    @FXML
    public void calculateStockValue() {
        double total = 0;
        for (InventoryItem item : list) {
            total += item.getPrice() * item.getQuantity();
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Total stock value = ₹" + total);
        alert.show();
    }

    private void clearFields() {
        nameField.clear();
        barcodeField.clear();
        priceField.clear();
        qtyField.clear();
    }
}
