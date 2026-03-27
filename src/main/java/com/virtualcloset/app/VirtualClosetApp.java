package com.virtualcloset.app;

import com.virtualcloset.app.service.ClosetRepository;
import com.virtualcloset.app.service.ClosetService;
import com.virtualcloset.app.service.OutfitStorage;
import com.virtualcloset.app.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VirtualClosetApp extends Application {
    @Override
    public void start(Stage stage) {
        ClosetService closetService = new ClosetService(new ClosetRepository(), new OutfitStorage());
        MainView mainView = new MainView(closetService);
        Scene scene = new Scene(mainView.createContent(), 1400, 900);
        stage.setTitle("Virtual Closet");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
