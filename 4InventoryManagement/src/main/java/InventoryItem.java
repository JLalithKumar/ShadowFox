public class InventoryItem {
    private String name;
    private String barcode;
    private double price;
    private int quantity;

    public InventoryItem(String name, String barcode, double price, int quantity) {
        this.name = name;
        this.barcode = barcode;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setName(String name) { this.name = name; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
