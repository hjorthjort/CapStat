package capstat.view;
import capstat.infrastructure.DataEventListener;
import capstat.infrastructure.EventBus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Created by Jakob on 14/05/15.
 */
public class MainController{
    private EventBus eb = EventBus.getInstance();
    private Scene scene;
    private Parent root;

    @FXML private void playPressed(){
        eb.notify(Main.SETSCENE_MATCH);
    }
}
