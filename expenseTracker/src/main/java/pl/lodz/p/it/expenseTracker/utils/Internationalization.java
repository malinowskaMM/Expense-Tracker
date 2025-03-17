package pl.lodz.p.it.expenseTracker.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class Internationalization {

    public String getMessage(String messageKey, String language) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("translations", Locale.forLanguageTag(language));
        return resourceBundle.getString(messageKey);
    }

}
