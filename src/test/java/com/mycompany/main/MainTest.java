package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {

    // Test data 1 and 2 from the POE
    private String[] msg1;
    private String[] msg2;

    @BeforeEach
    void setUp() {
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
}
