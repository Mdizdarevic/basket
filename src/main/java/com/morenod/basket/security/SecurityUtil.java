package com.morenod.basket.security;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;

@Component
public class SecurityUtil {

    private static final List<String> allowlist = 
    List.of(
        "api.basket.com", 
        "api.stripe.com", // if in the future i wanna make a strip payment system
        "maps.googleapis.com" // if in the future i wanna make a "locate food bank" feature
    );
    
    private static final List<String> blocklist = 
    List.of(
        "127.", 
        "10.", 
        "169.254"
    );

    public boolean isUrlSafe(String url) {
        try {
            String host = URI.create(url).getHost();
            // if (host.equals("localhost")) return false;
            if (host == null || !allowlist.contains(host)) {return false;}
            
            for (String prefix : blocklist) {
                if (host.startsWith(prefix)) {return false;}
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}