module Server {

    //requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    //requires javafx.graphics;
    /*requires javafx.web;
    requires javafx.media;*/
    requires org.apache.logging.log4j;
    requires static lombok;


    opens com.kwasheniak to javafx.fxml;
    exports com.kwasheniak;
}