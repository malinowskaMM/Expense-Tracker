package pl.lodz.p.it.expenseTracker.utils.etag;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagMessageVerifierInitializationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagMessageVerifierSignatureValidationFailedException;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

import java.text.ParseException;

@Service
public class MessageVerifier {

    @Value("${ETag.secretKey}")
    private String secretKey;

    private JWSVerifier jwsVerifier;

    private final Internationalization internationalization;

    public MessageVerifier(Internationalization internationalization) {
        this.internationalization = internationalization;
    }

    @PostConstruct
    public void init() throws ETagMessageVerifierInitializationFailedException {
        try {
            jwsVerifier = new MACVerifier(secretKey);
        } catch (JOSEException e) {
            throw new ETagMessageVerifierInitializationFailedException(internationalization.getMessage("utils.etagVerifierInitializationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }
    }

    public boolean validateSignature(String message) {
        try {
            final JWSObject jwsObject = JWSObject.parse(message);
            return jwsObject.verify(jwsVerifier);
        } catch (ParseException | JOSEException e) {
            throw new ETagMessageVerifierSignatureValidationFailedException(internationalization.getMessage("utils.etagVerifierSignatureValidationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }
    }
}