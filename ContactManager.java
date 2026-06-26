import java.io.*;
import java.util.*;

/**
 * ContactManager.java
 * A console-based Contact Management Application.
 *
 * Features:
 *  - Add, View, Update, Delete contacts
 *  - Search contact by name, phone, or email
 *  - Sort contacts alphabetically
 *  - Persist data to a text file (contacts.txt) so records survive restarts
 *
 * Compile : javac ContactManager.java
 * Run     : java ContactManager
 */
public class ContactManager {

    // ---------- Contact model (inner class) ----------
    static class Contact {
        int id;
        String name;
        String phone;
        String email;
        String address;

        Contact(int id, String name, String phone, String email, String address) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.address = address;
        }

        // Convert to a single line for file storage: id|name|phone|email|address
        String toFileLine() {
            return id + "|" + name + "|" + phone + "|" + email + "|" + address;
        }

        static Contact fromFileLine(String line) {
            String[] parts = line.split("\\|");
            String addr = parts.length > 4 ? parts[4] : "";
            return new Contact(
                    Integer.parseInt(parts[0].trim()),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    addr.trim()
            );
        }

        @Override
        public String toString() {
            return String.format("%-5d %-20s %-15s %-25s %-20s", id, name, phone, email, address);
        }
    }

    // ---------- Fields ----------
    private static final String FILE_NAME = "contacts.txt";
    private final List<Contact> contacts = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private int nextId = 1;

    public static void main(String[] args) {
        ContactManager manager = new ContactManager();
        manager.loadFromFile();
        manager.run();
    }

    // ---------- Main loop ----------
    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addContact();
                case 2 -> viewAllContacts();
                case 3 -> searchContact();
                case 4 -> updateContact();
                case 5 -> deleteContact();
                case 6 -> sortContacts();
                case 7 -> {
                    saveToFile();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("===================================");
        System.out.println("     CONTACT MANAGEMENT SYSTEM      ");
        System.out.println("===================================");
        System.out.println("1. Add Contact");
        System.out.println("2. View All Contacts");
        System.out.println("3. Search Contact");
        System.out.println("4. Update Contact");
        System.out.println("5. Delete Contact");
        System.out.println("6. Sort Contacts (A-Z)");
        System.out.println("7. Save & Exit");
        System.out.println("===================================");
    }

    // ---------- CRUD operations ----------
    private void addContact() {
        System.out.println("\n--- Add New Contact ---");
        String name = readNonEmptyString("Enter name: ");
        String phone = readPhone("Enter phone number: ");

        if (findByPhone(phone) != null) {
            System.out.println("A contact with this phone number already exists. Contact not added.\n");
            return;
        }

        String email = readString("Enter email (optional): ");
        String address = readString("Enter address (optional): ");

        Contact c = new Contact(nextId++, name, phone, email, address);
        contacts.add(c);
        System.out.println("Contact added successfully with ID: " + c.id + "\n");
    }

    private void viewAllContacts() {
        System.out.println("\n--- All Contacts ---");
        if (contacts.isEmpty()) {
            System.out.println("No contacts found.\n");
            return;
        }
        printTableHeader();
        for (Contact c : contacts) {
            System.out.println(c);
        }
        System.out.println("\nTotal contacts: " + contacts.size() + "\n");
    }

    private void searchContact() {
        System.out.println("\n--- Search Contact ---");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Phone");
        System.out.println("3. Search by Email");
        int choice = readInt("Enter your choice: ");

        List<Contact> results = new ArrayList<>();

        if (choice == 1) {
            String name = readString("Enter name (or part of it): ");
            for (Contact c : contacts) {
                if (c.name.toLowerCase().contains(name.toLowerCase())) results.add(c);
            }
        } else if (choice == 2) {
            String phone = readString("Enter phone (or part of it): ");
            for (Contact c : contacts) {
                if (c.phone.contains(phone)) results.add(c);
            }
        } else if (choice == 3) {
            String email = readString("Enter email (or part of it): ");
            for (Contact c : contacts) {
                if (c.email.toLowerCase().contains(email.toLowerCase())) results.add(c);
            }
        } else {
            System.out.println("Invalid choice.\n");
            return;
        }

        if (results.isEmpty()) {
            System.out.println("No matching contacts found.\n");
        } else {
            printTableHeader();
            for (Contact c : results) System.out.println(c);
            System.out.println();
        }
    }

    private void updateContact() {
        System.out.println("\n--- Update Contact ---");
        int id = readInt("Enter contact ID to update: ");
        Contact c = findById(id);
        if (c == null) {
            System.out.println("Contact not found.\n");
            return;
        }

        System.out.println("Leave field blank to keep current value.");

        System.out.print("Name [" + c.name + "]: ");
        String name = scanner.nextLine();
        if (!name.isBlank()) c.name = name.trim();

        System.out.print("Phone [" + c.phone + "]: ");
        String phone = scanner.nextLine();
        if (!phone.isBlank()) {
            if (isValidPhone(phone.trim())) {
                c.phone = phone.trim();
            } else {
                System.out.println("Invalid phone format, keeping previous value.");
            }
        }

        System.out.print("Email [" + c.email + "]: ");
        String email = scanner.nextLine();
        if (!email.isBlank()) c.email = email.trim();

        System.out.print("Address [" + c.address + "]: ");
        String address = scanner.nextLine();
        if (!address.isBlank()) c.address = address.trim();

        System.out.println("Contact updated successfully.\n");
    }

    private void deleteContact() {
        System.out.println("\n--- Delete Contact ---");
        int id = readInt("Enter contact ID to delete: ");
        Contact c = findById(id);
        if (c == null) {
            System.out.println("Contact not found.\n");
            return;
        }
        System.out.print("Are you sure you want to delete " + c.name + "? (y/n): ");
        String confirm = scanner.nextLine();
        if (confirm.trim().equalsIgnoreCase("y")) {
            contacts.remove(c);
            System.out.println("Contact deleted successfully.\n");
        } else {
            System.out.println("Deletion cancelled.\n");
        }
    }

    private void sortContacts() {
        if (contacts.isEmpty()) {
            System.out.println("No contacts to sort.\n");
            return;
        }
        contacts.sort(Comparator.comparing(c -> c.name.toLowerCase()));
        System.out.println("Contacts sorted by name.\n");
        viewAllContacts();
    }

    // ---------- Helper methods ----------
    private Contact findById(int id) {
        for (Contact c : contacts) {
            if (c.id == id) return c;
        }
        return null;
    }

    private Contact findByPhone(String phone) {
        for (Contact c : contacts) {
            if (c.phone.equals(phone)) return c;
        }
        return null;
    }

    private void printTableHeader() {
        System.out.printf("%-5s %-20s %-15s %-25s %-20s%n", "ID", "Name", "Phone", "Email", "Address");
        System.out.println("-----------------------------------------------------------------------------");
    }

    private boolean isValidPhone(String phone) {
        // Accepts digits, spaces, +, -, and parentheses; must contain at least 7 digits
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return phone.matches("[0-9+\\-() ]+") && digitsOnly.length() >= 7;
    }

    // ---------- Input helpers (with validation) ----------
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("This field cannot be empty.");
        }
    }

    private String readPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidPhone(input)) return input;
            System.out.println("Please enter a valid phone number (at least 7 digits).");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    // ---------- File persistence ----------
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                Contact c = Contact.fromFileLine(line);
                contacts.add(c);
                if (c.id >= nextId) nextId = c.id + 1;
            }
            System.out.println("Loaded " + contacts.size() + " contact(s) from " + FILE_NAME + "\n");
        } catch (IOException e) {
            System.out.println("Could not load existing data: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Contact c : contacts) {
                writer.println(c.toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }
}
