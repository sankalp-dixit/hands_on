public class FactoryMethod {

    interface Vehicle {
        void start();
    }

    static class Car implements Vehicle {
        public void start() {
            System.out.println("Car is starting.");
        }
    }

    static class Bike implements Vehicle {
        public void start() {
            System.out.println("Bike is starting.");
        }
    }

    static class VehicleFactory {
        public Vehicle getVehicle(String type) {
            if (type.equalsIgnoreCase("Car")) {
                return new Car();
            } else if (type.equalsIgnoreCase("Bike")) {
                return new Bike();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        VehicleFactory factory = new VehicleFactory();

        Vehicle v1 = factory.getVehicle("Car");
        v1.start();

        Vehicle v2 = factory.getVehicle("Bike");
        v2.start();
    }
}