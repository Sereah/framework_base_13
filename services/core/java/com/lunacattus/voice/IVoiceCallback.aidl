// IVoiceCallback.aidl
package com.lunacattus.voice;

// Declare any non-default types here with import statements

interface IVoiceCallback {
    oneway void onInitCompleted(in boolean success);
    oneway void onWakeUp();
}