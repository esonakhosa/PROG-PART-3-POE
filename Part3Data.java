import java.util.Arrays;

public class Part3Data {
    private String[][] sentMessages = new String[0][5];
    private String[][] disregardedMessages = new String[0][5];
    private String[][] storedMessages = new String[0][5];

    private String[] messageHashes = new String[0];
    private String[] messageIDs = new String[0];

    // Adds a message to the array for its status.
    public void addMessage(String status, String sender, String recipient,
                           String message, String messageID, String messageHash) {
        String[] details = {sender, recipient, message, messageID, messageHash};

        if (status.equalsIgnoreCase("Sent")) {
            sentMessages = addRow(sentMessages, details);
        } else if (status.equalsIgnoreCase("Stored")) {
            storedMessages = addRow(storedMessages, details);
        } else if (status.equalsIgnoreCase("Disregarded")) {
            disregardedMessages = addRow(disregardedMessages, details);
        } else {
            throw new IllegalArgumentException(
                    "Status must be Sent, Stored, or Disregarded.");
        }

        messageIDs = Arrays.copyOf(messageIDs, messageIDs.length + 1);
        messageIDs[messageIDs.length - 1] = messageID;

        messageHashes = Arrays.copyOf(messageHashes, messageHashes.length + 1);
        messageHashes[messageHashes.length - 1] = messageHash;
    }

    private String[][] addRow(String[][] messages, String[] details) {
        String[][] updatedMessages = Arrays.copyOf(messages, messages.length + 1);
        updatedMessages[updatedMessages.length - 1] = details;
        return updatedMessages;
    }

    public String[][] getSentMessages() {
        return sentMessages;
    }

    public String[][] getDisregardedMessages() {
        return disregardedMessages;
    }

    public String[][] getStoredMessages() {
        return storedMessages;
    }

    public String[] getMessageHashes() {
        return messageHashes;
    }

    public String[] getMessageIDs() {
        return messageIDs;
    }

    public static void main(String[] args) {
        Part3Data data = new Part3Data();
        data.addMessage("Sent", "Alice", "Bob", "Hello, Bob!", "ID001", "HASH001");

        System.out.println("Sent messages: " + data.getSentMessages().length);
        System.out.println("Message IDs: " + Arrays.toString(data.getMessageIDs()));
        System.out.println("Message hashes: " + Arrays.toString(data.getMessageHashes()));
    }
}
