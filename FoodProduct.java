public class FoodProduct extends Product {

    public FoodProduct(String id, String name, double price, int stock) {
        super(id, name, price, stock);
    }

    @Override
    public String getCategory() { return "Food"; }

    @Override
    public double getDiscountRate() { return 0.0; }  
}
