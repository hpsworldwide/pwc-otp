package com.hpsworldwide.powercard.otp.mfa;

import com.hpsworldwide.powercard.otp.lib.img.BarCodeUtils;
import com.hpsworldwide.powercard.otp.lib.img.BarCodeUtils.BarcodeFormat;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base32;

/**
 * Managing software tokens<br>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238: TOTP: Time-Based
 * One-Time Password Algorithm</a>
 * @see <a href="https://github.com/wstrange/GoogleAuth">used GoogleAuth
 * library</a>
 *
 * @author (c) HPS Solutions
 */
public class OTP_Auth {

    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private static final BarcodeFormat BARCODE_FORMAT = BarcodeFormat.QR_CODE;
    private final SecureRandom secureRandom;
    private final Base32 base32;
    private final GoogleAuthenticator authEngine;

    public OTP_Auth(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        this.base32 = new Base32();
        this.authEngine = new GoogleAuthenticator();
    }

    /**
     * base32, upperCase<br>
     * e.g. for nbBytesOfRandomness=10 => 80 bits of randomness = 16 chars<br>
     * for nbBytesOfRandomness=16 => 128 bits of randomness = 26 chars<br>
     * for nbBytesOfRandomness=20 => 160 bits of randomness = 32 chars<br>
     * note : this element must be encrypted in database
     */
    public String generateSharedSecret(int nbBytesOfRandomness) {
        byte[] randomSeed = new byte[nbBytesOfRandomness];
        secureRandom.nextBytes(randomSeed);
        String secret = base32.encodeAsString(randomSeed);
        // erase randomSeed
        Arrays.fill(randomSeed, (byte) 0xFF);
        return secret;
    }

    /**
     * scratch codes are meant to be used when the user can't authenticate (for
     * any reason) using the regular software token application. At enrolment
     * time, the server generates a number of scratch codes (e.g. 5) that are
     * given to the user and stored in the server's database. The user must
     * store them securely. Their entropy must be non negligible (e.g. 128 bits
     * of entropy per scratch code). Each scratch code can only be used once,
     * the server must blacklist it after use (thus their name).<br>
     * base32, upperCase, 5 bits of randomness per char ; e.g. 128 bits of
     * randomness in 26 chars<br>
     * note : these elements must be encrypted in database
     */
    public String[] generateScratchCodes(int nbCharsPerCode, int nbCodes) {
        String[] codes = new String[nbCodes];
        int codeLength = (int) Math.ceil(nbCharsPerCode * 0.625); // 5/8
        byte[] baCode = new byte[codeLength];
        for (int i = 0; i < nbCodes; i++) {
            secureRandom.nextBytes(baCode);
            codes[i] = base32.encodeAsString(baCode).substring(0, nbCharsPerCode);
        }
        // erase baCode
        Arrays.fill(baCode, (byte) 0xFF);
        return codes;
    }

    /**
     * content of the QR Code displayed for user enrolment<br>
     * this text contains a shared secret, thus it must not be stored in plain
     * text
     *
     * @param issuer optional
     * @see
     * <a href="https://github.com/google/google-authenticator/wiki/Key-Uri-Format">QR
     * Code for enrolment format</a>
     */
    public static String getBarcodeContent(String label, String userAccountName, String secret, String issuer) throws UnsupportedEncodingException {
        String result = String.format("otpauth://totp/%s:%s?secret=%s", urlEncode(label), urlEncode(userAccountName), urlEncode(secret));
        if (issuer != null && !issuer.isEmpty()) {
            result += "&issuer=" + urlEncode(issuer);
        }
        return result;
    }

    private static String urlEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, CHARSET).replace("+", "%20");
    }

    /**
     * HTML img element containing PNG image (Base64 format) of a QR Code for
     * user enrolment (content in UTF-8)<br>
     * usage : <code>&lt;img src="data:image/png;base64,XXX" /&gt;</code> where
     * XXX is the result of this function<br>
     * this image contains a shared secret, therefore it must not be stored in
     * plain text
     *
     * @param issuer optional
     * @param barcodeSize in pixels
     */
    public static String getBase64PngImage(String label, String userLogin, String secret, String issuer, int barcodeSize) throws IOException {
        String barcodeContent = getBarcodeContent(label, userLogin, secret, issuer);
        return BarCodeUtils.toPngImageBase64(barcodeContent, barcodeSize, barcodeSize, BARCODE_FORMAT, CHARSET);
    }

    /**
     * PNG image (binary) of a QR Code for user enrolment (content in UTF-8)<br>
     * this image contains a shared secret, therefore it must not be stored in
     * plain text
     *
     * @param issuer optional
     * @param barcodeSize in pixels
     */
    public static void writePngImage(String label, String userLogin, String secret, String issuer, int barcodeSize, OutputStream outputStream) throws IOException {
        String barcodeContent = getBarcodeContent(label, userLogin, secret, issuer);
        BarCodeUtils.writePngImage(barcodeContent, barcodeSize, barcodeSize, BARCODE_FORMAT, CHARSET, outputStream);
    }

    /**
     * attention: server's time must be accurate, e.g. by using NTPsec (secure
     * Network Time Protocol)
     *
     * @param verificationCode usually 6 digits
     */
    public boolean authorize(String secret, int verificationCode) {
        return authEngine.authorize(secret, verificationCode);
    }

}
