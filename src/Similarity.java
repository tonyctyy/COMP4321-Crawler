import java.util.HashMap;
import java.util.Map;

public class Similarity {
    public static void main(String[] args) {
        // Example data
        Map<Integer, Map<Integer, Double>> matrix = new HashMap<>();
        matrix.put(1, new HashMap<>());
        matrix.get(1).put(1, 0.5);
        matrix.get(1).put(2, 0.3);
        matrix.get(1).put(3, 0.2);
        matrix.put(2, new HashMap<>());
        matrix.get(2).put(1, 0.1);
        matrix.get(2).put(2, 0.2);
        matrix.get(2).put(3, 0.7);

        // Example query
        Map<Integer, Double> query = new HashMap<>();
        query.put(1, 0.3);
        query.put(2, 0.2);
        query.put(3, 0.1);

        // Calculate cosine similarity
        double[] similarities = calculateCosineSimilarity(matrix, query);

        // Print the results
        System.out.print("Similarities: ");
        for (double similarity : similarities) {
            System.out.print(similarity + " ");
        }
        System.out.println();
    }

    public static double[] calculateCosineSimilarity(Map<Integer, Map<Integer, Double>> matrix, Map<Integer, Double> query) {
        // Convert the matrix into a 2D array representation
        int numRows = matrix.size();
        int numCols = matrix.get(1).size();
        double[][] matrixArray = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            Map<Integer, Double> row = matrix.get(i + 1);
            for (int j = 0; j < numCols; j++) {
                matrixArray[i][j] = row.get(j + 1);
            }
        }

        // Normalize the matrix rows
        for (int i = 0; i < numRows; i++) {
            double norm = 0.0;
            for (int j = 0; j < numCols; j++) {
                norm += matrixArray[i][j] * matrixArray[i][j];
            }
            norm = Math.sqrt(norm);
            for (int j = 0; j < numCols; j++) {
                matrixArray[i][j] /= norm;
            }
        }

        // Convert the query into an array representation
        double[] queryArray = new double[numCols];
        for (int j = 0; j < numCols; j++) {
            queryArray[j] = query.get(j + 1);
        }

        // Normalize the query
        double queryNorm = 0.0;
        for (int j = 0; j < numCols; j++) {
            queryNorm += queryArray[j] * queryArray[j];
        }
        queryNorm = Math.sqrt(queryNorm);
        for (int j = 0; j < numCols; j++) {
            queryArray[j] /= queryNorm;
        }

        // Calculate the cosine similarity
        double[] similarities = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            double dotProduct = 0.0;
            for (int j = 0; j < numCols; j++) {
                dotProduct += matrixArray[i][j] * queryArray[j];
            }
            similarities[i] = dotProduct;
        }

        return similarities;
    }
}