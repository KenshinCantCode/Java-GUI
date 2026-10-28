public abstract class Product implements Discountable {

    private String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        setId(id);
        setName(name);
        setPrice(price);
        setStock(stock);
    }

    // ---------- Encapsulation: getters and validated setters ----------
    public String getId() { return id; }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("ID cannot be empty.");
        this.id = id.trim().replace("|", "");
    }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name.trim().replace("|", "");
    }

    public double getPrice() { return price; }

    public void setPrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }

    public int getStock() { return stock; }

    public void setStock(int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative.");
        this.stock = stock;
    }

    public void reduceStock(int qty) {
        if (qty <= 0 || qty > stock)
            throw new IllegalArgumentException("Invalid quantity for " + name);
        stock -= qty;
    }

    // ---------- Abstract: every subclass MUST define its own category ----------
    public abstract String getCategory();

    // ---------- Polymorphism: uses getDiscountRate(), which differs per subclass ----------
    @Override
    public double getFinalPrice() {
        return price - (price * getDiscountRate());
    }

    /** Format used when saving to products.txt */
    public String toFileString() {
        return getCategory() + "|" + id + "|" + name + "|" + price + "|" + stock;
    }

    @Override
    public String toString() {
        return name + " (" + getCategory() + ")";
    }
}
