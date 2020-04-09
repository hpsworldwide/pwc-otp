package com.hpsworldwide.powercard.otp.mfa;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 *
 * @author (c) HPS Solutions
 */
public class TestScratchCodes {

    
    public static void main(String[] args) {
        OTP_Auth otpAuth = new OTP_Auth(new SecureRandom());
        test(16, 10, otpAuth);
        test(1, 1, otpAuth);
        test(2, 1, otpAuth);
        test(3, 1, otpAuth);
    }
    
    public static void test(int nbChars, int nbCodes, OTP_Auth otpAuth) {
        System.out.println(Arrays.toString(otpAuth.generateScratchCodes(nbChars, nbCodes)));
    }
}
