import java.security.SecureRandom;

public class Message {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String sender;
    private final String recipient;
    private final String messageText;
    private final String messageID;
    private final String messageHash;
    private final int messageNumber;
    private String flag;

    public Message(String sender, String recipient, String messageText,
                   int messageNumber, String flag) {
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageNumber = messageNumber;
        this.flag = flag;
        this.messageID = generateMessageID();
        this.messageHash = createMessageHash();
    }

    // Constructor for restoring a message with its saved ID and hash.
    public Message(String sender, String recipient, String messageText,
                   int messageNumber, String messageID, String messageHash,
                   String flag) {
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageNumber = messageNumber;
        this.messageID = messageID;
        this.messageHash = messageHash;
        this.flag = flag;
    }

    private String generateMessageID() {
        StringBuilder id = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            id.append(RANDOM.nextInt(10));
        }
        return id.toString();
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("\\d{10}");
    }

    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+\\d{1,15}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted "
                + "or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    public String validateMessage() {
        if (messageText == null || messageText.trim().isEmpty()) {
            return "Please enter a message.";
        }
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        }
        int excess = messageText.length() - 250;
        return "Message exceeds 250 characters by " + excess
                + ", please reduce the size.";
    }

    public String createMessageHash() {
        if (!checkMessageID() || messageText == null
                || messageText.trim().isEmpty()) {
            return "";
        }

        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1]
                .replaceAll("[^A-Za-z0-9]", "");

        return messageID.substring(0, 2) + ":" + messageNumber + ":"
                + (firstWord + lastWord).toUpperCase();
    }

    // 1 = Send, 2 = Disregard, 3 = Store.
    public String SentMessage(int choice) {
        String validation = validateMessage();
        if (!"Message ready to send.".equals(validation)) {
            return validation;
        }

        String recipientValidation = checkRecipientCell();
        if (!"Cell phone number successfully captured."
                .equals(recipientValidation)) {
            return recipientValidation;
        }

        if (choice == 1) {
            flag = "Sent";
            return "Message successfully sent.";
        }
        if (choice == 2) {
            flag = "Disregard";
            return "Press 0 to delete the message.";
        }
        if (choice == 3) {
            flag = "Stored";
            return "Message successfully stored.";
        }
        return "Invalid option selected.";
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

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public static void main(String[] args) {
        Message message = new Message(
                "Alice", "+27123456789", "Hello there!", 1, "Pending");

        System.out.println("Message ID: " + message.getMessageID());
        System.out.println("Message hash: " + message.getMessageHash());
        System.out.println(message.SentMessage(1));
        System.out.println("Status: " + message.getFlag());
    }
}
