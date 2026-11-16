import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewClass1 {
    public static void main(String[] args) {
        runChecks();
        System.out.println("All message checks passed.");
    }

    private static void runChecks() {
        MessageManager manager = createManager();

        checkEquals(2, manager.getSentMessages().length, "sent message count");
        checkEquals("Did you get the cake?", manager.getSentMessages()[0].getMessageText(), "first sent message");
        checkEquals("It is dinner time !", manager.getSentMessages()[1].getMessageText(), "second sent message");

        checkEquals(1, manager.getDisregardedMessages().length, "disregarded message count");
        checkEquals("Yohoooo, I am at your gate.", manager.getDisregardedMessages()[0].getMessageText(), "disregarded message");

        checkEquals(2, manager.getStoredMessages().length, "stored message count");
        checkEquals("Where are you? You are late! I have asked you to be on time.",
                manager.getStoredMessages()[0].getMessageText(), "first stored message");
        checkEquals("Ok, I am leaving without you.",
                manager.getStoredMessages()[1].getMessageText(), "second stored message");

        checkEquals(5, manager.getMessageIDs().length, "message ID count");
        checkEquals(5, manager.getMessageHashes().length, "message hash count");
        checkEquals("Where are you? You are late! I have asked you to be on time.",
                manager.findLongestStoredMessage(), "longest stored message");

        Message found = manager.searchMessageByID("0838884567");
        checkNotNull(found, "search by message ID");
        checkEquals("It is dinner time !", found.getMessageText(), "message found by ID");

        String byRecipient = manager.searchMessagesByRecipient("+27838884567");
        checkContains(byRecipient, "Where are you? You are late! I have asked you to be on time.", "recipient search");
        checkContains(byRecipient, "Ok, I am leaving without you.", "recipient search");
        checkContains(byRecipient, "It is dinner time !", "recipient search");

        MessageManager deleteManager = createManager();
        String hash = deleteManager.getStoredMessages()[0].getMessageHash();
        checkTrue(deleteManager.deleteMessageByHash(hash), "delete by hash");
        checkEquals(1, deleteManager.getStoredMessages().length, "stored count after delete");
        checkEquals("Ok, I am leaving without you.",
                deleteManager.getStoredMessages()[0].getMessageText(), "remaining stored message");

        String report = manager.displayReport();
        checkContains(report, "Message Hash:", "report hash label");
        checkContains(report, "Recipient:", "report recipient label");
        checkContains(report, "Message:", "report message label");
        checkContains(report, "Did you get the cake?", "report sent message");
        checkContains(report, "It is dinner time !", "report sent message");

        checkEquals("Developer", manager.getStoredMessages()[0].getSender(), "message sender");
    }

    private static MessageManager createManager() {
        MessageManager manager = new MessageManager();
        manager.addMessage(new Message("Developer", "+27834557896", "Did you get the cake?", 1,
                "1234567890", null, "Sent"));
        manager.addMessage(new Message("Developer", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", 2,
                "1234567891", null, "Stored"));
        manager.addMessage(new Message("Developer", "+27834484567", "Yohoooo, I am at your gate.", 3,
                "1234567892", null, "Disregard"));
        manager.addMessage(new Message("Developer", "+27838884567", "It is dinner time !", 4,
                "0838884567", null, "Sent"));
        manager.addMessage(new Message("Developer", "+27838884567", "Ok, I am leaving without you.", 5,
                "1234567894", null, "Stored"));
        return manager;
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(description + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void checkTrue(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description + ": expected true");
        }
    }

    private static void checkNotNull(Object value, String description) {
        if (value == null) {
            throw new AssertionError(description + ": expected a value, but was null");
        }
    }

    private static void checkContains(String value, String expectedPart, String description) {
        if (value == null || !value.contains(expectedPart)) {
            throw new AssertionError(description + ": missing <" + expectedPart + ">");
        }
    }
}

class Message {
    private final String sender;
    private final String recipient;
    private final String messageText;
    private final int messageNumber;
    private final String messageID;
    private final String messageHash;
    private final String status;

    public Message(String sender, String recipient, String messageText, int messageNumber,
                   String messageID, String messageHash, String status) {
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageNumber = messageNumber;
        this.messageID = messageID;
        this.messageHash = (messageHash == null || messageHash.isEmpty())
                ? createHash(sender, recipient, messageText, messageNumber, messageID)
                : messageHash;
        this.status = status;
    }

    private static String createHash(String sender, String recipient, String text, int number, String id) {
        String input = String.valueOf(sender) + "|" + recipient + "|" + text + "|" + number + "|" + id;
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getStatus() {
        return status;
    }
}

class MessageManager {
    private final List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        messages.add(message);
    }

    public Message[] getSentMessages() {
        return messagesWithStatus("Sent");
    }

    public Message[] getDisregardedMessages() {
        return messagesWithStatus("Disregard");
    }

    public Message[] getStoredMessages() {
        return messagesWithStatus("Stored");
    }

    private Message[] messagesWithStatus(String status) {
        List<Message> result = new ArrayList<>();
        for (Message message : messages) {
            if (status.equalsIgnoreCase(message.getStatus())) {
                result.add(message);
            }
        }
        return result.toArray(new Message[0]);
    }

    public String[] getMessageIDs() {
        String[] result = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            result[i] = messages.get(i).getMessageID();
        }
        return result;
    }

    public String[] getMessageHashes() {
        String[] result = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            result[i] = messages.get(i).getMessageHash();
        }
        return result;
    }

    public String findLongestStoredMessage() {
        Message longest = null;
        for (Message message : getStoredMessages()) {
            if (longest == null || message.getMessageText().length() > longest.getMessageText().length()) {
                longest = message;
            }
        }
        return longest == null ? null : longest.getMessageText();
    }

    public Message searchMessageByID(String messageID) {
        for (Message message : messages) {
            if (Objects.equals(messageID, message.getMessageID())) {
                return message;
            }
        }
        return null;
    }

    public String searchMessagesByRecipient(String recipient) {
        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            if (Objects.equals(recipient, message.getRecipient())) {
                if (result.length() > 0) {
                    result.append(System.lineSeparator());
                }
                result.append(message.getMessageText());
            }
        }
        return result.toString();
    }

    public boolean deleteMessageByHash(String messageHash) {
        for (int i = 0; i < messages.size(); i++) {
            if (Objects.equals(messageHash, messages.get(i).getMessageHash())) {
                messages.remove(i);
                return true;
            }
        }
        return false;
    }

    public String displayReport() {
        StringBuilder report = new StringBuilder();
        for (Message message : messages) {
            if (report.length() > 0) {
                report.append(System.lineSeparator());
            }
            report.append("Message Hash: ").append(message.getMessageHash()).append(System.lineSeparator())
                    .append("Recipient: ").append(message.getRecipient()).append(System.lineSeparator())
                    .append("Message: ").append(message.getMessageText());
        }
        return report.toString();
    }
}
