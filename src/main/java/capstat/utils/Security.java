package capstat.utils;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class Security {

    public static String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
}