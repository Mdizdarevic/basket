package com.morenod.basket.security;

import java.io.*;
import java.util.List;

public class WhitelistObjectInputStream extends ObjectInputStream {

    private static final List<String> whitelist = List.of(
        "com.morenod.basket.model.Donation",
        "com.morenod.basket.model.User",
        "java.lang.Long",    
        "java.lang.Integer",
        "java.lang.String",
        "java.lang.Number"
    );

    public WhitelistObjectInputStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    // checking if untrusted class is there, if yes, abort deserialization
    @Override // overriding resolveclass
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        if (!whitelist.contains(desc.getName())) {
            throw new InvalidClassException("Unauthorized deserialization attempt: ", desc.getName());
        }
        return super.resolveClass(desc);
    }
}