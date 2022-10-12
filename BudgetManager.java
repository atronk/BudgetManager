package budget;

import java.io.*;
import java.util.*;

public class BudgetManager {
    private final Scanner scan = new Scanner(System.in);

    private float balance = 0.0f;
    private float expenses = 0.0f;
    private final Map<Integer, HashMap<String, Float>> purchases = new HashMap<>();
    private final HashMap<Integer, String> purchaseNumbers = new HashMap<>();
    private final File purchasesFile = new File("purchases.txt");

    private void promptMainMenu() {
        System.out.println("""
                Choose your action:
                1) Add income
                2) Add purchase
                3) Show list of purchases
                4) Balance
                5) Save
                6) Load
                7) Analyze (sort)
                0) Exit
                """);
    }

    private void promptAnalyzerMenu() {
        System.out.println("""
                How do you want to sort?
                1) Sort all purchases
                2) Sort by type
                3) Sort certain type
                4) Back
                """);
    }

    private void promptChoosePurchaseType(Boolean all) {
        if (all) {
            System.out.println("""
                    Choose the type of purchases
                    1) Food
                    2) Clothes
                    3) Entertainment
                    4) Other
                    5) All
                    6) Back
                    """);
        } else {
            System.out.println("""
                    Choose the type of purchase
                    1) Food
                    2) Clothes
                    3) Entertainment
                    4) Other
                    5) Back
                    """);
        }
    }

    private int parseIntFromNextLine() {
        try {
            return Integer.parseInt(scan.nextLine());
        } catch (NumberFormatException exc) {
            return -1;
        }
    }

