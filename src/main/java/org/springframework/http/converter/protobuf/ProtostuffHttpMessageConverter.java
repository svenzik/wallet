package org.springframework.http.converter.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//import com.google.protobuf.spring.ExtensionRegistryInitializer;
//import com.googlecode.protobuf.format.CouchDBFormat;
//import com.googlecode.protobuf.format.HtmlFormat;
//import com.googlecode.protobuf.format.JsonFormat;
//import com.googlecode.protobuf.format.XmlFormat;

/**
 * @since Spring 3.0
 */
public class ProtostuffHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public static String LS = System.getProperty("line.separator");
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final MediaType PROTOBUF = new MediaType("application", "x-protobuf", DEFAULT_CHARSET);
    public static final MediaType XML = new MediaType("application", "xml", DEFAULT_CHARSET);
    public static final MediaType JSON = new MediaType("application", "json", DEFAULT_CHARSET);
    public static final MediaType TEXT = new MediaType("text", "plain");
    public static final MediaType HTML = new MediaType("text", "html");

//    private ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    private static final ConcurrentHashMap<Class, Method> newBuilderMethodCache = new ConcurrentHashMap<Class, Method>();

    public ProtostuffHttpMessageConverter() {
//        this(null);
//        super(PROTOBUF, JSON, HTML, TEXT, XML);
        super(PROTOBUF);
    }

//    public ProtobufHttpMessageConverter(ExtensionRegistryInitializer registryInitializer) {
//        super(PROTOBUF, JSON, HTML, TEXT, XML);
//        initializeExtentionRegistry(registryInitializer);
//    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }


    /**
     * Read from stream and write object
     *
     * @param clazz
     * @param inputMessage
     * @return
     * @throws java.io.IOException
     * @throws org.springframework.http.converter.HttpMessageNotReadableException
     */
    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        //set the content type of data
//        MediaType contentType = inputMessage.getHeaders().getContentType();
//        contentType = contentType != null ? contentType : PROTOBUF;

        //ParseFromIstream

        Schema<? extends Object> schema = RuntimeSchema.getSchema(clazz);
        Object result = schema.newMessage();

        ProtostuffIOUtil.mergeFrom(inputMessage.getBody(), result, (Schema<Object>) schema);

        return  result;

//        try {
//
//
//            ProtostuffIOUtil.writeTo();
//
//            builder.mergeFrom(inputMessage.getBody());
//
////            if (isJson(contentType)) {
////                String data = convertInputStreamToString(inputMessage.getBody());
////                String serverHeader = inputMessage.getHeaders().getFirst("Server");
////                if (serverHeader != null && serverHeader.contains("CouchDB"))
////                    CouchDBFormat.merge(data, extensionRegistry, builder);
////                else
////                    JsonFormat.merge(data, extensionRegistry, builder);
////
////            } else if (isText(contentType)) {
////                String data = convertInputStreamToString(inputMessage.getBody());
////                TextFormat.merge(data, extensionRegistry, builder);
////            } else if (isXml(contentType)) {
////                String data = convertInputStreamToString(inputMessage.getBody());
////                XmlFormat.merge(data, extensionRegistry, builder);
////            } else {
////                InputStream is = inputMessage.getBody();
////                builder.mergeFrom(is, extensionRegistry);
////            }
//
//            return builder.build();
//
//        } catch (Exception e) {
//            throw new HttpMessageNotReadableException("Unable to convert inputMessage to Proto object", e);
//        }
    }

    /**
     * Write data to output
     * @param data
     * @param outputMessage
     * @throws java.io.IOException
     * @throws org.springframework.http.converter.HttpMessageNotWritableException
     */
    @Override
    protected void writeInternal(Object data, HttpOutputMessage outputMessage)
                                throws IOException, HttpMessageNotWritableException {

//        MediaType contentType = outputMessage.getHeaders().getContentType();
//        Charset charset = contentType.getCharSet() != null ? contentType.getCharSet() : DEFAULT_CHARSET;

        //SerializeToOstream
        Schema<?> schema = RuntimeSchema.getSchema(data.getClass());
        //TODO: It is better to re-use the buffer (application/threadlocal buffer)
        // to avoid buffer allocation everytime you serialize.
        LinkedBuffer buffer = LinkedBuffer.allocate(512);

        try {
            ProtostuffIOUtil.writeTo(outputMessage.getBody(), data, (Schema<Object>) schema, buffer);
        } finally {
            buffer.clear();
        }


//        if (isHtml(contentType)) {
//            String data = HtmlFormat.printToString(data);
//            FileCopyUtils.copy(data.getBytes(charset), outputMessage.getBody());
//        } else if (isJson(contentType)) {
//            String data = JsonFormat.printToString(data);
//            FileCopyUtils.copy(data.getBytes(charset), outputMessage.getBody());
//        } else if (isText(contentType)) {
//            String data = TextFormat.printToString(data);
//            FileCopyUtils.copy(data.getBytes(charset), outputMessage.getBody());
//        } else if (isXml(contentType)) {
//            String data = XmlFormat.printToString(data);
//            FileCopyUtils.copy(data.getBytes(charset), outputMessage.getBody());
//        } else {
//            FileCopyUtils.copy(data.toByteArray(), outputMessage.getBody());
//        }
    }

    @Override
    protected MediaType getDefaultContentType(Object message) {
        return PROTOBUF;
    }

