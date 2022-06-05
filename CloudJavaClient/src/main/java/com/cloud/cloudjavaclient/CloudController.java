package com.cloud.cloudjavaclient;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudController implements Initializable {
    @FXML
    public ListView<String> clientFiles;
    @FXML
    public ListView<String> serverFiles;
    private Network network;


    private void readLoop() {
        try {
            while (true) {
                String msg = network.readMsg();
                System.out.println(msg);
                Platform.runLater(() -> serverFiles.getItems().add(msg));

            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String homeDir = System.getProperty("user.home");
            clientFiles.getItems().clear();
            clientFiles.getItems().addAll(getFiles(homeDir));
            network = new Network(8189);
            Thread readThread = new Thread(() -> readLoop());
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    public void upload(ActionEvent actionEvent) {

    }

    public void download(ActionEvent actionEvent) {

    }

}
