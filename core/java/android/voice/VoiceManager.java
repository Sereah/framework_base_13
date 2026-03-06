package android.voice;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;

/** @hide */
public class VoiceManager {
    private static final String TAG = "VoiceManager";
    private final Context mContext;
    private final IVoiceManager mService;

    public VoiceManager(Context context) {
        mContext = context;
        mService = IVoiceManager.Stub.asInterface(
                ServiceManager.getService(VoiceConstants.VOICE_SERVICE));
        if (mService == null) {
            Slog.e(TAG, "Fail to get VoiceManager service");
        }
    }

    public void testVoice(String message) {
        try {
            mService.testVoice(message);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
