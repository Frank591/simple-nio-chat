package ru.fsl.chat.contracts.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrefixedMessageTest {

    @Test
    public void ResponseMessageToStringTest(){
        PrefixedMessage responseMessage = new PrefixedMessage(200, "test");
        assertEquals("200test", responseMessage.toString());

        PrefixedMessage responseMessage1 = new PrefixedMessage(500, "test1");
        assertEquals("500test1", responseMessage1.toString());

        PrefixedMessage responseMessage2 = new PrefixedMessage(500, null);
        assertEquals("500", responseMessage2.toString());

        PrefixedMessage responseMessage3 = new PrefixedMessage(500, "");
        assertEquals("500", responseMessage3.toString());
    }

    @Test
    public void ResponseMessageParseTest(){
        PrefixedMessage responseMessage1 = PrefixedMessage.parse("200test");
        assertEquals(200, responseMessage1.getCode());
        assertEquals("test", responseMessage1.getBody());

        PrefixedMessage responseMessage2 = PrefixedMessage.parse("500test1");
        assertEquals(500, responseMessage2.getCode());
        assertEquals("test1", responseMessage2.getBody());

        PrefixedMessage responseMessage3 = PrefixedMessage.parse("300");
        assertEquals(300, responseMessage3.getCode());
        Assertions.assertNull(responseMessage3.getBody());

        PrefixedMessage responseMessage4 = PrefixedMessage.parse("102hello,world from 1");
        assertEquals(102, responseMessage4.getCode());
        assertEquals("hello,world from 1",responseMessage4.getBody());
    }


    @Test
    public void ResponseMessageWrongInputParamsTest(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PrefixedMessage(-1, "test"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PrefixedMessage(0, "test"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PrefixedMessage(1000, "test"));
    }

}
