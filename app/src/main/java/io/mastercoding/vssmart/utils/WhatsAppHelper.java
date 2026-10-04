package io.mastercoding.vssmart.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;
import io.mastercoding.vssmart.data.model.ListingModel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility helper to sanitize Indian phone numbers (+91) and launch direct WhatsApp chat intents
 * with pre-filled product inquiries or fallback phone calls.
 */
public final class WhatsAppHelper {

    public static final String PACKAGE_WHATSAPP = "com.whatsapp";
    public static final String PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b";

    private WhatsAppHelper() {
    }

    /**
     * Sanitizes raw user phone input into a standard 12-digit Indian WhatsApp format: 91XXXXXXXXXX.
     * Handles inputs like "+91 98765 43210", "09876543210", "9876543210", "+91-9876543210".
     *
     * @param rawPhone User-entered phone number
     * @return Sanitized 12-digit number starting with 91, or empty string if invalid
     */
    public static String sanitizeIndianPhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }

        // Remove all non-numeric characters except leading plus if any
        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");

        if (digitsOnly.length() == 10) {
            // Standard 10-digit mobile number -> prefix with 91
            return "91" + digitsOnly;
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
            // 11 digits starting with leading 0 (e.g. 09876543210)
            return "91" + digitsOnly.substring(1);
        } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            // Already 12-digit with 91 prefix
            return digitsOnly;
        } else if (digitsOnly.length() > 10) {
            // Take the last 10 digits and prepend 91
            return "91" + digitsOnly.substring(digitsOnly.length() - 10);
        }

        return digitsOnly;
    }

    /**
     * Converts any Indian phone number representation into strict E.164 format: +91XXXXXXXXXX
     * e.g. "7000068732", "+917000068732", "07000068732", "+91 70000 68732" -> "+917000068732"
     *
     * @param rawPhone User-entered phone number
     * @return Formatted E.164 phone string starting with "+91"
     */
    public static String formatToE164IndianPhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }

        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
            return "+91" + digitsOnly.substring(1);
        } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return "+" + digitsOnly;
        } else if (digitsOnly.length() > 10) {
            return "+91" + digitsOnly.substring(digitsOnly.length() - 10);
        }
        return "+91" + digitsOnly;
    }

    /**
     * Validates whether a raw phone number contains a valid 10-digit Indian mobile number.
     */
    public static boolean isValidPhoneNumber(String rawPhone) {
        if (rawPhone == null) return false;
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) return true;
        if (digits.length() == 11 && digits.startsWith("0")) return true;
        if (digits.length() == 12 && digits.startsWith("91")) return true;
        return digits.length() >= 10;
    }

    /**
     * Checks if standard WhatsApp or WhatsApp Business is installed on the user device.
     */
    public static boolean isWhatsAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        return isPackageInstalled(pm, PACKAGE_WHATSAPP) || isPackageInstalled(pm, PACKAGE_WHATSAPP_BUSINESS);
    }

    private static boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Opens WhatsApp to start a direct chat with the seller, pre-filling a personalized inquiry.
     *
     * @param context Activity / Fragment context
     * @param listing The target product listing
     */
    public static void openWhatsAppChat(Context context, ListingModel listing) {
        if (context == null || listing == null) return;

        String sanitizedNumber = sanitizeIndianPhoneNumber(listing.getWhatsappPhone());
        if (sanitizedNumber.isEmpty()) {
            Toast.makeText(context, "Invalid seller contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formulate pre-filled message
        String prefilledMessage = String.format(
                "Hi %s, I saw your listing \"%s\" on VssMart for %s. Is it still available? I would like to buy it.",
                listing.getSellerName(),
                listing.getTitle(),
                listing.getFormattedPrice()
        );

        try {
            String encodedMessage = URLEncoder.encode(prefilledMessage, StandardCharsets.UTF_8.name());
            String uriString = "https://api.whatsapp.com/send?phone=" + sanitizedNumber + "&text=" + encodedMessage;
            
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            if (isPackageInstalled(context.getPackageManager(), PACKAGE_WHATSAPP)) {
                intent.setPackage(PACKAGE_WHATSAPP);
            } else if (isPackageInstalled(context.getPackageManager(), PACKAGE_WHATSAPP_BUSINESS)) {
                intent.setPackage(PACKAGE_WHATSAPP_BUSINESS);
            }
            
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback: Open in browser or show error
            try {
                String webUri = "https://wa.me/" + sanitizedNumber;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                context.startActivity(webIntent);
            } catch (Exception fallbackEx) {
                Toast.makeText(context, "Unable to launch WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Launches the native phone dialer with seller's phone number pre-filled.
     */
    public static void openPhoneDialer(Context context, String rawPhone) {
        if (context == null || rawPhone == null || rawPhone.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sanitizedNumber = sanitizeIndianPhoneNumber(rawPhone);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + sanitizedNumber));
            context.startActivity(dialIntent);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show();
        }
    }
}
