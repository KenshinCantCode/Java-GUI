public class SnackProduct extends Product {

    public SnackProduct(String id, String name, double price, int stock) {
        super(id, name, price, stock);
    }

    @Override
    public String getCategory() { return "Snack"; }

    @Override
    public double getDiscountRate() { return 0.10; }
}
