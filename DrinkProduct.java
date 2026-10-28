/** INHERITANCE: DrinkProduct extends Product. POLYMORPHISM: 5% promo discount. */
public class DrinkProduct extends Product {

    public DrinkProduct(String id, String name, double price, int stock) {
        super(id, name, price, stock);
    }

    @Override
    public String getCategory() { return "Drink"; }

    @Override
    public double getDiscountRate() { return 0.05; } 
}
