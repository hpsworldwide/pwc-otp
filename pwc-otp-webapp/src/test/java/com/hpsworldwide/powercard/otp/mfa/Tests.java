package com.hpsworldwide.powercard.otp.mfa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;

/**
 * see https://github.com/wstrange/GoogleAuth<br>
 * https://github.com/google/google-authenticator/wiki/Key-Uri-Format
 *
 * @author (c) HPS Solutions
 */
public class Tests {

    public static void main(String[] args) {
        GoogleAuthenticator serverGAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = serverGAuth.createCredentials();
        String sharedSecret = key.getKey();
        byte[] randomSeed = new byte[10];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomSeed);
        sharedSecret = new Base32().encodeAsString(randomSeed);

        String label = "HPS_Auth2";
        String userLogin = "abdeslam@hps.ma";
        String issuer = "HPS";
        // TODO : String.format
        String barcodeContent = "otpauth://totp/" + label + ":" + userLogin + "?secret=" + sharedSecret + "&issuer=" + issuer;
        System.out.println("sharedSecret: " + sharedSecret);
        System.out.println("barcodeContent: " + barcodeContent);
        testClient(sharedSecret);
        testIsCodeValid("IQUMQN3TFJCSNFI7", 116939);
    }

    private static void testClient(String sharedSecret) {
        GoogleAuthenticator clientGAuth = new GoogleAuthenticator();
        int totpCode = clientGAuth.getTotpPassword(sharedSecret);
        System.out.println("TOTP code: " + totpCode);
        testIsCodeValid(sharedSecret, totpCode);
    }

    private static void testIsCodeValid(String sharedSecret, int totpCode) {
        GoogleAuthenticator serverGAuth = new GoogleAuthenticator();
        boolean isCodeValid = serverGAuth.authorize(sharedSecret, totpCode);
        boolean isCodeValidP1 = serverGAuth.authorize(sharedSecret, (totpCode + 1));
        System.out.println("sharedSecret: " + sharedSecret);
        System.out.println("isCodeValid? " + isCodeValid);
        System.out.println("isCodeValidP1? " + isCodeValidP1);
    }

}
