package com.moovie.app;

import android.content.Context;
import android.util.Log;

import com.moovie.BuildConfig;
import com.moovie.data.WatchmodeRepository;
import com.moovie.model.watchmode.Platform;

import java.util.List;

/**

 Simple app-wide service locator for WatchmodeRepository.

 This avoids creating new instances per Activity and centralizes initialization.
 */
public final class AppStore {

    private static WatchmodeRepository wmRepo;
    private static boolean logosLoadingInProgress = false;
    private static boolean logosReady = false;

    private AppStore() {
        // prevent instantiation
    }

    /**

     Initialize the shared WatchmodeRepository once.

     This method is safe to call multiple times; subsequent calls are no-ops.

     Do NOT perform heavy work on the UI thread here.
     */
    public static synchronized void init(Context ctx) {
        if (wmRepo != null) {
            return;
        }

        wmRepo = new WatchmodeRepository(BuildConfig.WM_API_KEY);
        logosLoadingInProgress = true;

        // Run the network preload in a background thread
        new Thread(() -> {
            try {
                wmRepo.fetchSourceLogos(new WatchmodeRepository.PlatformsCallback() {
                    @Override public void onSuccess(String titleId, List<Platform> platforms) {
                        // post to main thread if UI needs to react
                        logosReady = true;
                        logosLoadingInProgress = false;
                    }
                    @Override public void onError(Exception e) {
                        logosReady = false;
                        logosLoadingInProgress = false;
                    }
                });
            } catch (Exception e) {
                logosReady = false;
                logosLoadingInProgress = false;
                Log.e("AppStore", "Logo preload failed", e);
            }
        }).start();
    }

    /**

     Accessor for the shared WatchmodeRepository.

     Will lazily initialize if not already created.
     */
    public static synchronized WatchmodeRepository getWatchmodeRepo(Context ctx) {
        if (wmRepo == null) {
            init(ctx);
        }
        return wmRepo;
    }

    /**

     Optional: expose readiness so UI can wait for logos to be ready.

     Callers can poll isLogosReady or observe via your preferred observable mechanism.
     */
    public static boolean isLogosReady() {
        return logosReady;
    }

    public static void setLogosReady(boolean ready) {
        logosReady = ready;
    }

    public static boolean isLoadingLogos() {
        return logosLoadingInProgress;
    }

    public static void setLoadingLogos(boolean loading) {
        logosLoadingInProgress = loading;
    }
}
