import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class StudentInfoSystem extends Application {

    private TableView<Student> table;
    private ObservableList<Student> data;

    private TextField idField, nameField, marksField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Student Info System");

        // --- Header ---
        Label header = new Label("Student Info System");
        header.getStyleClass().add("header");

        // --- Form Inputs ---
        idField = new TextField();
        idField.setPromptText("ID");
        nameField = new TextField();
        nameField.setPromptText("Name");
        marksField = new TextField();
        marksField.setPromptText("Marks");

        Button addButton = new Button("Add");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> addStudent());
        updateButton.setOnAction(e -> updateStudent());
        deleteButton.setOnAction(e -> deleteStudent());

        HBox form = new HBox(10, idField, nameField, marksField, addButton, updateButton, deleteButton);
        form.setPadding(new Insets(20));

        // --- Table ---
        table = new TableView<>();
        data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, Double> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));

        TableColumn<Student, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        table.getColumns().addAll(idCol, nameCol, marksCol, gradeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setOnMouseClicked(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if(selected != null){
                idField.setText(selected.getId());
                nameField.setText(selected.getName());
                marksField.setText(String.valueOf(selected.getMarks()));
            }
        });

        VBox root = new VBox(10, header, form, table);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1000, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- CRUD Methods ---
    private void addStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String marksText = marksField.getText().trim();

        if(id.isEmpty() || name.isEmpty() || marksText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields!");
            return;
        }

        try {
            double marks = Double.parseDouble(marksText);
            String grade = calculateGrade(marks);

            data.add(new Student(id, name, marks, grade));

            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Student added successfully!");
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Marks must be a number!");
        }
    }

    private void updateStudent() {
        Student selected = table.getSelectionModel().getSelectedItem();
        if(selected == null){
            showAlert(Alert.AlertType.ERROR, "Please select a student to update!");
            return;
        }

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String marksText = marksField.getText().trim();

        if(id.isEmpty() || name.isEmpty() || marksText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields!");
            return;
        }

        try {
            double marks = Double.parseDouble(marksText);
            String grade = calculateGrade(marks);

            selected.setId(id);
            selected.setName(name);
            selected.setMarks(marks);
            selected.setGrade(grade);

            table.refresh();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Student updated successfully!");
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Marks must be a number!");
        }
    }

    private void deleteStudent() {
        Student selected = table.getSelectionModel().getSelectedItem();
        if(selected == null){
            showAlert(Alert.AlertType.ERROR, "Please select a student to delete!");
            return;
        }
        data.remove(selected);
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Student deleted successfully!");
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        marksField.clear();
    }

    private String calculateGrade(double marks) {
        if(marks >= 90) return "A";
        else if(marks >= 75) return "B";
        else if(marks >= 60) return "C";
        else if(marks >= 50) return "D";
        else return "F";
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // --- Student class ---
    public static class Student {
        private final javafx.beans.property.SimpleStringProperty id;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleDoubleProperty marks;
        private final javafx.beans.property.SimpleStringProperty grade;

        public Student(String id, String name, double marks, String grade) {
            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.marks = new javafx.beans.property.SimpleDoubleProperty(marks);
            this.grade = new javafx.beans.property.SimpleStringProperty(grade);
        }

        public String getId() { return id.get(); }
        public String getName() { return name.get(); }
        public double getMarks() { return marks.get(); }
        public String getGrade() { return grade.get(); }

        public void setId(String id) { this.id.set(id); }
        public void setName(String name) { this.name.set(name); }
        public void setMarks(double marks) { this.marks.set(marks); }
        public void setGrade(String grade) { this.grade.set(grade); }

        public javafx.beans.property.StringProperty idProperty() { return id; }
        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public javafx.beans.property.DoubleProperty marksProperty() { return marks; }
        public javafx.beans.property.StringProperty gradeProperty() { return grade; }
    }
}
