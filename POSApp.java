import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple POS System (Swing GUI).
 * Run: javac *.java  then  java POSApp
 */
public class POSApp extends JFrame {

    private final List<Product> products = new ArrayList<>();
    private final List<CartItem> cart = new ArrayList<>();

    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Price", "Promo Price", "Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"Name", "Qty", "Unit Price", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JLabel totalLabel = new JLabel("TOTAL: P0.00");

    public POSApp() {
        super("Simple POS System");
        loadData();
        buildUI();
        refreshProducts();
        refreshCart();
    }

    // ===================== FILE I/O: load / seed data =====================
    private void loadData() {
        try {
            products.addAll(FileManager.loadProducts());
            if (products.isEmpty()) {
                products.add(new FoodProduct("F001", "Chicken Adobo Meal", 85.00, 30));
                products.add(new FoodProduct("F002", "Pancit Canton", 60.00, 25));
                products.add(new DrinkProduct("D001", "Iced Coffee", 55.00, 40));
                products.add(new DrinkProduct("D002", "Bottled Water", 20.00, 100));
                products.add(new SnackProduct("S001", "Potato Chips", 35.00, 50));
                products.add(new SnackProduct("S002", "Chocolate Bar", 25.00, 60));
                FileManager.saveProducts(products);
            }
        } catch (IOException e) {
            showError("Could not read " + FileManager.PRODUCTS_FILE + ": " + e.getMessage());
        }
    }

    // ===================== GUI =====================
    private void buildUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(24);
        cartTable.setRowHeight(24);

        // Left: products
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Products"));
        left.add(new JScrollPane(productTable), BorderLayout.CENTER);
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addToCart = new JButton("Add to Cart");
        JButton addProduct = new JButton("New Product");
        JButton restock = new JButton("Restock");
        leftButtons.add(addToCart);
        leftButtons.add(addProduct);
        leftButtons.add(restock);
        left.add(leftButtons, BorderLayout.SOUTH);

        // Right: cart
        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(BorderFactory.createTitledBorder("Cart"));
        right.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel rightBottom = new JPanel(new BorderLayout());
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rightBottom.add(totalLabel, BorderLayout.NORTH);
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton remove = new JButton("Remove Item");
        JButton clear = new JButton("Clear Cart");
        JButton checkout = new JButton("Checkout");
        JButton history = new JButton("Sales History");
        rightButtons.add(remove);
        rightButtons.add(clear);
        rightButtons.add(checkout);
        rightButtons.add(history);
        rightBottom.add(rightButtons, BorderLayout.SOUTH);
        right.add(rightBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        // Actions
        addToCart.addActionListener(e -> addSelectedToCart());
        addProduct.addActionListener(e -> showAddProductDialog());
        restock.addActionListener(e -> restockSelected());
        remove.addActionListener(e -> removeSelectedFromCart());
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        checkout.addActionListener(e -> checkout());
        history.addActionListener(e -> showHistory());
    }

