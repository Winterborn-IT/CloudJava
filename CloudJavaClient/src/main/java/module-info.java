module com.cloud.cloudjavaclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cloud.cloudjavaclient to javafx.fxml;
    exports com.cloud.cloudjavaclient;
}