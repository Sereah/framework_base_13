package com.android.server.voice;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Slog;
import android.voice.IVoiceManager;
import android.voice.VoiceConstants;

import com.android.server.LocalServices;
import com.android.server.SystemService;

/**
 * 语音助手系统服务（运行在system_server进程）
 * 核心职责：权限校验、进程管理、接口转发，不处理具体语音业务
 *
 * @hide
 */
public class VoiceService extends SystemService {
    private static final String TAG = "VoiceService";

    private final Context mContext;
    private final Handler mHandler;
    private final VoicePermissions mVoicePermissions;

    public VoiceService(Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mVoicePermissions = new VoicePermissions(context);
    }

    @Override
    public void onStart() {
        Slog.i(TAG, "Starting VoiceService");
        publishBinderService(VoiceConstants.VOICE_SERVICE, mImpl);
        LocalServices.addService(VoiceServiceInternal.class, new LocalServiceImpl());
        bindCoreService();
    }

    private final class LocalServiceImpl extends VoiceServiceInternal {

        @Override
        public void onLongPressPowerKey() {
            VoiceService.this.onLongPressPowerKey();
        }
    }

    private void onLongPressPowerKey() {
        Slog.i(TAG, "onLongPressPowerKey");
    }

    private final IBinder mImpl = new IVoiceManager.Stub() {
        @Override
        public void testVoice(String message) {
            Slog.i(TAG,
                    "testVoice called with: " + message + " from pid: " + Binder.getCallingPid());
        }
    };

    private void bindCoreService() {

    }
}
