module me.mdzs.clientserverchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.mdzs.clientserverchat to javafx.fxml;
    exports me.mdzs.clientserverchat;
}