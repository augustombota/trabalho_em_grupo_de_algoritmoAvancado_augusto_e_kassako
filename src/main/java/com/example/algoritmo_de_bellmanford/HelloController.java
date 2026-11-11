package com.example.algoritmo_de_bellmanford;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

import java.util.*;

public class HelloController {

    @FXML private Button btnExecutar;
    @FXML private Label lblStatus;
    @FXML private TextArea txtConsole;
    @FXML private TableView<ResultadoNo> tabelaResultados;
    @FXML private TableColumn<ResultadoNo, String> colVertice;
    @FXML private TableColumn<ResultadoNo, String> colDistancia;
    @FXML private TableColumn<ResultadoNo, String> colPredecessor;
    @FXML private Pane paneGrafo;

    public static class Aresta {
        int origem, destino, peso;
        public Aresta(int u, int v, int w) {
            this.origem = u; this.destino = v; this.peso = w;
        }
    }

    public static class ResultadoNo {
        private final String vertice;
        private final String distancia;
        private final String predecessor;

        public ResultadoNo(String v, String d, String p) {
            this.vertice = v; this.distancia = d; this.predecessor = p;
        }

        public String getVertice() { return vertice; }
        public String getDistancia() { return distancia; }
        public String getPredecessor() { return predecessor; }
    }

    // Coordenadas exatas para simular a disposição geométrica da imagem (A, B, C, D em retângulo)
    private final double[][] coordenadasNos = {
            {100, 60},  // Nó 0 (A) - Cima Esquerda
            {400, 60},  // Nó 1 (B) - Cima Direita
            {100, 180}, // Nó 2 (C) - Baixo Esquerda
            {400, 180}  // Nó 3 (D) - Baixo Direita
    };

    @FXML
    public void initialize() {
        colVertice.setCellValueFactory(new PropertyValueFactory<>("vertice"));
        colDistancia.setCellValueFactory(new PropertyValueFactory<>("distancia"));
        colPredecessor.setCellValueFactory(new PropertyValueFactory<>("predecessor"));

        desenharGrafoVisual();
        executarCalculoBellmanFord();
    }

    private void desenharGrafoVisual() {
        paneGrafo.getChildren().clear();
        List<Aresta> arestas = obterArestasExemplo();

        for (Aresta a : arestas) {
            double x1 = coordenadasNos[a.origem][0];
            double y1 = coordenadasNos[a.origem][1];
            double x2 = coordenadasNos[a.destino][0];
            double y2 = coordenadasNos[a.destino][1];

            // Ajuste de espaçamento para arestas paralelas (como B -> D e D -> B)
            double offsetX = 0, offsetY = 0;
            if (a.origem == 1 && a.destino == 3) { // B -> D (deslocado para a esquerda)
                offsetX = -12; offsetY = 0;
            } else if (a.origem == 3 && a.destino == 1) { // D -> B (deslocado para a direita)
                offsetX = 12; offsetY = 0;
            }

            double startX = x1 + offsetX;
            double startY = y1 + offsetY;
            double endX = x2 + offsetX;
            double endY = y2 + offsetY;

            // Linha da aresta
            Line linha = new Line(startX, startY, endX, endY);
            linha.setStroke(Color.valueOf("#2b2b2b"));
            linha.setStrokeWidth(2.2);

            // Calcular o ângulo para desenhar a seta na ponta
            double angle = Math.atan2(endY - startY, endX - startX);
            double arrowLength = 12;
            double arrowWidth = 6;

            // Ajustar o ponto final para a seta ficar na borda do círculo (raio = 22)
            double radius = 22;
            double adjustedEndX = endX - radius * Math.cos(angle);
            double adjustedEndY = endY - radius * Math.sin(angle);

            linha.setEndX(adjustedEndX);
            linha.setEndY(adjustedEndY);

            // Ponta da seta (Triângulo)
            Polygon seta = new Polygon(
                    adjustedEndX, adjustedEndY,
                    adjustedEndX - arrowLength * Math.cos(angle - Math.toDegrees(30)), adjustedEndY - arrowLength * Math.sin(angle - Math.toDegrees(30)),
                    adjustedEndX - arrowLength * Math.cos(angle + Math.toDegrees(30)), adjustedEndY - arrowLength * Math.sin(angle + Math.toDegrees(30))
            );
            // Correção trigonométrica simples para a ponta da seta
            seta.getPoints().clear();
            double xA = adjustedEndX;
            double yA = adjustedEndY;
            double xB = adjustedEndX - arrowLength * Math.cos(angle - 0.4);
            double yB = adjustedEndY - arrowLength * Math.sin(angle - 0.4);
            double xC = adjustedEndX - arrowLength * Math.cos(angle + 0.4);
            double yC = adjustedEndY - arrowLength * Math.sin(angle + 0.4);
            seta.getPoints().addAll(xA, yA, xB, yB, xC, yC);
            seta.setFill(Color.valueOf("#2b2b2b"));

            // Posição do texto do peso (junto ao meio da aresta)
            double textX = (startX + endX) / 2 + offsetX * 1.5;
            double textY = (startY + endY) / 2 + offsetY * 1.5;

            // Pequeno ajuste visual para o texto não ficar em cima da linha
            if (a.origem == 0 && a.destino == 1) textY -= 10; // A -> B (peso em cima)
            if (a.origem == 2 && a.destino == 3) textY += 18; // C -> D (peso em baixo)
            if (a.origem == 0 && a.destino == 2) textX -= 15; // A -> C (peso à esquerda)

            Text textoPeso = new Text(textX - 4, textY, String.valueOf(a.peso));
            textoPeso.setFill(Color.RED);
            textoPeso.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            paneGrafo.getChildren().addAll(linha, seta, textoPeso);
        }

        // Desenhar os Vértices (Círculos verdes idênticos ao Draw.io)
        String[] nomes = {"A", "B", "C", "D"};
        for (int i = 0; i < coordenadasNos.length; i++) {
            double x = coordenadasNos[i][0];
            double y = coordenadasNos[i][1];

            Circle circulo = new Circle(x, y, 22);
            circulo.setFill(Color.valueOf("#2e7d32")); // Verde
            circulo.setStroke(Color.BLACK);
            circulo.setStrokeWidth(1.5);

            Text textoNo = new Text(x - 5, y + 6, nomes[i]);
            textoNo.setFill(Color.WHITE);
            textoNo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            paneGrafo.getChildren().addAll(circulo, textoNo);
        }
    }