    private float parseFloatSafe(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException exc) {
            return -1.f;
        }
    }

    private void noPuchaseNotif() {
        System.out.println("The purchase list is empty!");
    }

    private void addIncome() {
        System.out.println("Enter income:");
        var income = parseFloatSafe(scan.nextLine());
        if (income == -1) {
            System.out.println("Wrong number format!");
        } else {
            balance += income;
            System.out.println("Income was added!");
        }
    }

    private void addPurchase() {
        while (true) {
            promptChoosePurchaseType(false);
            var type = parseIntFromNextLine();
            if (type == 5) {
                break;
            } else if (type >= 1 && type <= 4) {
                System.out.println("Enter purchase name:");
                var name = scan.nextLine();
                System.out.println("Enter its price:");
                var price = Float.parseFloat(scan.nextLine());
                expenses += price;
                purchases.get(type).put(name, price);
                System.out.println("Purchase was added!");
            } else {
                System.out.println("No such action!");
            }
            System.out.print("\n");
        }
    }

    private float getTotal(HashMap<String, Float> purchases) {
        if (purchases.size() == 0) {
            return -1;
        }
        float total = 0.0f;
        for (var val : purchases.values()) {
            total += val;
        }
        return total;
    }

    private float getTotalForAll(Integer type) {
        float total = -1;

        if (type == 5) {
            var hasPurchases = false;
            for (int i = 1; i < 5; i++) {
                var subTotal = getTotal(purchases.get(i));
                if (subTotal != -1) {
                    hasPurchases = true;
                    total += subTotal;
                }
            }
            if (hasPurchases) {
                total += 1;
            }
        } else {
            total = getTotal(purchases.get(type));
        }
        return total;
    }

    private static void printPurchase(String key, Float val) {
        System.out.printf("%s $%.2f\n", key, val);
    }

    private static void printPurchaseByType(String key, Float val) {
        System.out.printf("%s - $%.2f\n", key, val);
    }

    private void listPurchases(Integer type) {
        System.out.println(purchaseNumbers.get(type));
        float total = getTotalForAll(type);

        if (total == -1) {
            noPuchaseNotif();
        } else {
            if (type == 5) {
                for (int i = 1; i < 5; i++) {
                    purchases.get(i).forEach(BudgetManager::printPurchase);
                }
            } else {
                purchases.get(type).forEach(BudgetManager::printPurchase);
            }
            System.out.printf("Total sum: $%.2f\n", total);
        }
    }

    private void showPurchases() {
        while (true) {
            promptChoosePurchaseType(true);
            var type = parseIntFromNextLine();
            if (type == 6) {
                break;
            } else if (type >= 1 && type <= 5) {
                listPurchases(type);
            } else {
                System.out.println("No such action!");
            }
            System.out.print("\n");
        }
    }

    private void showBalance() {
        System.out.printf("Balance: $%.2f\n", balance > expenses ? balance - expenses : 0.0);
    }

    private void exitBudget() {
        System.out.println("Bye!");
        System.exit(0);
    }

    private void initPurchasesMap() {
        purchaseNumbers.put(1, "Food");
        purchaseNumbers.put(2, "Clothes");
        purchaseNumbers.put(3, "Entertainment");
        purchaseNumbers.put(4, "Other");
        purchaseNumbers.put(5, "All");
    }

    private void initPurchasesArrays() {
        for (int i = 1; i < 5; i++) {
            purchases.put(i, new HashMap<>());
        }
    }

    private void savePurchases() {
        try (FileWriter fw = new FileWriter(purchasesFile, false)) {
            fw.write(balance + "\n");
            for (var categoryKey : purchases.keySet()) {
                var category = purchases.get(categoryKey);

                fw.write(purchaseNumbers.get(categoryKey) + "\n");
                for (var purchaseName : category.keySet()) {
                    fw.write(purchaseName + " $" + category.get(purchaseName) + "\n");
                }
                fw.write("END\n");
            }
            System.out.println("Purchases were saved!");
        } catch (IOException exc) {
            System.err.println(exc.getMessage());
        }
    }

    private void loadPurchases() {
        try (Scanner scan = new Scanner(purchasesFile)) {
            balance = parseFloatSafe(scan.nextLine());
            if (balance == -1.f) {
                System.out.println("Balance in file is a wrong number");
                return;
            }
            expenses = 0;
            for (int i = 1; i < 5; i++) {
                var lines = new HashMap<String, Float>();
                scan.nextLine();
                for (var line = scan.nextLine(); !line.equals("END"); line = scan.nextLine()) {
                    var purchaseName = line.substring(0, line.lastIndexOf(" "));
                    var purchaseCost = line.substring(line.lastIndexOf("$") + 1);
                    var purchaseCostFloat = parseFloatSafe(purchaseCost);
                    if (purchaseCostFloat == -1.f) {
                        System.out.printf("Purchase \"%s\" is skipped because something is wrong with the price\n", purchaseName);
                        continue;
                    }

                    lines.put(purchaseName, purchaseCostFloat);
                    expenses += purchaseCostFloat;
                }
                purchases.put(i, lines);
            }
            System.out.println("Purchases were loaded!");
        } catch (FileNotFoundException exc) {
            System.err.printf("File with purchases not found: %s\n", exc.getMessage());
        }
    }

    private static Map<String, Float> sortedHashMapByValuesDesc(Map<String, Float> hashmap) {
        // Insert all key-value pairs into TreeMap using a custom comparator.
        TreeMap<String, Float> treeMap = new TreeMap<>((o1, o2) -> {
            if (!hashmap.get(o1).equals(hashmap.get(o2)))
                return -Float.compare(hashmap.get(o1), hashmap.get(o2));

            return -o1.compareTo(o2);
        });
        treeMap.putAll(hashmap);
        return treeMap;
    }

    private HashMap<String, Float> getAllPurchasesMap() {
        HashMap<String, Float> result = new HashMap<>();
        for (var map : purchases.values()) {
            result.putAll(map);
        }
        return result;
    }

    private void sortAll() {
        var isEmpty = true;
        for (var map : purchases.values()) {
            if (map.size() > 0) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            noPuchaseNotif();
        } else {
            System.out.println("All:");
            HashMap<String, Float> map = getAllPurchasesMap();
            Map<String, Float> sorted = sortedHashMapByValuesDesc(map);
            sorted.forEach(BudgetManager::printPurchase);
        }
    }

    private void sortByType() {
        Map<String, Float> map = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            String type = purchaseNumbers.get(i);
            map.put(type, 0.f);
            for (var price : purchases.get(i).values()) {
                map.put(type, map.get(type) + price);
            }
        }
        var sorted = sortedHashMapByValuesDesc(map);
        System.out.println("Types:");
        sorted.forEach(BudgetManager::printPurchaseByType);
        System.out.printf("Total sum: $%.2f\n", sorted.values().stream().reduce(0.f, Float::sum));
    }

    private void sortCertainType() {
        promptChoosePurchaseType(false);
        var type = parseIntFromNextLine();
        if (type >= 1 && type <= 4) {
            HashMap<String, Float> map = purchases.get(type);
            if (map.size() == 0) {
                noPuchaseNotif();
            } else {
                System.out.printf("%s:\n", purchaseNumbers.get(type));
                Map<String, Float> sorted = sortedHashMapByValuesDesc(map);
                sorted.forEach(BudgetManager::printPurchase);
            }
        } else if (type != 5) {
            System.out.println("No such action!");
        }
    }

    private void analyze() {
        while (true) {
            promptAnalyzerMenu();
            switch (parseIntFromNextLine()) {
                case 1 -> sortAll();
                case 2 -> sortByType();
                case 3 -> sortCertainType();
                case 4 -> {
                    return;
                }
                default -> System.out.println("No such action!");
            }
            System.out.print("\n");
        }
    }

    public void start() {
        initPurchasesMap();
        initPurchasesArrays();
        while (true) {
            promptMainMenu();
            switch (parseIntFromNextLine()) {
                case 1 -> addIncome();
                case 2 -> addPurchase();
                case 3 -> showPurchases();
                case 4 -> showBalance();
                case 5 -> savePurchases();
                case 6 -> loadPurchases();
                case 7 -> analyze();
                case 0 -> {
                    exitBudget();
                    return;
                }
                default -> System.out.println("No such action");
            }
            System.out.print("\n");
        }
    }
}
