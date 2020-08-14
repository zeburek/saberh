module saberh {
    requires kotlin.stdlib;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires tornadofx;
    requires tornadofx.controlsfx;
    requires org.controlsfx.controls;

    opens ru.zeburek.saberh.utils to javafx.graphics;
    opens ru.zeburek.saberh.views to tornadofx;
    opens ru.zeburek.saberh.controllers to tornadofx;
    opens ru.zeburek.saberh.models to tornadofx;

    exports ru.zeburek.saberh;
}