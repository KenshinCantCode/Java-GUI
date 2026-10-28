import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FILE I/O: reads/writes products.txt (inventory) and appends to sales_log.txt (receipts).
 */
public class FileManager {

    public static final String PRODUCTS_FILE = "products.txt";
    public static final String SALES_FILE = "sales_log.txt";

    /** Factory: builds the right subclass from the category text. */
    public static Product createProduct(String category, String id, String name,
                                        double price, int stock) {
        switch (category.trim().toLowerCase()) {
            case "food":  return new FoodProduct(id, name, price, stock);
            case "drink": return new DrinkProduct(id, name, price, stock);
            case "snack": return new SnackProduct(id, name, price, stock);
            default: throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    public static List<Product> loadProducts() throws IOException {
        List<Product> list = new ArrayList<>();
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 5) continue;
                try {
                    list.add(createProduct(p[0], p[1], p[2],
                            Double.parseDouble(p[3]), Integer.parseInt(p[4])));
                } catch (IllegalArgumentException e) {
                    // skip corrupted lines
                }
            }
        }
        return list;
    }

    public static void saveProducts(List<Product> products) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product p : products) {
                pw.println(p.toFileString());
            }
        }
    }

    public static void appendSale(String receiptText) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SALES_FILE, true))) {
            pw.println(receiptText);
            pw.println("==========================================");
        }
    }

    public static String readSales() throws IOException {
        File file = new File(SALES_FILE);
        if (!file.exists()) return "No sales recorded yet.";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
