package com.android.server.voice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;
import android.util.Slog;
import android.voice.VoiceConstants;

/**
 * @hide
 */
public class VoicePermissions {
    private static final String TAG = "VoicePermissions";
    private final Context mContext;

    public VoicePermissions(Context context) {
        mContext = context;
    }

    /**
     * 校验绑定语音服务的权限
     *
     * @throws SecurityException 权限校验失败时抛出
     */
    public void enforceManageVoicePermission() {
        final long identity = Binder.clearCallingIdentity();
        try {
            if (mContext.checkCallingOrSelfPermission(
                    VoiceConstants.PERMISSION_MANAGE_VOICE)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.e(TAG, "Permission denied: MANAGE_VOICE");
                throw new SecurityException("Requires MANAGE_VOICE permission");
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /**
     * 校验绑定语音服务的权限
     *
     * @throws SecurityException 权限校验失败时抛出
     */
    public void enforceBindVoiceServicePermission() {
        final long identity = Binder.clearCallingIdentity();
        try {
            if (mContext.checkCallingOrSelfPermission(
                    VoiceConstants.PERMISSION_BIND_VOICE_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {
                Slog.e(TAG, "Permission denied: BIND_VOICE_SERVICE");
                throw new SecurityException("Requires BIND_VOICE_SERVICE permission");
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
}
