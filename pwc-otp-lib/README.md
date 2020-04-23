<img src="https://www.hps-worldwide.com/sites/default/files/logo_hps_0.png" width="40%">

# PowerCARD OTP Lib

### Multi-factor Authentication (MFA)

#### Step 1 : User enrolment
Generate 1 shared secret (e.g. 160 bits, nbBytesOfRandomness = 20) with 
`OTP_Auth.generateSharedSecret`<br>
Store this secret in server's database (must be encrypted, can't be hashed)
<br><br>
Ask user to download on its smartphone an Authenticator app, can be :
* Google Authenticator (https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)
* Microsoft Authenticator (https://play.google.com/store/apps/details?id=com.azure.authenticator)
* Authy (https://authy.com/)
* ...

Display this secret to the user with QR-Code using `OTP_Auth.getBase64PngImage` or `OTP_Auth.writePngImage`

Ask the user to scan the QR-Code

If the user can't scan the QR-Code using its phone, display information manually (account name, shared secret)
using `OTP_Auth.generateScratchCodes`<br> 
Generate a certain number of scratch codes (e.g. 5 scratch codes of 26 chars each) and send them to the user (by e-mail, ask them to print) and store this single-usage codes encrypted in database (note : they can be encrypted or securely hashed (with key) in database)


#### Step 2 : user verification
Ask the user to consult the authenticator mobile app and type the 6 digits secret code, verify the secret code with `OTP_Auth.authoriz`e (note : server's time must be accurate, e.g. by using NTPsec (secure Network Time Protocol))

Should the users loose his phones, can't log with the TOTP process, web server should step to scratch code verification (note: once a scratch code has been used, it should be permanently blacklisted)