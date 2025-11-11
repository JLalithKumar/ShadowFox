import java.util.Scanner;

public class Calculator {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n==== Enhanced Console Calculator ====");
            System.out.println("1. Basic Arithmetic");
            System.out.println("2. Scientific Operations");
            System.out.println("3. Unit Conversions");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());
                switch (choice) {
                    case 1 -> basicOperations();
                    case 2 -> scientificOperations();
                    case 3 -> unitConversion();
                    case 4 -> System.out.println("Exiting... Thank you!");
                    default -> System.out.println("Invalid choice! Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
                choice = 0;
            }
        } while (choice != 4);
    }

    public static void basicOperations() {
        try {
            System.out.print("Enter first number: ");
            double a = Double.parseDouble(sc.nextLine());
            System.out.print("Enter second number: ");
            double b = Double.parseDouble(sc.nextLine());

            System.out.println("Select Operation: +  -  *  /");
            char op = sc.nextLine().charAt(0);

            switch (op) {
                case '+' -> System.out.println("Result: " + (a + b));
                case '-' -> System.out.println("Result: " + (a - b));
                case '*' -> System.out.println("Result: " + (a * b));
                case '/' -> {
                    if (b == 0) System.out.println("Error: Division by zero!");
                    else System.out.println("Result: " + (a / b));
                }
                default -> System.out.println("Invalid operation!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Try again.");
        }
    }

    public static void scientificOperations() {
        try {
            System.out.println("\n--- Scientific Functions ---");
            System.out.println("1. Square Root");
            System.out.println("2. Power");
            System.out.println("3. Logarithm");
            System.out.println("4. Trigonometric (sin, cos, tan)");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter number: ");
                    double num = Double.parseDouble(sc.nextLine());
                    System.out.println("√" + num + " = " + Math.sqrt(num));
                }
                case 2 -> {
                    System.out.print("Enter base: ");
                    double base = Double.parseDouble(sc.nextLine());
                    System.out.print("Enter exponent: ");
                    double exp = Double.parseDouble(sc.nextLine());
                    System.out.println("Result: " + Math.pow(base, exp));
                }
                case 3 -> {
                    System.out.print("Enter number: ");
                    double num = Double.parseDouble(sc.nextLine());
                    System.out.println("log(" + num + ") = " + Math.log(num));
                }
                case 4 -> {
                    System.out.print("Enter angle (in degrees): ");
                    double angle = Math.toRadians(Double.parseDouble(sc.nextLine()));
                    System.out.println("sin = " + Math.sin(angle));
                    System.out.println("cos = " + Math.cos(angle));
                    System.out.println("tan = " + Math.tan(angle));
                }
                default -> System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    public static void unitConversion() {
        System.out.println("\n--- Unit Conversions ---");
        System.out.println("1. Temperature");
        System.out.println("2. Currency");
        System.out.print("Choose: ");

        try {
            int choice = Integer.parseInt(sc.nextLine());
            switch (choice) {
                case 1 -> temperatureConversion();
                case 2 -> currencyConversion();
                default -> System.out.println("Invalid option!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input!");
        }
    }

    public static void temperatureConversion() {
        System.out.println("1. Celsius to Fahrenheit");
        System.out.println("2. Fahrenheit to Celsius");
        System.out.print("Choose: ");
        int ch = Integer.parseInt(sc.nextLine());
        System.out.print("Enter value: ");
        double val = Double.parseDouble(sc.nextLine());

        if (ch == 1)
            System.out.println(val + "°C = " + ((val * 9 / 5) + 32) + "°F");
        else if (ch == 2)
            System.out.println(val + "°F = " + ((val - 32) * 5 / 9) + "°C");
        else
            System.out.println("Invalid choice!");
    }

    public static void currencyConversion() {
        System.out.println("1. USD to INR");
        System.out.println("2. INR to USD");
        System.out.print("Choose: ");
        int ch = Integer.parseInt(sc.nextLine());
        System.out.print("Enter amount: ");
        double amt = Double.parseDouble(sc.nextLine());
        double rate = 83.0; 

        if (ch == 1)
            System.out.println("$" + amt + " = ₹" + (amt * rate));
        else if (ch == 2)
            System.out.println("₹" + amt + " = $" + (amt / rate));
        else
            System.out.println("Invalid choice!");
    }
}