//    @Override
//    protected Long getContentLength(Object data, MediaType contentType) {
//        Charset charset = contentType.getCharSet() != null ? contentType.getCharSet() : DEFAULT_CHARSET;
//
//        if (isHtml(contentType)) {
//            String data = HtmlFormat.printToString(data);
//            return (long) data.getBytes(charset).length;
//        } else if (isJson(contentType)) {
//            String data = JsonFormat.printToString(data);
//            return (long) data.getBytes(charset).length;
//        } else if (isText(contentType)) {
//            String data = TextFormat.printToString(data);
//            return (long) data.getBytes(charset).length;
//        } else if (isXml(contentType)) {
//            String data = XmlFormat.printToString(data);
//            return (long) data.getBytes(charset).length;
//        } else {
//            return (long) data.toByteArray().length;
//        }
//    }

//    protected boolean isJson(MediaType contentType) {
//        return JSON.getType().equals(contentType.getType()) && JSON.getSubtype().equals(contentType.getSubtype());
//    }
//
//    protected boolean isText(MediaType contentType) {
//        return TEXT.getType().equals(contentType.getType()) && TEXT.getSubtype().equals(contentType.getSubtype());
//    }
//
//    protected boolean isXml(MediaType contentType) {
//        return XML.getType().equals(contentType.getType()) && XML.getSubtype().equals(contentType.getSubtype());
//    }
//
//    protected boolean isHtml(MediaType contentType) {
//        return HTML.getType().equals(contentType.getType()) && HTML.getSubtype().equals(contentType.getSubtype());
//    }

    private Method getNewBuilderMessageMethod(Class<? extends Object> clazz) throws NoSuchMethodException {
        Method m = newBuilderMethodCache.get(clazz);
        if (m == null) {
            m = clazz.getMethod("newBuilder");
            newBuilderMethodCache.put(clazz, m);
        }
        return m;
    }

//    private void initializeExtentionRegistry(ExtensionRegistryInitializer registryInitializer) {
//        if (registryInitializer != null) {
//            registryInitializer.initializeExtensionRegistry(extensionRegistry);
//        }
//    }

//    public static String convertInputStreamToString(InputStream io) {
//        StringBuffer sb = new StringBuffer();
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(io, "UTF-8"));
//            String line = reader.readLine();
//            while (line != null) {
//                sb.append(line).append(LS);
//                line = reader.readLine();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Unable to obtain an InputStream", e);
//
//        }
//        return sb.toString();
//    }

}