    private void refreshProducts() {
        productModel.setRowCount(0);
        for (Product p : products) {
            productModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    String.format("%.2f", p.getPrice()),
                    String.format("%.2f", p.getFinalPrice()),   // polymorphic price
                    p.getStock()});
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (CartItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getProduct().getName(), item.getQuantity(),
                    String.format("%.2f", item.getProduct().getFinalPrice()),
                    String.format("%.2f", item.getSubtotal())});
            total += item.getSubtotal();
        }
        totalLabel.setText(String.format("TOTAL: P%.2f", total));
    }

    private double cartTotal() {
        double t = 0;
        for (CartItem i : cart) t += i.getSubtotal();
        return t;
    }

    // ===================== Actions =====================
    private void addSelectedToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) { showError("Select a product first."); return; }
        Product p = products.get(row);

        String input = JOptionPane.showInputDialog(this, "Quantity for " + p.getName() + ":", "1");
        if (input == null) return;
        int qty;
        try {
            qty = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            showError("Please enter a whole number.");
            return;
        }

        CartItem existing = findInCart(p);
        int alreadyInCart = existing == null ? 0 : existing.getQuantity();
        if (qty <= 0 || qty + alreadyInCart > p.getStock()) {
            showError("Invalid quantity. Available stock: " + (p.getStock() - alreadyInCart));
            return;
        }

        if (existing == null) cart.add(new CartItem(p, qty));
        else existing.addQuantity(qty);
        refreshCart();
    }

    private CartItem findInCart(Product p) {
        for (CartItem i : cart) if (i.getProduct() == p) return i;
        return null;
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { showError("Select a cart item to remove."); return; }
        cart.remove(row);
        refreshCart();
    }

    private void restockSelected() {
        int row = productTable.getSelectedRow();
        if (row < 0) { showError("Select a product to restock."); return; }
        Product p = products.get(row);
        String input = JOptionPane.showInputDialog(this, "Add how many units to " + p.getName() + "?", "10");
        if (input == null) return;
        try {
            int add = Integer.parseInt(input.trim());
            if (add <= 0) throw new NumberFormatException();
            p.setStock(p.getStock() + add);
            FileManager.saveProducts(products);
            refreshProducts();
        } catch (NumberFormatException ex) {
            showError("Enter a positive whole number.");
        } catch (IOException ex) {
            showError("Could not save: " + ex.getMessage());
        }
    }

    private void showAddProductDialog() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField price = new JTextField();
        JTextField stock = new JTextField();
        JComboBox<String> category = new JComboBox<>(new String[]{"Food", "Drink", "Snack"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("ID:"));        panel.add(id);
        panel.add(new JLabel("Name:"));      panel.add(name);
        panel.add(new JLabel("Category:"));  panel.add(category);
        panel.add(new JLabel("Price:"));     panel.add(price);
        panel.add(new JLabel("Stock:"));     panel.add(stock);

        int result = JOptionPane.showConfirmDialog(this, panel, "New Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            for (Product p : products) {
                if (p.getId().equalsIgnoreCase(id.getText().trim())) {
                    showError("A product with that ID already exists.");
                    return;
                }
            }
            Product p = FileManager.createProduct((String) category.getSelectedItem(),
                    id.getText(), name.getText(),
                    Double.parseDouble(price.getText().trim()),
                    Integer.parseInt(stock.getText().trim()));
            products.add(p);
            FileManager.saveProducts(products);
            refreshProducts();
        } catch (NumberFormatException ex) {
            showError("Price must be a number and stock a whole number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("Could not save: " + ex.getMessage());
        }
    }

    private void checkout() {
        if (cart.isEmpty()) { showError("The cart is empty."); return; }
        double total = cartTotal();

        String input = JOptionPane.showInputDialog(this,
                String.format("Total: P%.2f%nEnter cash received:", total));
        if (input == null) return;

        double cash;
        try {
            cash = Double.parseDouble(input.trim());
        } catch (NumberFormatException ex) {
            showError("Enter a valid amount.");
            return;
        }
        if (cash < total) { showError("Insufficient payment."); return; }

        // Build receipt
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder sb = new StringBuilder();
        sb.append("        SIMPLE POS RECEIPT\n");
        sb.append("Date: ").append(time).append("\n");
        sb.append("------------------------------------------\n");
        for (CartItem item : cart) {
            sb.append(String.format("%-20s %3d x %7.2f = %8.2f%n",
                    item.getProduct().getName(), item.getQuantity(),
                    item.getProduct().getFinalPrice(), item.getSubtotal()));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL:  P%.2f%nCASH:   P%.2f%nCHANGE: P%.2f%n", total, cash, cash - total));

        try {
            for (CartItem item : cart) item.getProduct().reduceStock(item.getQuantity());
            FileManager.saveProducts(products);   // update inventory file
            FileManager.appendSale(sb.toString()); // log the sale
        } catch (IOException ex) {
            showError("Sale completed but could not save files: " + ex.getMessage());
        }

        showTextDialog("Receipt", sb.toString());
        cart.clear();
        refreshProducts();
        refreshCart();
    }

    private void showHistory() {
        try {
            showTextDialog("Sales History", FileManager.readSales());
        } catch (IOException ex) {
            showError("Could not read sales log: " + ex.getMessage());
        }
    }

    // ===================== Helpers =====================
    private void showTextDialog(String title, String text) {
        JTextArea area = new JTextArea(text, 22, 46);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, JOptionPane.PLAIN_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new POSApp().setVisible(true));
    }
}
