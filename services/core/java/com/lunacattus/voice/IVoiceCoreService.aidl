// IVoiceCoreService.aidl
package com.lunacattus.voice;

import com.lunacattus.voice.IVoiceCallback;

interface IVoiceCoreService {
    oneway void initEngine();
    oneway void release();

    oneway void registerCallback(in IVoiceCallback callback);
    oneway void unregisterCallback(in IVoiceCallback callback);

    oneway void wakeup();
}