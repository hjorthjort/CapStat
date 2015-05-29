package capstat.view;

import capstat.application.LoginController;
import capstat.application.StatisticsController;
import capstat.infrastructure.eventbus.EventBus;
import capstat.model.CapStat;
import capstat.model.statistics.Plottable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Jakob on 21/05/15.
 *
 * Class for the user to view statistics by plotting Charts using desired Data.
 */
public class StatisticsViewController implements Initializable{
    XYChart.Series series = new XYChart.Series();
    CapStat cs = CapStat.getInstance();
    LoginController lc = new LoginController();
    StatisticsController sc = new StatisticsController();
    EventBus eb = EventBus.getInstance();
    int length;
    @FXML ComboBox XComboBox, YComboBox;
    @FXML private LineChart<Double, Double> lineChart;
    @FXML NumberAxis yAxis;
    @FXML CategoryAxis xAxis;
    @FXML Label currentUserLabel;
    @FXML Button logoutButton, plotButton, mainButton;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        XComboBox.getSelectionModel().select(1);
        YComboBox.getSelectionModel().select(0);
        currentUserLabel.setText(cs.getLoggedInUser().getNickname());

        Platform.runLater(() -> {
            mainButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                returnToMain();
            });
        });
        Platform.runLater(() -> {
            plotButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.P), () -> {
                plotClicked();
            });
        });
        Platform.runLater(() -> {
            logoutButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.L), () -> {
                logoutPressed();
            });
        });
    }


    @FXML public void logoutPressed(){
        lc.logoutUser();
        eb.notify(MainView.SETSCENE_LOGIN);
    }

    @FXML public void returnToMain(){
        eb.notify(MainView.SETSCENE_MAIN);
    }

    @FXML public void plotClicked(){
        lineChart.getData().remove(series);
        series = new XYChart.Series();
        addPlotData();
        xAxis.setLabel(XComboBox.getSelectionModel().getSelectedItem().toString());
        yAxis.setLabel(YComboBox.getSelectionModel().getSelectedItem().toString());
        lineChart.getData().add(series);
    }

    public void addPlotData(){
        if(getXArray().length>=getYArray().length){
            length = getYArray().length;
        } else{
            length = getXArray().length;
        }
        for(int i = 0; i<length; i++){
            series.getData().add(new XYChart.Data(getXArray()[i].getLabel(),
                    getYArray
                    ()[i].getValue()));
        }
    }
    public Plottable[] getXArray(){
        String statisticType = XComboBox.getSelectionModel().getSelectedItem()
                        .toString();
        StatisticsController.Statistic statisticStrategy =
                getStatisticTypeForString(statisticType);
        List<Plottable> plottablesList = sc.getData(statisticStrategy, cs
                .getLoggedInUser());

        return plottablesList.toArray(new Plottable[0]);

    }
    public Plottable[] getYArray(){
        String statisticType = YComboBox.getSelectionModel().getSelectedItem()
                .toString();
        StatisticsController.Statistic statisticStrategy =
                getStatisticTypeForString(statisticType);
        List<Plottable> plottablesList = sc.getData(statisticStrategy, cs
                .getLoggedInUser());

        return plottablesList.toArray(new Plottable[0]);
    }

    private StatisticsController.Statistic getStatisticTypeForString(String
                                                                             string) {
        switch (string) {
            case ("Accuracy"):
                return StatisticsController.ACCURACY;
            case ("Time"):
                return StatisticsController.TIME;
        }
        return null;

    }
}
