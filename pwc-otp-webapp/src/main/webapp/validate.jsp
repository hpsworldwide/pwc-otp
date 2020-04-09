<%-- 
    Document   : validate
    Created on : 20 oct. 2017, 13:51:36
    Author     : HPS Solutions
--%>

<%@page import="java.security.SecureRandom"%>
<%@page import="com.hpsworldwide.powercard.otp.mfa.OTP_Auth"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String sharedSecret = request.getParameter("shared_secret");
    String sVerificationCode = request.getParameter("verification_code");
    int iVerificationCode = Integer.parseInt(sVerificationCode);
    boolean isValid = new OTP_Auth(null).authorize(sharedSecret, iVerificationCode);
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test OTP Auth</title>
        <style>
            body {
                font-family: sans-serif;
            }
        </style>
    </head>
    <body>
        <h1>Check Authentication</h1>
        isValid? <%=isValid%>
    </body>
</html>
