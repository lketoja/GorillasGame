package fi.utu.tech.distributed.gorilla;

import fi.utu.tech.distributed.gorilla.logic.GameMode;
import fi.utu.tech.distributed.gorilla.logic.GorillaLogic;
import fi.utu.tech.oomkit.app.OOMApp;
import fi.utu.tech.oomkit.controls.Button;
import fi.utu.tech.oomkit.controls.NodeList;
import fi.utu.tech.oomkit.windows.Window;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends OOMApp {
    final static GorillaLogic appLogic = new GorillaLogic();

    @Override
    protected Window generateMainWindow(Stage stage, String appName, double width, double height) {
        return new SimpleMainWindow(stage, appName, width * 1.5, height * 1.5) {
            @Override
            public NodeList bottomBarContent() {
                return new NodeList(
                        new Label("Some examples:"),
                        new Button("Intro", e -> appLogic.setMode(GameMode.Intro)),
                        new Button("Menu", e -> appLogic.setMode(GameMode.Menu)),
                        new Button("Game", e -> appLogic.setMode(GameMode.Game)),
                        new Button("<<", e -> appLogic.views.addVelocity(-5)),
                        new Button("=", e -> appLogic.views.setVelocity(0)),
                        new Button(">>", e -> appLogic.views.addVelocity(5))
                ).cat(basicButtons());
            }
        };
    }

    public App() {
        super(appLogic);
    }
}
