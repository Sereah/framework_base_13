package com.lunacattus.voice.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import android.voice.IVoiceManager;
import android.voice.VoiceConstants;

import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.lunacattus.voice.IVoiceCoreService;
import com.lunacattus.voice.IVoiceCallback;

/**
 * 语音助手系统服务（运行在system_server进程）
 * 核心职责：权限校验、进程管理、接口转发，不处理具体语音业务
 *
 * @hide
 */
public class VoiceService extends SystemService {
    private static final String TAG = "VoiceService";

    private static final String CORE_APP_PACKAGE = "com.lunacattus.voice";
    private static final String CORE_APP_ACTION = "com.lunacattus.voice.action.BIND_CORE";

    private final Context mContext;
    private final Handler mHandler;
    private final VoicePermissions mVoicePermissions;
    private IVoiceCoreService mCoreService;
    private final Runnable mRebindRunnable = this::bindCoreService;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.d(TAG, "Core Service connected: " + name);
            mCoreService = IVoiceCoreService.Stub.asInterface(service);
            try {
                mCoreService.asBinder().linkToDeath(mDeathRecipient, 0);
                mCoreService.initEngine();
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to link to death or init engine", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Slog.w(TAG, "Voice CoreService Disconnected unexpectedly!");
            mCoreService = null;
            scheduleRebind();
        }
    };

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Slog.e(TAG, "Voice CoreService binder died! App likely crashed.");
            if (mCoreService != null) {
                mCoreService.asBinder().unlinkToDeath(this, 0);
                mCoreService = null;
            }
            scheduleRebind();
        }
    };

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
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_THIRD_PARTY_APPS_CAN_START) {
            Slog.i(TAG, "Phase PHASE_THIRD_PARTY_APPS_CAN_START reached, binding CoreService...");
            bindCoreService();
        }
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
        if (mCoreService != null) return;
        Intent intent = new Intent(CORE_APP_ACTION);
        intent.setPackage(CORE_APP_PACKAGE);
        boolean success = mContext.bindServiceAsUser(
                intent,
                mConnection,
                Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT,
                UserHandle.SYSTEM
        );
        Slog.d(TAG, "bind core service success: " + success);
        if (!success) {
            scheduleRebind();
        }
    }

    private void scheduleRebind() {
        mHandler.removeCallbacks(mRebindRunnable);
        mHandler.postDelayed(mRebindRunnable, 3000);
    }
}
