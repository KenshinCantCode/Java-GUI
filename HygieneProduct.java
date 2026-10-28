public class HygieneProduct extends Product {

    public HygieneProduct(String id, String name, double price, int stock) {
        super(id, name, price, stock);
    }

    @Override
    public String getCategory() { return "Hygiene"; }

    @Override
    public double getDiscountRate() { return 0.10; } 
}
