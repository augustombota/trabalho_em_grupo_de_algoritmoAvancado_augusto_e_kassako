module com.example.algoritmo_de_bellmanford {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.algoritmo_de_bellmanford to javafx.fxml;
    exports com.example.algoritmo_de_bellmanford;
}