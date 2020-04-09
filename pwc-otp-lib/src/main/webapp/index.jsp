<%-- 
    Document   : index
    Created on : 19 oct. 2017, 17:43:28
    Author     : HPS Solutions
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.security.SecureRandom"%>
<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="com.hpsworldwide.powercard.otp.mfa.otp.OTP_Auth"%>
<%@page import="com.hpsworldwide.powercard.otp.lib.img.BarCodeUtils" %>
<%@page import="com.hpsworldwide.powercard.otp.lib.img.BarCodeUtils.BarcodeFormat"%>
<%
    int barcodeSize = 200;
    String label = "e-Banking [💳] [✓]"; // weird characters for test emoji + UTF-8 support
    String issuer = "myBank [🏦] [✓]";
    String userAccountName = "john.smith@mybank.com";
    int nbBytesOfRandomness = 20;
    String sharedSecret = new OTP_Auth(new SecureRandom()).generateSharedSecret(nbBytesOfRandomness);
    String barcodeHtmlImage = OTP_Auth.getBase64PngImage(label, userAccountName, sharedSecret, issuer, barcodeSize);
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test OTP Auth</title>
        <link type="image/vnd.microsoft.icon" rel="shortcut icon" href="favicon.ico" />
        <style>
            body {
                font-family: sans-serif;
            }
        </style>
    </head>
    <body>
        <h1>Test OTP Auth</h1>
        <a href="manual.txt" target="_blank">Dev. Manual</a>
        <h2>Enrolment</h2>
        [🏦] [✓]<br>
        scan this QR Code with the Google Authenticator mobile app (<a href="https://itunes.apple.com/app/google-authenticator/id388497605?mt=8" target="_blank">iTunes</a>, <a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank">Google Play</a>):<br>
        <img src="data:image/png;base64,<%=barcodeHtmlImage%>" />
        <hr>
        <h2>Code verification</h2>
        <form action="validate.jsp" method="post">
            <input type="hidden" name="shared_secret" value="<%=sharedSecret%>" />
            <label for="verification_code">verification code: </label><input type="text" id="verification_code" name="verification_code" autocomplete="off" autofocus /><br>
            <input type="submit" value="authorization" />
        </form>
    </body>
</html>
