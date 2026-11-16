import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MessageManager manager = new MessageManager();
        Message example = new Message(
                "0000000001", 1, "5550100", "Hello there!", "Sent");
        manager.addMessage(example);
        System.out.println(manager.displayReport());
    }
}

class Message {
    private String messageID;
    private int messageNumber;
    private String recipient;
    private String sender;
    private String messageText;
    private String messageHash;
    private String flag;

    public Message() {
        this("", 0, "", "", "Unknown", "Stored", "");
    }

    // Compatible with the common five-argument message constructor.
    public Message(String messageID, int messageNumber, String recipient,
                   String messageText, String flag) {
        this(messageID, messageNumber, recipient, messageText,
                "Unknown", flag, createHash(messageNumber, messageText));
    }

    public Message(String messageID, int messageNumber, String recipient,
                   String messageText, String sender, String flag) {
        this(messageID, messageNumber, recipient, messageText,
                sender, flag, createHash(messageNumber, messageText));
    }

    Message(String messageID, int messageNumber, String recipient,
            String messageText, String sender, String flag, String messageHash) {
        this.messageID = safe(messageID);
        this.messageNumber = messageNumber;
        this.recipient = safe(recipient);
        this.messageText = safe(messageText);
        this.sender = safe(sender);
        this.flag = safe(flag);
        this.messageHash = safe(messageHash);
    }

    private static String createHash(int number, String text) {
        String content = safe(text);
        String start = content.substring(0, Math.min(2, content.length()));
        String end = content.substring(Math.max(0, content.length() - 2));
        return number + ":" + start + ":" + end;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getSender() { return sender; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
    public String getFlag() { return flag; }

    public void setSender(String sender) { this.sender = safe(sender); }
    public void setFlag(String flag) { this.flag = safe(flag); }
}

class MessageManager {
    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];
    private String[] messageHashes = new String[0];
    private String[] messageIDs = new String[0];
    private int messageCounter;

    // Records are Base64-encoded, tab-separated fields in this text file.
    private static final Path FILE = Paths.get("messages.txt");

    public void addMessage(Message message) {
        if (message == null || message.getFlag() == null) {
            return;
        }

        String flag = message.getFlag();
        if (flag.equalsIgnoreCase("Sent")) {
            sentMessages = append(sentMessages, message);
        } else if (flag.equalsIgnoreCase("Disregard")) {
            disregardedMessages = append(disregardedMessages, message);
        } else if (flag.equalsIgnoreCase("Stored")) {
            storedMessages = append(storedMessages, message);
            storeMessage(message);
        } else {
            return;
        }

        messageIDs = append(messageIDs, message.getMessageID());
        messageHashes = append(messageHashes, message.getMessageHash());
        messageCounter++;
    }

    private boolean storeMessage(Message message) {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(encode(message));
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Could not store message: " + e.getMessage());
            return false;
        }
    }

