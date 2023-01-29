package tools;

import java.io.*;

public class EncoderDecoder {

    /**
     * Encodes object in byte arrays (!!! Object must implement Serializable)
     * @param object        : Object to be encoded
     * @return              : encoded byte array
     */
    public static byte[] encode(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decode byte array into object (!!! Object must implement Serializable)
     * @param bytes     : bytes array to be decoded
     * @return          : object
     */
    public static Object decode(byte[] bytes){
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
