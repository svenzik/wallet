package org.springframework.http.converter.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import java.io.*;

import org.junit.Test;
import org.junit.Assert;

/**
 */
public class ProtostuffHttpMessageConverterTest {

    private ProtostuffTestData originalData;
    private byte[] serializedData;


    private ProtostuffTestData getOriginalData(){
        if (originalData == null) {
            originalData = new ProtostuffTestData("asdf", 5);
        }
        return originalData;
    }

    private byte[] getSerializedData() {
        if (serializedData == null) {

            Schema<?> schema = RuntimeSchema.getSchema(getOriginalData().getClass());
            LinkedBuffer buffer = LinkedBuffer.allocate(512);

            try {
                serializedData = ProtostuffIOUtil.toByteArray(getOriginalData(), (Schema<Object>) schema, buffer);
            } finally {
                buffer.clear();
            }
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
        ProtostuffTestData result = (ProtostuffTestData) new ProtostuffHttpMessageConverter().readInternal(getOriginalData().getClass(), httpInputMessage);

        Assert.assertEquals(originalData.getClass().getCanonicalName(), result.getClass().getCanonicalName());
        Assert.assertEquals(originalData.stringField, result.stringField);
        Assert.assertEquals(originalData.intField, result.intField);
    }

    @Test
    public void testWriteInternal() throws Exception {

        new ProtostuffHttpMessageConverter().writeInternal(getOriginalData(), httpOutputMessage);

        Assert.assertArrayEquals(getSerializedData(), ((ByteArrayOutputStream) httpOutputMessage.getBody()).toByteArray());

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
