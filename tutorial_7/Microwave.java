public class Microwave extends Appliance {

    public Microwave(String brand) {
        super(brand);
    }

    @Override
    public void operate() {
        System.out.println("Heating food...");
    }
}