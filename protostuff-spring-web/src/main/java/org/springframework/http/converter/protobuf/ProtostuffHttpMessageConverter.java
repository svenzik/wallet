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
import java.util.Arrays;
import java.util.List;
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

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final MediaType PROTOSTUFF = new MediaType("application", "x-protostuff", DEFAULT_CHARSET);
    private static final MediaType PROTOBUF = new MediaType("application", "x-protobuf", DEFAULT_CHARSET);
    private static final MediaType XML = new MediaType("application", "xml", DEFAULT_CHARSET);
    private static final MediaType JSON = new MediaType("application", "json", DEFAULT_CHARSET);
    private static final MediaType TEXT = new MediaType("text", "plain");
    private static final MediaType HTML = new MediaType("text", "html");

    private static final List<MediaType> protostuffSupportedMediaTypes = Arrays.asList(new MediaType[]{
            PROTOSTUFF
//            ,
//            PROTOBUF,
//            XML,
//            JSON,
//            TEXT,
//            HTML
    });

//    private ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    private static final ConcurrentHashMap<Class, Method> newBuilderMethodCache = new ConcurrentHashMap<Class, Method>();

    public ProtostuffHttpMessageConverter() {
        super(PROTOSTUFF);
    }

    public ProtostuffHttpMessageConverter(String mediaType) {
        super(MediaType.parseMediaType(mediaType));
    }

    public ProtostuffHttpMessageConverter(List<String> mediaTypes) {
        super(filterCompatibleMediaTypesAndConvertToArray(mediaTypes));
    }

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

        Schema<? extends Object> schema = RuntimeSchema.getSchema(clazz);
        Object result = schema.newMessage();

        ProtostuffIOUtil.mergeFrom(inputMessage.getBody(), result, (Schema<Object>) schema);

        return  result;
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

        Schema<?> schema = RuntimeSchema.getSchema(data.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(512);

        try {
            ProtostuffIOUtil.writeTo(outputMessage.getBody(), data, (Schema<Object>) schema, buffer);
        } finally {
            buffer.clear();
        }

    }


    /**
     * Helper method to convert string list to MediaType array and filter only supported media types
     * This is here because AbstractHttpMessageConverter supports Array of media types,
     * but it is easies to use list of strings in configuration files
     * @param mediaTypes List of media types to support
     * @return  Sublist of only supported media types
     */
    private static MediaType[] filterCompatibleMediaTypesAndConvertToArray(List<String> mediaTypes) {

        List<MediaType> result = new ArrayList<MediaType>(mediaTypes.size());

        for (String mediaTypeString : mediaTypes ) {
            MediaType mediaType = getCompatibleMediaType(MediaType.parseMediaType(mediaTypeString));
            if (mediaType != null) {
                result.add(mediaType);
            }
        }

        return result.toArray(new MediaType[result.size()]);
    }

    /**
     * Checks if media type should be added as supported media types, returns null if not
     * @param mediaType User specified media type
     * @return true, if it is compatible with protostuff, else null
     */
    protected static MediaType getCompatibleMediaType(MediaType mediaType) {
        for (MediaType protostuffCompatibleMediaType : protostuffSupportedMediaTypes) {
            if (protostuffCompatibleMediaType.isCompatibleWith(mediaType)) {
                return protostuffCompatibleMediaType;
            }
        }
        return null;
    }

}