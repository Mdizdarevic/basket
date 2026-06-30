package com.morenod.basket.security;

import com.morenod.basket.model.Donation;
import java.io.*;

public class SerializeUtil {

    // converting donation object to object outout stream for storage
    public static void serializeDonation(Donation donation, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(donation);
        }
    }

    // where im handling the magic bytes for serilaization
    private static void validateHeader(byte[] data) {
        if (data.length < 4 || data[0] != (byte)0xAC || data[1] != (byte)0xED || 
            data[2] != (byte)0x00 || data[3] != (byte)0x05) {
            throw new SecurityException("Invalid binary header!");
        }
    }

    // deserializing after validating the header only for wihtelisted data
    public static Object secureDeserialize(byte[] data) throws Exception {
        validateHeader(data);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             WhitelistObjectInputStream wois = new WhitelistObjectInputStream(bais)) {
            return wois.readObject();
        }
    }
}