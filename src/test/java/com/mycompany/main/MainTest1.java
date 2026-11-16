package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {

    // Test data 1 and 2 from the POE
    private String[] msg1;
    private String[] msg2;

    @BeforeEach
    void setUp() throws IOException {
        // Use a temporary JSON file so the tests never touch real data
        File temp = Files.createTempFile("quickchat-test", ".json").toFile();
        temp.deleteOnExit();
        Main.jsonFile = temp.getAbsolutePath();
        Main.resetAll();
        msg1 = Main.createMessage("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        msg2 = Main.createMessage("0098765432", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
    }

    @Test
    void messageLength_success() {
        assertEquals("Message sent",
                Main.checkMessageLength("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    void messageLength_failure() {
        assertEquals("Please enter a message of less than 250 characters.",
                Main.checkMessageLength("a".repeat(251)));
    }

    @Test
    void recipientNumber_success() {
        assertEquals("Cell phone number successfully captured.",
                Main.checkRecipientCell("+27718693002"));
    }

    @Test
    void recipientNumber_failure() {
        assertEquals("Cell phone number is incorrectly formatted or does "
                + "not contain an international code. "
                + "Please correct the number and try again.",
                Main.checkRecipientCell("08575975889"));
    }

    @Test
    void messageHash_testCase1() {
        assertEquals("00:0:HITONIGHT", msg1[1]);
    }

    @Test
    void messageHash_remainingMessagesInLoop() {
        String[] ids = {"0012345678", "0098765432", "1234567890"};
        String[] texts = {
            "Hi Mike, can you join us for dinner tonight?",
            "Hi Keegan, did you receive the payment?",
            "Hi, thanks!"
        };
        String[] expected = {"00:0:HITONIGHT", "00:1:HIPAYMENT", "12:2:HITHANKS"};

        for (int i = 0; i < texts.length; i++) {
            assertEquals(expected[i],
                    Main.createMessageHash(ids[i], i, texts[i]));
        }
    }

    @Test
    void messageID_isCreated() {
        String id = Main.generateMessageID();
        assertTrue(Main.checkMessageID(id));
        assertEquals(10, id.length());
        System.out.println("Message ID generated: " + id);
    }

    @Test
    void sentMessage_send() {
        assertEquals("Message successfully sent.", Main.SentMessage(msg1, 1));
    }

    @Test
    void sentMessage_disregard() {
        assertEquals("Press 0 to delete the message.", Main.SentMessage(msg2, 2));
    }

    @Test
    void sentMessage_store() {
        assertEquals("Message successfully stored.", Main.SentMessage(msg1, 3));
    }

    @Test
    void totalMessagesSent_afterSendAndDiscard() {
        Main.SentMessage(msg1, 1);   // Test case 1: send
        Main.SentMessage(msg2, 2);   // Test case 2: discard
        assertEquals(1, Main.returnTotalMessages());
    }

    @Test
    void printMessages_containsSentMessageOnly() {
        Main.SentMessage(msg1, 1);
        Main.SentMessage(msg2, 2);
        String printed = Main.printMessages();
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertFalse(printed.contains("PAYMENT"));
    }

    // ===================== Part 3 =====================

    // Populates the arrays with the five test messages (no hard-coding in Main)
    private String[][] populateTestData() {
        String[][] data = {
            {"0000000001", "+27834557896", "Did you get the cake?"},
            {"0000000002", "+27838884567",
                "Where are you? You are late! I have asked you to be on time."},
            {"0000000003", "+27834484567", "Yohoooo, I am at your gate."},
            {"0000000004", "0838884567", "It is dinner time!"},
            {"0000000005", "+27838884567", "Ok, I am leaving without you."}
        };
        // Flags: 1 = Sent, 3 = Stored, 2 = Disregard
        int[] flags = {1, 3, 2, 1, 3};

        String[][] messages = new String[5][];
        for (int i = 0; i < data.length; i++) {
            messages[i] = Main.createMessage(data[i][0], i, data[i][1], data[i][2]);
            Main.SentMessage(messages[i], flags[i]);
        }
        return messages;
    }

    @Test
    void sentMessagesArray_correctlyPopulated() {
        populateTestData();
        List<String> expected = List.of("Did you get the cake?", "It is dinner time!");
        assertEquals(expected, Main.getSentMessageTexts());
    }

    @Test
    void disregardedAndStoredArrays_correctlyPopulated() {
        populateTestData();
        assertEquals(1, Main.disregardedMessages.size());
        assertEquals(2, Main.storedMessages.size());
        assertEquals(5, Main.messageIDs.size());
        assertEquals(5, Main.messageHashes.size());
    }

    @Test
    void displayLongestMessage() {
        populateTestData();
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Main.displayLongestMessage());
    }

    @Test
    void searchForMessageID() {
        populateTestData();
        assertEquals("Recipient: 0838884567\nMessage: It is dinner time!",
                Main.searchByMessageID("0000000004"));
    }

    @Test
    void searchForMessageID_notFound() {
        populateTestData();
        assertEquals("Message ID not found.", Main.searchByMessageID("9999999999"));
    }

    @Test
    void searchMessagesForRecipient() {
        populateTestData();
        List<String> expected = List.of(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you.");
        assertEquals(expected, Main.searchByRecipient("+27838884567"));
    }

    @Test
    void deleteMessageUsingHash() {
        String[][] messages = populateTestData();
        String hashOfMessage2 = messages[1][1];
        assertEquals("Message: \"Where are you? You are late! I have asked "
                + "you to be on time.\" successfully deleted.",
                Main.deleteByMessageHash(hashOfMessage2));
        assertEquals(1, Main.storedMessages.size());
        assertEquals("Message hash not found.", Main.deleteByMessageHash(hashOfMessage2));
    }

    @Test
    void displayReport_listsHashRecipientAndMessage() {
        populateTestData();
        String report = Main.displayReport(Main.sentMessages);
        assertTrue(report.contains("Message Hash: 00:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message: It is dinner time!"));
        assertFalse(report.contains("leaving without you"));
    }

    @Test
    void storedMessages_areReadBackFromJson() {
        populateTestData();
        int before = Main.storedMessages.size();
        Main.loadStoredMessages();
        assertEquals(before, Main.storedMessages.size());
        assertEquals("Ok, I am leaving without you.", Main.storedMessages.get(1)[3]);
    }
}
