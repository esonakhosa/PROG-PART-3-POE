import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner input = new Scanner(System.in);
    private static final MessageManager manager = new MessageManager();

    public static void main(String[] args) {
        manager.loadStoredMessages();

        boolean running = true;
        while (running) {
            System.out.println("\n========== QUICKCHAT ==========");
            System.out.println("1. Send Message");
            System.out.println("2. Disregard Message");
            System.out.println("3. Store Message");
            System.out.println("4. Stored Messages");
            System.out.println("5. Display Sent Messages");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();
            switch (choice) {
                case "1":
                    createMessage("Sent");
                    break;
                case "2":
                    createMessage("Disregard");
                    break;
                case "3":
                    createMessage("Stored");
                    break;
                case "4":
                    storedMessagesMenu();
                    break;
                case "5":
                    displaySentMessages();
                    break;
                case "6":
                    running = false;
                    System.out.println("Thank you for using QuickChat.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        input.close();
    }

    private static void createMessage(String flag) {
        System.out.print("Enter recipient cellphone number: ");
        String recipient = input.nextLine().trim();
        System.out.print("Enter your message: ");
        String messageText = input.nextLine();

        Message message = new Message(
                "Developer", recipient, messageText,
                manager.getMessageCounter() + 1, flag);

        int action = flag.equals("Sent") ? 1 : flag.equals("Disregard") ? 2 : 3;
        String result = message.SentMessage(action);
        System.out.println(result);

        if (result.equals("Message successfully sent.")
                || result.equals("Message successfully stored.")) {
            manager.addMessage(message);
            System.out.println("Message ID: " + message.getMessageID());
            System.out.println("Message Hash: " + message.getMessageHash());
        }

        if (flag.equals("Sent")) {
            System.out.println("Total messages sent: " + manager.getSentMessages().length);
        }
    }

    private static void displaySentMessages() {
        Message[] messages = manager.getSentMessages();
        if (messages.length == 0) {
            System.out.println("No messages have been sent.");
            return;
        }

        for (Message message : messages) {
            printMessage(message);
        }
    }

    private static void printMessage(Message message) {
        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message Hash: " + message.getMessageHash());
        System.out.println("Recipient: " + message.getRecipient());
        System.out.println("Message: " + message.getMessageText());
        System.out.println();
    }

    private static void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n======= STORED MESSAGES =======");
            System.out.println("1. Display senders and recipients");
            System.out.println("2. Display the longest stored message");
            System.out.println("3. Search for a message by ID");
            System.out.println("4. Search messages by recipient");
            System.out.println("5. Delete a message using its hash");
            System.out.println("6. Display message report");
            System.out.println("7. Return to main menu");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println(manager.displaySendersAndRecipients());
                    break;
                case "2":
                    System.out.println(manager.findLongestStoredMessage());
                    break;
                case "3":
                    searchByID();
                    break;
                case "4":
                    searchByRecipient();
                    break;
                case "5":
                    deleteByHash();
                    break;
                case "6":
                    System.out.println(manager.displayReport());
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void searchByID() {
        System.out.print("Enter the message ID: ");
        Message found = manager.searchMessageByID(input.nextLine().trim());
        if (found == null) {
            System.out.println("Message not found.");
        } else {
            System.out.println("Recipient: " + found.getRecipient());
            System.out.println("Message: " + found.getMessageText());
        }
    }

    private static void searchByRecipient() {
        System.out.print("Enter recipient cellphone number: ");
        String result = manager.searchMessagesByRecipient(input.nextLine().trim());
        System.out.println(result.isEmpty() ? "No messages found." : result);
    }

    private static void deleteByHash() {
        System.out.print("Enter the message hash: ");
        if (!manager.deleteMessageByHash(input.nextLine().trim())) {
            System.out.println("Message hash not found.");
        } else {
            System.out.println("Message deleted.");
        }
    }
}

class Message {
    private final String sender;
    private final String recipient;
    private final String messageText;
    private final String messageID;
    private final String messageHash;
    private final String flag;

    Message(String sender, String recipient, String messageText,
            int messageNumber, String flag) {
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageID = String.format("%010d", messageNumber);
        this.flag = flag;
        this.messageHash = createHash(messageNumber, messageText);
    }

    String SentMessage(int choice) {
        if (recipient.isEmpty()) {
            return "Recipient cellphone number cannot be empty.";
        }
        if (messageText.length() > 250) {
            return "Message exceeds 250 characters. Please shorten it.";
        }
        if (choice == 1) {
            return "Message successfully sent.";
        }
        if (choice == 2) {
            return "Message disregarded.";
        }
        if (choice == 3) {
            return "Message successfully stored.";
        }
        return "Invalid message action.";
    }

    private String createHash(int messageNumber, String text) {
        String[] words = text.trim().isEmpty() ? new String[0] : text.trim().split("\\s+");
        String first = words.length == 0 ? "" : words[0];
        String last = words.length == 0 ? "" : words[words.length - 1];
        return (messageID.substring(0, 2) + ":" + messageNumber + ":"
                + first + last).toUpperCase();
    }

    String getSender() {
        return sender;
    }

    String getRecipient() {
        return recipient;
    }

    String getMessageText() {
        return messageText;
    }

    String getMessageID() {
        return messageID;
    }

    String getMessageHash() {
        return messageHash;
    }

    String getFlag() {
        return flag;
    }
}

class MessageManager {
    private final List<Message> messages = new ArrayList<>();

    void loadStoredMessages() {
        // Messages are kept for the lifetime of this application session.
    }

    int getMessageCounter() {
        return messages.size();
    }

    void addMessage(Message message) {
        messages.add(message);
    }

    Message[] getSentMessages() {
        List<Message> sent = new ArrayList<>();
        for (Message message : messages) {
            if ("Sent".equals(message.getFlag())) {
                sent.add(message);
            }
        }
        return sent.toArray(new Message[0]);
    }

    String displaySendersAndRecipients() {
        if (messages.isEmpty()) {
            return "No messages are available.";
        }
        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            result.append("Sender: ").append(message.getSender())
                    .append(" | Recipient: ").append(message.getRecipient())
                    .append(System.lineSeparator());
        }
        return result.toString().trim();
    }

    String findLongestStoredMessage() {
        Message longest = null;
        for (Message message : messages) {
            if ("Stored".equals(message.getFlag())
                    && (longest == null
                    || message.getMessageText().length() > longest.getMessageText().length())) {
                longest = message;
            }
        }
        return longest == null ? "No stored messages are available."
                : longest.getMessageText();
    }

    Message searchMessageByID(String id) {
        for (Message message : messages) {
            if (message.getMessageID().equals(id)) {
                return message;
            }
        }
        return null;
    }

    String searchMessagesByRecipient(String recipient) {
        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            if (message.getRecipient().equals(recipient)) {
                result.append("Message ID: ").append(message.getMessageID())
                        .append(" | Message: ").append(message.getMessageText())
                        .append(System.lineSeparator());
            }
        }
        return result.toString().trim();
    }

    boolean deleteMessageByHash(String hash) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageHash().equals(hash)) {
                messages.remove(i);
                return true;
            }
        }
        return false;
    }

    String displayReport() {
        if (messages.isEmpty()) {
            return "No messages are available.";
        }
        StringBuilder result = new StringBuilder("Message Report:");
        for (Message message : messages) {
            result.append(System.lineSeparator())
                    .append("ID: ").append(message.getMessageID())
                    .append(" | Hash: ").append(message.getMessageHash())
                    .append(" | Recipient: ").append(message.getRecipient())
                    .append(" | Message: ").append(message.getMessageText());
        }
        return result.toString();
    }
}
