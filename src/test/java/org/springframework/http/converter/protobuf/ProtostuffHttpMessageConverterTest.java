package org.springframework.http.converter.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.playtech.wallet.domain.Player;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.protobuf.ProtostuffHttpMessageConverter;

import java.io.*;
import java.math.BigDecimal;

import org.junit.Test;
import org.junit.Assert;

/**
 * Created by svenzik on 3/13/14.
 */
public class ProtostuffHttpMessageConverterTest {

    private ProtostuffTestData data;
//    private Player data;
    private byte[] serializedData;

//    private Player getData(){
//        if (data == null) {
//            data = new Player();
//            data.setUsername(this.getClass().getName());
//            data.changeBalance(new BigDecimal(1.00));
//        }
//        return data;
//    }
    private ProtostuffTestData getData(){
        if (data == null) {
            data = new ProtostuffTestData("asdf", 5);
        }
        return data;
    }

    private byte[] getSerializedData() {
        if (serializedData == null) {

//            Schema<Player> schema = RuntimeSchema.getSchema(Player.class);
            Schema<?> schema = RuntimeSchema.getSchema(getData().getClass());
            LinkedBuffer buffer = LinkedBuffer.allocate(512);

            serializedData = ProtostuffIOUtil.toByteArray(getData(), (Schema<Object>) schema, buffer);
//            try {
//            } finally {
//                buffer.clear();
//            }
        }
        return serializedData;
    }

    //helpers
    HttpInputMessage httpInputMessage = new HttpInputMessage() {
        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(getSerializedData());
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }
    };

    HttpOutputMessage httpOutputMessage = new HttpOutputMessage() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        @Override
        public OutputStream getBody() throws IOException {
            return outputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }
    };


    //TESTS

    @Test
    public void testReadInternal() throws Exception {
        Object result = new ProtostuffHttpMessageConverter().readInternal(getData().getClass(), httpInputMessage);
//        Player result = (Player) new ProtostuffHttpMessageConverter().readInternal(getData().getClass(), httpInputMessage);

        this.assertEquals(getData(), (ProtostuffTestData) result);
    }

    @Test
    public void testWriteInternal() throws Exception {

        new ProtostuffHttpMessageConverter().writeInternal(getData(), httpOutputMessage);

        Assert.assertArrayEquals(getSerializedData(), ((ByteArrayOutputStream) httpOutputMessage.getBody()).toByteArray());

    }

    /**
     * Test helper
     * @param original
     * @param result
     */
    private void assertEquals(ProtostuffTestData original, ProtostuffTestData result) {
        Assert.assertEquals(original.getClass().getCanonicalName(), result.getClass().getCanonicalName());
        Assert.assertEquals(original.stringField, result.stringField);
        Assert.assertEquals(original.intField, result.intField);
    }

    /**
     * Test helper
     * @param original
     * @param result
     */
    private void assertEquals(Player original, Player result) {
        Assert.assertEquals(original.getClass().getCanonicalName(), result.getClass().getCanonicalName());
        Assert.assertEquals(original.getUsername(), result.getUsername());
        Assert.assertEquals(original.getBalance(), result.getBalance());
    }




    //must be static
    private static class ProtostuffTestData {

        private ProtostuffTestData(String stringField, int intField) {
            this.stringField = stringField;
            this.intField = intField;
        }

        private String stringField;
        private int intField;

    }
}