    public void loadStoredMessages() {
        List<Message> loaded = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Message message = decode(line);
                if (message != null) {
                    loaded.add(message);
                }
            }
        } catch (IOException e) {
            // The file may not exist until the first stored message is added.
        }

        storedMessages = loaded.toArray(new Message[0]);
        messageIDs = new String[0];
        messageHashes = new String[0];
        messageCounter = 0;
        for (Message message : storedMessages) {
            messageIDs = append(messageIDs, message.getMessageID());
            messageHashes = append(messageHashes, message.getMessageHash());
            messageCounter = Math.max(messageCounter, message.getMessageNumber());
        }
    }

    private String encode(Message message) {
        String[] fields = {
            message.getMessageID(), Integer.toString(message.getMessageNumber()),
            message.getRecipient(), message.getMessageText(), message.getSender(),
            message.getFlag(), message.getMessageHash()
        };
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) line.append('\t');
            line.append(Base64.getEncoder().encodeToString(
                    fields[i].getBytes(StandardCharsets.UTF_8)));
        }
        return line.toString();
    }

    private Message decode(String line) {
        try {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 7) return null;
            String[] values = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                values[i] = new String(Base64.getDecoder().decode(fields[i]),
                        StandardCharsets.UTF_8);
            }
            return new Message(values[0], Integer.parseInt(values[1]), values[2],
                    values[3], values[4], values[5], values[6]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void rewriteStoredMessagesFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Message message : storedMessages) {
                writer.write(encode(message));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not update stored messages: " + e.getMessage());
        }
    }

    public String displaySendersAndRecipients() {
        if (storedMessages.length == 0) return "There are no stored messages.";
        StringBuilder output = new StringBuilder();
        for (Message message : storedMessages) {
            output.append("Sender: ").append(message.getSender())
                    .append("\nRecipient: ").append(message.getRecipient())
                    .append("\n\n");
        }
        return output.toString();
    }

    public String findLongestStoredMessage() {
        if (storedMessages.length == 0) return "There are no stored messages.";
        Message longest = storedMessages[0];
        for (Message message : storedMessages) {
            if (message.getMessageText().length() > longest.getMessageText().length()) {
                longest = message;
            }
        }
        return longest.getMessageText();
    }

    public Message searchMessageByID(String id) {
        for (Message message : storedMessages) {
            if (message.getMessageID().equals(id)) return message;
        }
        for (Message message : sentMessages) {
            if (message.getMessageID().equals(id)) return message;
        }
        return null;
    }

    public String searchMessagesByRecipient(String recipient) {
        StringBuilder output = new StringBuilder();
        for (Message message : sentMessages) {
            if (message.getRecipient().equals(recipient)) {
                output.append(message.getMessageText()).append('\n');
            }
        }
        for (Message message : storedMessages) {
            if (message.getRecipient().equals(recipient)) {
                output.append(message.getMessageText()).append('\n');
            }
        }
        return output.toString();
    }

    public boolean deleteMessageByHash(String hash) {
        for (int i = 0; i < storedMessages.length; i++) {
            if (storedMessages[i].getMessageHash().equals(hash)) {
                Message deleted = storedMessages[i];
                System.out.println("Message: \"" + deleted.getMessageText()
                        + "\" successfully deleted.");
                storedMessages = removeAt(storedMessages, i);
                messageIDs = removeFirst(messageIDs, deleted.getMessageID());
                messageHashes = removeFirst(messageHashes, deleted.getMessageHash());
                rewriteStoredMessagesFile();
                return true;
            }
        }
        return false;
    }

    public String displayReport() {
        if (sentMessages.length == 0) return "No sent messages are available.";
        StringBuilder report = new StringBuilder("QUICKCHAT MESSAGE REPORT\n========================\n");
        for (Message message : sentMessages) {
            report.append("Message Hash: ").append(message.getMessageHash())
                    .append("\nRecipient: ").append(message.getRecipient())
                    .append("\nMessage: ").append(message.getMessageText())
                    .append("\n\n");
        }
        return report.toString();
    }

    private static Message[] append(Message[] values, Message value) {
        Message[] result = java.util.Arrays.copyOf(values, values.length + 1);
        result[values.length] = value;
        return result;
    }

    private static String[] append(String[] values, String value) {
        String[] result = java.util.Arrays.copyOf(values, values.length + 1);
        result[values.length] = value;
        return result;
    }

    private static Message[] removeAt(Message[] values, int index) {
        Message[] result = new Message[values.length - 1];
        System.arraycopy(values, 0, result, 0, index);
        System.arraycopy(values, index + 1, result, index, values.length - index - 1);
        return result;
    }

    private static String[] removeFirst(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                String[] result = new String[values.length - 1];
                System.arraycopy(values, 0, result, 0, i);
                System.arraycopy(values, i + 1, result, i, values.length - i - 1);
                return result;
            }
        }
        return values;
    }

    public Message[] getSentMessages() { return sentMessages.clone(); }
    public Message[] getDisregardedMessages() { return disregardedMessages.clone(); }
    public Message[] getStoredMessages() { return storedMessages.clone(); }
    public String[] getMessageHashes() { return messageHashes.clone(); }
    public String[] getMessageIDs() { return messageIDs.clone(); }
    public int getMessageCounter() { return messageCounter; }
}
