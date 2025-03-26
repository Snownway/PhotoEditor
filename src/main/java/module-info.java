module com.photoeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.unsupported.desktop;
    requires thumbnailator;

    opens com.photoeditor to javafx.fxml;
    exports com.photoeditor;
}