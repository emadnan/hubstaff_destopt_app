module com.autoscreen.Autoscreen {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires org.jetbrains.annotations;

    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;
    requires org.json;
    requires java.net.http;

    exports Autoscreen to javafx.graphics;

    opens Autoscreen to javafx.fxml;
}
