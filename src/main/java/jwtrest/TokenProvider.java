package jwtrest;

import static jwtrest.Constants.REMEMBERME_VALIDITY_SECONDS;

import io.jsonwebtoken.*;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * TokenProvider: create and validate RSA-signed JWTs.
 * Keep private/public keys safe; for demo we embed strings similar to your sample.
 */
@Named
@ApplicationScoped
public class TokenProvider implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(TokenProvider.class.getName());
    private static final String AUTHORITIES_KEY = "auth";

    private String privateKey;
    private String publicKey;
    private PrivateKey myprivateKey;
    private PublicKey mypublicKey;
    private long tokenValidity;
    private long tokenValidityForRememberMe;

    @PostConstruct
    public void init() {
        // Replace values with your configured keys or load from config.
        this.privateKey = "-----BEGIN PRIVATE KEY-----\n" +
"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDscdPaVzw4gVdZ\n" +
"Tn3/ObPwn09fh69T0sb1JhqyfjWjWmGlEpDyIm8/7CwfeJg8IvE9psKERdMVGBdu\n" +
"nlaNNrV9gwaYOm6A+Mq0pyaf1qYbvLNlH/Bxs9aGdljWA/p0sG3Q9QfK7pfXo+ON\n" +
"StdRgT2XQ6ntaF5SS0qrl3eH4vj8cA4bqtXEnecs8hvZ5oKvqH9Ic+hjVj66I2FG\n" +
"PCfte0+4/CAdOEG1Iy9DUv/UCpUXQr71Jx4iENVivn0G/QahIdHywyYc+aNBLuFP\n" +
"mfGWbfpGosmvw04Ns1bJ3VbNqiAUdtVBagIHENSSvM4fi1g+zfoeyz6zI992oMKb\n" +
"cVkli7J7AgMBAAECggEAPr+sof7RuAs3U7edf4zgQKT+yxL2yC+hZnDg1+rfsETt\n" +
"sgG93sydqqCySv3VpOv/CsYFTFY9gRUeLtitjpdszezmZKLpLagZH51WhkfQWH8Z\n" +
"OuahiINaFOA2jkGLB8nEsxY8JbJxMKTJYHdysioy3sBxydQPaBacL/KoIbNuMYvG\n" +
"z3sTtp+x/sYtLSFQliHQyYKp6wn5OkunRiK9LGRhHo2YmZP17BGnPGqiTtfHm2Gd\n" +
"Suz4D8OBA0+bVnLSAvm98nxD4IP3p4RggYoPVUR/lejiMIyAhCX/VdtRTQ7LRyai\n" +
"yYvjdMyK/HL5JT3UvSqPRUqGmlcGTItVr078dcfoNQKBgQD6aMxZksaEALT3ffgA\n" +
"wV6JdeVC73/Zh4PxF6op1v5MQjKSobUVYYSU2VY5Fjhbaa5FOzJ4niK1wPQ0IusG\n" +
"n+pXRB6FnjeGVFKTuXbCCFMtz1Jpf8GdJnE5k8SbiJpz1KMF04FzbkyEv3ZujvDF\n" +
"ZfNwtGwlSZdeYA2RZQ0xaPZ+XQKBgQDxuTbxgmoY3y6yMbIXG8ln4erNjSfeS1ST\n" +
"j+Y/MCDG3e9P3cDeJ1YbkJM85gR4JJ+33o7UAHlTBqwfkuozUXI5TfGM5mliae9I\n" +
"afrBLC6GOJbmeipYCuhOcs9oFHNLGtUdIrL8upONDI3vhVCPOevGDXX7Qy6SrwhD\n" +
"gY0dPPL2twKBgQCFJXoa2rdhcvrGah2XUTpSPvTTXYOOh8b5R8r8CkwXAk/ftcoS\n" +
"f1K+zr1RB2aZ/JxkRXC1v+fW2q0+l55+XEN/eUH9bD4719Q8pi3mSATqY8V/QSYS\n" +
"yGEF63Nr+/ucQWEZdPftB6X6I8/2Q1z0qRwDPnwOa43n6YztVVzpe9UtJQKBgBft\n" +
"JGIdbZEOrlmXD7C34GS2+P3lpfeb70A38yt4ARaOKzz1jzyuGhjaY6iYgALAZUrW\n" +
"7b5QWbH9LBZn/WSdADhp3an7uJy0Lao2S0rJq8U/XrhriTtzwPDa7mq3832Qp12d\n" +
"7KeVdDt27amywXmb2xmchXwcd0SvrJq5/RTSzp3bAoGBALVG/3xuCbP3x2O5aMI/\n" +
"sJnoG30dbvmL0frH6qwbVUlFbv8SqDAe9rmFSs7GsslW9eTyndxwjFJVlDMqveUi\n" +
"S0o2iorh826wbdrUliFGKVZ23ALsH4sqRO9jD5i6Zhbt65k5pBQyMn20QCQ4nLM7\n" +
"+pZ5plSck0xdbSBGmaRXTW/x\n" +
"-----END PRIVATE KEY-----"; 
        this.publicKey  = "-----BEGIN PUBLIC KEY-----\n" +
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7HHT2lc8OIFXWU59/zmz\n" +
"8J9PX4evU9LG9SYasn41o1phpRKQ8iJvP+wsH3iYPCLxPabChEXTFRgXbp5WjTa1\n" +
"fYMGmDpugPjKtKcmn9amG7yzZR/wcbPWhnZY1gP6dLBt0PUHyu6X16PjjUrXUYE9\n" +
"l0Op7WheUktKq5d3h+L4/HAOG6rVxJ3nLPIb2eaCr6h/SHPoY1Y+uiNhRjwn7XtP\n" +
"uPwgHThBtSMvQ1L/1AqVF0K+9SceIhDVYr59Bv0GoSHR8sMmHPmjQS7hT5nxlm36\n" +
"RqLJr8NODbNWyd1WzaogFHbVQWoCBxDUkrzOH4tYPs36Hss+syPfdqDCm3FZJYuy\n" +
"ewIDAQAB\n" +
"-----END PUBLIC KEY-----";

        // sanitize and pad if necessary (similar to your code)
        privateKey = privateKey.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\s", "").replaceAll("-----.*?-----", "").trim();
        int pad = 4 - (privateKey.length() % 4);
        if (pad < 4) privateKey += "=".repeat(pad);

        publicKey = publicKey.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\s", "").replaceAll("-----.*?-----", "").trim();
        int pad1 = 4 - (publicKey.length() % 4);
        if (pad1 < 4) publicKey += "=".repeat(pad1);

        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            myprivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            mypublicKey  = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize keys: {0}", ex.getMessage());
            ex.printStackTrace();
        }

        this.tokenValidity = TimeUnit.HOURS.toMillis(10); // 10 hours
        this.tokenValidityForRememberMe = TimeUnit.SECONDS.toMillis(REMEMBERME_VALIDITY_SECONDS); // 24 hours
    }

    public String createToken(String username, Set<String> authorities, Boolean rememberMe) {
        long now = (new Date()).getTime();
        long validity = rememberMe ? tokenValidityForRememberMe : tokenValidity;
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("localhost")
                .claim(AUTHORITIES_KEY, authorities.stream().collect(joining(",")))
                .signWith(myprivateKey, SignatureAlgorithm.RS256)
                .setExpiration(new Date(now + validity))
                .compact();
    }
    

    public JWTCredential getCredential(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(mypublicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Set<String> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .collect(Collectors.toSet());

        return new JWTCredential(claims.getSubject(), authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(mypublicKey).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            LOGGER.log(Level.INFO, "Invalid JWT: {0}", e.getMessage());
            return false;
        }
    }
    // SIMPLE HELPER FOR YOUR REST LOGIN
public String generateToken(String role) {
    return createToken(
            role,                           // username
            Set.of(role),                   // role set
            false                           // remember me = false
    );
}

}
