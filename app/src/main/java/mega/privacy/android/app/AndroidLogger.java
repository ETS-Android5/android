package mega.privacy.android.app;

import mega.privacy.android.app.logging.LegacyLogUtil;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaLoggerInterface;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidLogger extends MegaLogger implements MegaLoggerInterface {

    public static final String LOG_FILE_NAME = "logSDK.txt";
    private LegacyLogUtil legacyLogUtil;

    public AndroidLogger(@Nullable String fileName, @NotNull LegacyLogUtil legacyLogUtil) {
        super(fileName);
        this.legacyLogUtil = legacyLogUtil;
    }

    public void log(String time, int logLevel, String source, String message) {
        //save to log file
        if (isReadyToWriteToFile(legacyLogUtil.getStatusLoggerSdk())) {
            fileLogQueue.add(createMessage(time, logLevel, source, message));
        }
    }

    //create SDK specific log message
    private String createMessage(String time, int logLevel, String source, String message) {

        String logLevelMessage = "";
        switch (logLevel) {
            case MegaApiAndroid.LOG_LEVEL_DEBUG:
                logLevelMessage = "DEB";
                break;
            case MegaApiAndroid.LOG_LEVEL_ERROR:
                logLevelMessage = "ERR";
                break;
            case MegaApiAndroid.LOG_LEVEL_FATAL:
                logLevelMessage = "FAT";
                break;
            case MegaApiAndroid.LOG_LEVEL_INFO:
                logLevelMessage = "INF";
                break;
            case MegaApiAndroid.LOG_LEVEL_MAX:
                logLevelMessage = "MAX";
                break;
            case MegaApiAndroid.LOG_LEVEL_WARNING:
                logLevelMessage = "WRN";
                break;
            default:
                logLevelMessage = "NON";
                break;
        }

        String sourceMessage = "";
        if (source != null) {
            String[] s = source.split("jni/mega");
            if (s.length > 1) {
                sourceMessage = s[1];
            } else {
                sourceMessage = source;
            }
        }

        return (sourceMessage == null || sourceMessage.isEmpty()) ?
                "[" + time + "][" + logLevelMessage + "] " + message + "\n" :
                "[" + time + "][" + logLevelMessage + "] " + message + " (" + sourceMessage + ")\n";
    }
}