    private List<Aresta> obterArestasExemplo() {
        List<Aresta> arestas = new ArrayList<>();
        arestas.add(new Aresta(0, 1, 4));   // A -> B (Peso 4)
        arestas.add(new Aresta(0, 2, 2));   // A -> C (Peso 2)
        arestas.add(new Aresta(2, 3, 2));   // C -> D (Peso 2)
        arestas.add(new Aresta(3, 1, 1));   // D -> B (Peso 1)
        arestas.add(new Aresta(3, 0, -3));  // B -> D (Peso -3)
        return arestas;
    }

    @FXML
    protected void onExecutarButtonClick() {
        executarCalculoBellmanFord();
    }

    private void executarCalculoBellmanFord() {
        txtConsole.clear();
        txtConsole.appendText(">>> A iniciar o Algoritmo de Bellman-Ford...\n");
        txtConsole.appendText(">>> Vértice de origem: 0 (Nó A)\n");
        txtConsole.appendText(">>> A efetuar o relaxamento iterativo das arestas (|V|-1 = 3 passagens)...\n\n");

        int numVertices = 4;
        List<Aresta> arestas = obterArestasExemplo();

        int[] dist = new int[numVertices];
        int[] pred = new int[numVertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(pred, -1);
        dist[0] = 0;

        for (int i = 1; i < numVertices; i++) {
            txtConsole.appendText("--- Iteração / Passagem " + i + " ---\n");
            for (Aresta a : arestas) {
                if (dist[a.origem] != Integer.MAX_VALUE && dist[a.origem] + a.peso < dist[a.destino]) {
                    dist[a.destino] = dist[a.origem] + a.peso;
                    pred[a.destino] = a.origem;
                    txtConsole.appendText("Relaxada aresta " + obterNomeNo(a.origem) + " -> " + obterNomeNo(a.destino)
                            + " (Peso: " + a.peso + "). Nova distância para " + obterNomeNo(a.destino) + ": " + dist[a.destino] + "\n");
                }
            }
        }

        boolean temCicloNegativo = false;
        for (Aresta a : arestas) {
            if (dist[a.origem] != Integer.MAX_VALUE && dist[a.origem] + a.peso < dist[a.destino]) {
                temCicloNegativo = true;
                break;
            }
        }

        txtConsole.appendText("\n========================================\n");
        if (temCicloNegativo) {
            lblStatus.setText("ALERTA: Ciclo de Custo Negativo Detetado!");
            lblStatus.setTextFill(Color.RED);
            txtConsole.appendText("[ERRO CRÍTICO] O grafo contém um ciclo de custo negativo!\n");
        } else {
            lblStatus.setText("Sucesso: Caminhos mínimos calculados.");
            lblStatus.setTextFill(Color.GREEN);
            txtConsole.appendText("[SUCESSO] Algoritmo executado sem ciclos negativos.\n");
        }
        txtConsole.appendText("========================================\n\n");

        ObservableList<ResultadoNo> dados = FXCollections.observableArrayList();
        String[] nomes = {"Nó A", "Nó B", "Nó C", "Nó D"};

        txtConsole.appendText(">>> RESULTADOS FINAIS NA TABELA:\n");
        for (int i = 0; i < numVertices; i++) {
            String dStr = (dist[i] == Integer.MAX_VALUE) ? "INF" : String.valueOf(dist[i]);
            String pStr = (pred[i] == -1) ? "Nenhum" : nomes[pred[i]];

            dados.add(new ResultadoNo(nomes[i], dStr, pStr));
            txtConsole.appendText("Destino: " + nomes[i] + " | Distância Mínima: " + dStr + " | Predecessor: " + pStr + "\n");
        }

        tabelaResultados.setItems(dados);
    }

    private String obterNomeNo(int indice) {
        String[] nomes = {"A", "B", "C", "D"};
        return (indice >= 0 && indice < nomes.length) ? nomes[indice] : String.valueOf(indice);
    }
}