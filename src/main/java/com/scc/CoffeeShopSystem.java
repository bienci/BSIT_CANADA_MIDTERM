package com.scc;

import java.sql.*;
import java.util.Scanner;

public class CoffeeShopSystem {
    // SQLite Database URL
    private static final String DB_URL = "jdbc:sqlite:coffeeshopsystem.db";

    public static void main(String[] args) {
        initializeDatabase();
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        do {
            printHeader("COFFEE SHOP ORDERS SYSTEM");
            System.out.println("[1] Add New Order");
            System.out.println("[2] View Active Orders");
            System.out.println("[3] Exit System");
            printDivider();
            System.out.print("Enter choice: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } else {
                System.out.println("\nINVALID INPUT! Please enter a number between 1 and 3.\n");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    addNewOrder(scanner);
                    break;
                case 2:
                    viewActiveOrders();
                    break;
                case 3:
                    System.out.println("\nExiting system. Thank you!\n");
                    break;
                default:
                    System.out.println("\nINVALID CHOICE! Please select 1, 2, or 3.\n");
            }
        } while (choice != 3);

        scanner.close();
    }

    /**
     * Initializes the SQLite database and creates the orders table if it doesn't exist.
     */
    private static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_name TEXT NOT NULL, " +
                "drink_item TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "status TEXT NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Prompts user for order details and inserts them into the SQLite database.
     */
    private static void addNewOrder(Scanner scanner) {
        printSubHeader("ADD NEW ORDER");

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine().trim();

        System.out.print("Enter Drink Item: ");
        String drinkItem = scanner.nextLine().trim();

        System.out.print("Enter Price: ");
        double price = 0.0;
        if (scanner.hasNextDouble()) {
            price = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
        } else {
            System.out.println("\nERROR: Invalid price format. Order cancelled.\n");
            scanner.nextLine();
            return;
        }

        System.out.print("Enter Status (PENDING / PREPARING / COMPLETED): ");
        String status = scanner.nextLine().trim().toUpperCase();

        // Validate status input
        if (!status.equals("PENDING") && !status.equals("PREPARING") && !status.equals("COMPLETED")) {
            System.out.println("\nERROR: Invalid status. Please use PENDING, PREPARING, or COMPLETED.\n");
            return;
        }

        String insertSQL = "INSERT INTO orders (customer_name, drink_item, price, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, customerName);
            pstmt.setString(2, drinkItem);
            pstmt.setDouble(3, price);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

            System.out.println("\nSUCCESS: Order added to queue!\n");

        } catch (SQLException e) {
            System.out.println("\nDatabase error while adding order: " + e.getMessage() + "\n");
        }
    }

    /**
     * Queries and displays all active/stored orders in a tabular format.
     */
    private static void viewActiveOrders() {
        printHeader("ACTIVE COFFEE ORDERS");

        String querySQL = "SELECT id, customer_name, drink_item, price, status FROM orders";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {

            // Print Table Border & Header
            System.out.println("+-----+----------------------+----------------------+------------+------------+");
            System.out.printf("| %-3s | %-20s | %-20s | %-10s | %-10s |\n", "ID", "CUSTOMER NAME", "DRINK ITEM", "PRICE", "STATUS");
            System.out.println("+-----+----------------------+----------------------+------------+------------+");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                int id = rs.getInt("id");
                String customerName = rs.getString("customer_name");
                String drinkItem = rs.getString("drink_item");
                double price = rs.getDouble("price");
                String status = rs.getString("status");

                System.out.printf("| %-3d | %-20s | %-20s | %10.2f | %-10s |\n",
                        id, truncate(customerName, 20), truncate(drinkItem, 20), price, status);
            }

            if (!hasRows) {
                System.out.printf("| %-75s |\n", "No active orders found in the database.");
            }

            System.out.println("+-----+----------------------+----------------------+------------+------------+\n");

        } catch (SQLException e) {
            System.out.println("\nDatabase error while retrieving orders: " + e.getMessage() + "\n");
        }
    }

    // Helper method to format table borders
    private static void printHeader(String title) {
        System.out.println("===============================================================================");
        System.out.printf("                              %s                               \n", title);
        System.out.println("===============================================================================");
    }

    private static void printSubHeader(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static void printDivider() {
        System.out.println("===============================================================================");
    }

    // Helper method to keep table columns cleanly aligned if strings are too long
    private static String truncate(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength - 3) + "...";
    }
}