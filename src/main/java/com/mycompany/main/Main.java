package com.mycompany.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static Scanner input = new Scanner(System.in);

    // Stores the user's entered details
    static String username;
    static String password;
    static String cellPhone;

    // Stores the successfully registered details
    static String registeredUsername;
    static String registeredPassword;
    static String registeredCellPhone;

    public static boolean checkUsername(String username) {
        // Username must have exactly 5 characters
        // and must contain an underscore
        if (username.length() == 5 && username.contains("_")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkPasswordComplexity(String password) {
        // Password must have at least 8 characters
        // and contain uppercase, lowercase, number and special character
        if (password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkCellPhoneNumber(String number) {
        // Number must start with +27
        // followed by exactly 9 digits
        if (number.matches("^\\+27[0-9]{9}$")) {
            return true;
        } else {
            return false;
        }
    }

    public static void registerUser() {

        // Ask for username until it is valid
        while (true) {
            System.out.print("Enter your username: ");
            username = input.nextLine();

            boolean usernameCorrect = checkUsername(username);

            if (usernameCorrect) {
                break;
            } else {
                System.out.println(
                    "Username is incorrectly formatted. Please try again."
                );
            }
        }

        // Ask for password
        System.out.print("Enter your password: ");
        password = input.nextLine();

        // Ask for cellphone number
        System.out.print("Enter your cellphone number (+27): ");
        cellPhone = input.nextLine();

        // Check password and cellphone number
        boolean passwordCorrect = checkPasswordComplexity(password);
        boolean phoneCorrect = checkCellPhoneNumber(cellPhone);

        // Check if all registration details are correct
        if (passwordCorrect && phoneCorrect) {

            // Save the valid registration details
            registeredUsername = username;
            registeredPassword = password;
            registeredCellPhone = cellPhone;

            // Display success message
            System.out.println("User registered successfully.");

        } else {
            System.out.println("Registration failed.");

            if (!passwordCorrect) {
                System.out.println("Password is incorrectly formatted.");
            }

            if (!phoneCorrect) {
                System.out.println("Cellphone number is incorrectly formatted.");
            }
        }
    }

    public static boolean loginUser(String username, String password) {
        // Compare entered details with registered details
        if (username.equals(registeredUsername)
                && password.equals(registeredPassword)) {
            return true;
        } else {
            return false;
        }
    }

    public static String returnLoginStatus(boolean loginSuccessful) {

        if (loginSuccessful) {
            return "Welcome, it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }

    // ================= Part 2: QuickChat messages =================

    // Each message is stored as {messageID, messageHash, recipient, message}
    static ArrayList<String[]> sentMessages = new ArrayList<>();
    static ArrayList<String[]> storedMessages = new ArrayList<>();

    // Part 3 arrays (populated from user input and the JSON file, not hard-coded)
    static ArrayList<String[]> disregardedMessages = new ArrayList<>();
    static ArrayList<String> messageHashes = new ArrayList<>();
    static ArrayList<String> messageIDs = new ArrayList<>();

    // The JSON file used to store messages
    static String jsonFile = "storedMessages.json";

    // Total number of messages sent
    static int totalMessagesSent = 0;

    // Generates a random ten-digit message ID (leading zeros allowed)
    public static String generateMessageID() {
        long number = (long) (new Random().nextDouble() * 10_000_000_000L);
        return String.format("%010d", number);
    }

    // Ensures the message ID is not more than ten characters
    public static boolean checkMessageID(String messageID) {
        return messageID != null && messageID.length() <= 10;
    }

    // Ensures the message is not more than 250 characters
    public static String checkMessageLength(String text) {
        if (text.length() <= 250) {
            return "Message sent";
        } else {
            return "Please enter a message of less than 250 characters.";
        }
    }

    // Ensures the number starts with an international code (+) and has
    // no more than ten digits after the code
    public static String checkRecipientCell(String recipient) {
        if (recipient != null
                && recipient.matches("^\\+[0-9]{1,3}[0-9]{1,10}$")) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does "
                    + "not contain an international code. "
                    + "Please correct the number and try again.";
        }
    }

    // Hash = first two digits of ID : message number : FIRSTLAST (all caps)
    public static String createMessageHash(String messageID,
                                           int messageNumber, String text) {
        String[] words = text.trim().split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        String start = messageID.length() >= 2
                ? messageID.substring(0, 2) : messageID;

        return (start + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    // Lets the user choose to send, disregard or store the message
    public static String SentMessage(String[] message) {
        System.out.println("Choose an option:");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Option: ");
        int choice;
        try {
            choice = Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            choice = 0;
        }
        return SentMessage(message, choice);
    }

    // Same as above but takes the choice directly (also used by unit tests)
    public static String SentMessage(String[] message, int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(message);
                totalMessagesSent++;
                recordIDAndHash(message);
                return "Message successfully sent.";
            case 2:
                disregardedMessages.add(message);
                recordIDAndHash(message);
                return "Press 0 to delete the message.";
            case 3:
                storeMessage(message);
                recordIDAndHash(message);
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    // Keeps the Message ID and Message Hash arrays up to date
    private static void recordIDAndHash(String[] message) {
        messageIDs.add(message[0]);
        messageHashes.add(message[1]);
    }

    // Returns all the messages sent while the program is running
    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (String[] m : sentMessages) {
            sb.append(getDetails(m)).append("\n");
        }
        return sb.toString();
    }

    // Returns the total number of messages sent
    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // Stores the message in a JSON file
    public static void storeMessage(String[] message) {
        storedMessages.add(message);
        saveStoredMessages();
    }

    // Writes every stored message to the JSON file
    public static void saveStoredMessages() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            String[] m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escape(m[0])).append("\",\n")
                .append("    \"messageHash\": \"").append(escape(m[1])).append("\",\n")
                .append("    \"recipient\": \"").append(escape(m[2])).append("\",\n")
                .append("    \"message\": \"").append(escape(m[3])).append("\"\n")
                .append("  }").append(i < storedMessages.size() - 1 ? "," : "")
                .append("\n");
        }
        json.append("]\n");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write JSON file: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    // Full details in the order: ID, Hash, Recipient, Message
    public static String getDetails(String[] m) {
        return "Message ID: " + m[0]
                + "\nMessage Hash: " + m[1]
                + "\nRecipient: " + m[2]
                + "\nMessage: " + m[3];
    }

    // Clears the stored lists and counter (used by the unit tests)
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
        disregardedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalMessagesSent = 0;
    }

    // ================= Part 3: arrays, search and report =================

    // Reads the JSON file back into the storedMessages array
    public static void loadStoredMessages() {
        storedMessages.clear();

        File file = new File(jsonFile);
        if (!file.exists()) {
            return;
        }

        try {
            String json = new String(Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8);
            String value = "\"((?:[^\"\\\\]|\\\\.)*)\"";
            Pattern pattern = Pattern.compile(
                    "\"messageID\"\\s*:\\s*" + value + "\\s*,\\s*"
                    + "\"messageHash\"\\s*:\\s*" + value + "\\s*,\\s*"
                    + "\"recipient\"\\s*:\\s*" + value + "\\s*,\\s*"
                    + "\"message\"\\s*:\\s*" + value);
            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                String[] message = {
                    unescape(matcher.group(1)),
                    unescape(matcher.group(2)),
                    unescape(matcher.group(3)),
                    unescape(matcher.group(4))
                };
                storedMessages.add(message);
                recordIDAndHash(message);
            }
        } catch (IOException e) {
            System.out.println("Could not read JSON file: " + e.getMessage());
        }
    }

    // Reverses escape() when reading the JSON file
    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                if (next == 'n') {
                    sb.append('\n');
                } else if (next == 'r') {
                    sb.append('\r');
                } else {
                    sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // The sender is the registered user
    public static String getSender() {
        return registeredUsername != null ? registeredUsername : "You";
    }

    // Texts of all the sent messages
    public static ArrayList<String> getSentMessageTexts() {
        ArrayList<String> texts = new ArrayList<>();
        for (String[] m : sentMessages) {
            texts.add(m[3]);
        }
        return texts;
    }

    // a) Display the sender and recipient of all stored messages
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (String[] m : storedMessages) {
            sb.append("Sender: ").append(getSender())
              .append(" | Recipient: ").append(m[2]).append("\n");
        }
        return sb.toString();
    }

    // b) Display the longest stored message
    public static String displayLongestMessage() {
        String longest = "";
        for (String[] m : storedMessages) {
            if (m[3].length() > longest.length()) {
                longest = m[3];
            }
        }
        return longest.isEmpty() ? "There are no stored messages." : longest;
    }

    // c) Search for a message ID and display the recipient and message
    public static String searchByMessageID(String messageID) {
        for (ArrayList<String[]> list : allMessageLists()) {
            for (String[] m : list) {
                if (m[0].equals(messageID)) {
                    return "Recipient: " + m[2] + "\nMessage: " + m[3];
                }
            }
        }
        return "Message ID not found.";
    }

    // d) Search for all the messages sent or stored for a recipient
    public static ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        for (String[] m : sentMessages) {
            if (m[2].equals(recipient)) {
                found.add(m[3]);
            }
        }
        for (String[] m : storedMessages) {
            if (m[2].equals(recipient)) {
                found.add(m[3]);
            }
        }
        return found;
    }

    // e) Delete a stored message using its message hash
    public static String deleteByMessageHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            String[] m = storedMessages.get(i);
            if (m[1].equals(hash)) {
                storedMessages.remove(i);
                messageHashes.remove(m[1]);
                messageIDs.remove(m[0]);
                saveStoredMessages();
                return "Message: \"" + m[3] + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    // f) Report listing the full details of the messages in a list
    public static String displayReport(ArrayList<String[]> messages) {
        if (messages.isEmpty()) {
            return "There are no messages to report.";
        }
        StringBuilder sb = new StringBuilder("----- MESSAGE REPORT -----\n");
        for (String[] m : messages) {
            sb.append("Message Hash: ").append(m[1]).append("\n")
              .append("Recipient: ").append(m[2]).append("\n")
              .append("Message: ").append(m[3]).append("\n")
              .append("--------------------------\n");
        }
        return sb.toString();
    }

    // All the message arrays searched by message ID
    private static ArrayList<ArrayList<String[]>> allMessageLists() {
        ArrayList<ArrayList<String[]>> lists = new ArrayList<>();
        lists.add(sentMessages);
        lists.add(storedMessages);
        lists.add(disregardedMessages);
        return lists;
    }

    // Stored Messages menu (main menu option 4)
    public static void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages for a recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("g) Back to the main menu");
            System.out.print("Choose an option: ");
            String option = input.nextLine().trim().toLowerCase();

            switch (option) {
                case "a":
                    System.out.println(displaySenderAndRecipient());
                    break;
                case "b":
                    System.out.println(displayLongestMessage());
                    break;
                case "c":
                    System.out.print("Enter the message ID: ");
                    System.out.println(searchByMessageID(input.nextLine().trim()));
                    break;
                case "d":
                    System.out.print("Enter the recipient number: ");
                    ArrayList<String> found = searchByRecipient(input.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }
                    for (String text : found) {
                        System.out.println("Message: " + text);
                    }
                    break;
                case "e":
                    System.out.print("Enter the message hash: ");
                    System.out.println(deleteByMessageHash(
                            input.nextLine().trim().toUpperCase()));
                    break;
                case "f":
                    System.out.println(displayReport(storedMessages));
                    break;
                case "g":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Creates a message array with its auto-generated hash
    public static String[] createMessage(String messageID, int messageNumber,
                                         String recipient, String text) {
        return new String[] {
            messageID,
            createMessageHash(messageID, messageNumber, text),
            recipient,
            text
        };
    }

    // QuickChat menu (only available after a successful login)
    public static void quickChat() {

        System.out.println("Welcome to QuickChat.");

        // Read the previously stored messages from the JSON file
        loadStoredMessages();

        // User defines how many messages they wish to enter
        int numMessages = 0;
        while (numMessages <= 0) {
            System.out.print("How many messages do you wish to enter? ");
            try {
                numMessages = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                numMessages = 0;
            }
            if (numMessages <= 0) {
                System.out.println("Please enter a whole number greater than 0.");
            }
        }

        int messagesEntered = 0;
        boolean running = true;

        // Runs until the user selects Quit
        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");
            String option = input.nextLine().trim();

            switch (option) {
                case "1":
                    if (messagesEntered >= numMessages) {
                        System.out.println("You have already entered all "
                                + numMessages + " messages.");
                        break;
                    }
                    while (messagesEntered < numMessages) {
                        enterMessage(messagesEntered);
                        messagesEntered++;
                    }
                    System.out.println("\nTotal messages sent: "
                            + returnTotalMessages());
                    break;
                case "2":
                    System.out.println("Coming Soon.");
                    break;
                case "3":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                case "4":
                    storedMessagesMenu();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Captures one message from the user
    public static void enterMessage(int messageNumber) {

        // Recipient (asked until valid)
        String recipient;
        while (true) {
            System.out.print("\nEnter recipient cell number (with international code): ");
            recipient = input.nextLine().trim();
            String result = checkRecipientCell(recipient);
            System.out.println(result);
            if (result.startsWith("Cell phone number successfully")) {
                break;
            }
        }

        // Message text (asked until 250 characters or fewer)
        String text;
        while (true) {
            System.out.print("Enter your message: ");
            text = input.nextLine();
            String result = checkMessageLength(text);
            System.out.println(result);
            if (result.equals("Message sent") && !text.trim().isEmpty()) {
                break;
            }
        }

        String messageID = generateMessageID();
        String[] message = createMessage(messageID, messageNumber, recipient, text);
        System.out.println("Message ID generated: " + messageID);

        String outcome = SentMessage(message);
        System.out.println(outcome);

        if (outcome.equals("Message successfully sent.")) {
            System.out.println("\n" + getDetails(message));
        } else if (outcome.equals("Press 0 to delete the message.")) {
            System.out.print("> ");
            if (input.nextLine().trim().equals("0")) {
                System.out.println("Message deleted.");
            }
        }
    }

    public static void main(String[] args) {

        // Call registration method
        registerUser();

        // Attempt login using the registered details
        boolean loginSuccessful = loginUser(username, password);

        // Display login status
        System.out.println(returnLoginStatus(loginSuccessful));

        // Messages can only be sent after a successful login
        if (loginSuccessful) {
            quickChat();
        }
    }
}
