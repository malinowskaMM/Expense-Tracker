package pl.lodz.p.it.expenseTracker.utils.etag;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagMessageSignerInitializationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagMessageSignerMessageSigningFailedException;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Service
public class MessageSigner {

    @Value("${ETag.secretKey}")
    private String secretKey;

    private JWSSigner jwsSigner;

    private final Internationalization internationalization;

    public MessageSigner(Internationalization internationalization) {
        this.internationalization = internationalization;
    }

    @PostConstruct
    public void init() {
        try {
            jwsSigner = new MACSigner(secretKey);
        } catch (KeyLengthException e) {
            throw new ETagMessageSignerInitializationFailedException(internationalization.getMessage("utils.etagSignerInitializationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }
    }

    public String sign(Signable signable) {
        try {
            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(signable.messageToSign()));
            jwsObject.sign(jwsSigner);
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new ETagMessageSignerMessageSigningFailedException(internationalization.getMessage("utils.etagSignerMessageSigningFailed", LocaleContextHolder.getLocale().getLanguage()));
        }
    }
}