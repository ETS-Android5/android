package mega.privacy.android.app.utils.billing;

import android.content.Context;

import mega.privacy.android.app.R;
import mega.privacy.android.app.service.iab.BillingManagerImpl;
import mega.privacy.android.app.utils.TextUtil;

public class PaymentUtils {

    /** SKU for our subscription PRO_I monthly */
    public static final String SKU_PRO_I_MONTH = BillingManagerImpl.SKU_PRO_I_MONTH;

    /** SKU for our subscription PRO_I yearly */
    public static final String SKU_PRO_I_YEAR = BillingManagerImpl.SKU_PRO_I_YEAR;

    /** SKU for our subscription PRO_II monthly */
    public static final String SKU_PRO_II_MONTH = BillingManagerImpl.SKU_PRO_II_MONTH;

    /** SKU for our subscription PRO_II yearly */
    public static final String SKU_PRO_II_YEAR = BillingManagerImpl.SKU_PRO_II_YEAR;

    /** SKU for our subscription PRO_III monthly */
    public static final String SKU_PRO_III_MONTH = BillingManagerImpl.SKU_PRO_III_MONTH;

    /** SKU for our subscription PRO_III yearly */
    public static final String SKU_PRO_III_YEAR = BillingManagerImpl.SKU_PRO_III_YEAR;

    /** SKU for our subscription PRO_LITE monthly */
    public static final String SKU_PRO_LITE_MONTH = BillingManagerImpl.SKU_PRO_LITE_MONTH;

    /** SKU for our subscription PRO_LITE yearly */
    public static final String SKU_PRO_LITE_YEAR = BillingManagerImpl.SKU_PRO_LITE_YEAR;

    /**
     * Get the level of a certain sku.
     *
     * @param sku The id of the sku item.
     * @return The level of the sku.
     */
    public static int getProductLevel(String sku) {
        if (TextUtil.isTextEmpty(sku)) {
            return -1;
        }
        switch (sku) {
            case SKU_PRO_LITE_MONTH:
            case SKU_PRO_LITE_YEAR:
                return 0;
            case SKU_PRO_I_MONTH:
            case SKU_PRO_I_YEAR:
                return 1;
            case SKU_PRO_II_MONTH:
            case SKU_PRO_II_YEAR:
                return 2;
            case SKU_PRO_III_MONTH:
            case SKU_PRO_III_YEAR:
                return 3;
            default:
                return -1;
        }
    }

    /**
     * Get renewal type of a certain sku item.
     *
     * @param context Context
     * @param sku The id of the sku item.
     * @return The renewal type of the sku item, Monthly or Yearly.
     */
    public static String getSubscriptionRenewalType(Context context, String sku) {
        switch (sku){
            case SKU_PRO_LITE_MONTH:
            case SKU_PRO_I_MONTH:
            case SKU_PRO_II_MONTH:
            case SKU_PRO_III_MONTH:
                return context.getString(R.string.subscription_type_monthly);
            case SKU_PRO_LITE_YEAR:
            case SKU_PRO_I_YEAR:
            case SKU_PRO_II_YEAR:
            case SKU_PRO_III_YEAR:
                return context.getString(R.string.subscription_type_yearly);
            default:
                return "";

        }
    }

    /**
     * Get type name of a certain sku item.
     *
     * @param context Context
     * @param sku The id of the sku item.
     * @return The type name of the sku.
     */
    public static String getSubscriptionType(Context context, String sku) {
        switch (sku) {
            case SKU_PRO_LITE_MONTH:
            case SKU_PRO_LITE_YEAR:
                return context.getString(R.string.lite_account);
            case SKU_PRO_I_MONTH:
            case SKU_PRO_I_YEAR:
                return context.getString(R.string.pro1_account);
            case SKU_PRO_II_MONTH:
            case SKU_PRO_II_YEAR:
                return context.getString(R.string.pro2_account);
            case SKU_PRO_III_MONTH:
            case SKU_PRO_III_YEAR:
                return context.getString(R.string.pro3_account);
            default:
                return "";
        }
    }
}
