package ve.edu.ucab.mazerunnerfx;

 import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ve.edu.ucab.mazerunnerfx.models.Statistics;
import ve.edu.ucab.mazerunnerfx.models.Statistics.GameRecord;

import java.io.IOException;
import java.util.List;

/**
 * Controlador para la vista de estadísticas.
 *
 * <p>Muestra el historial de partidas del jugador y permite navegar de regreso
 * al menú principal.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class EstadisticasController {

    @FXML
    private Label usuarioLabel;

    @FXML
    private TableView<GameRecord> recordsTable;

    @FXML
    private TableColumn<GameRecord, String> dateCol;

    @FXML
    private TableColumn<GameRecord, String> durationCol;

    @FXML
    private TableColumn<GameRecord, Integer> scoreCol;

    @FXML
    private TableColumn<GameRecord, String> resultCol;

    private String usuarioCorreo;

    @FXML
    protected void initialize() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));

        recordsTable.setItems(FXCollections.observableArrayList());
    }

    public void setUsuario(String correo) {
        this.usuarioCorreo = correo;
        if (usuarioLabel != null) usuarioLabel.setText(correo == null ? "(usuario)" : correo);
        loadRecords();
    }

    private void loadRecords() {
        List<GameRecord> recs = Statistics.getRecordsForUser(usuarioCorreo);
        ObservableList<GameRecord> items = FXCollections.observableArrayList(recs);
        recordsTable.setItems(items);
    }

    @FXML
    protected void onRegresar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu-seleccion.fxml"));
            Parent root = loader.load();
            MenuSeleccionController c = loader.getController();
            if (c != null && usuarioCorreo != null) c.setUsuario(usuarioCorreo);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
