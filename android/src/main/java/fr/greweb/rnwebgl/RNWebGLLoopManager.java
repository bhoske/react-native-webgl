package fr.greweb.rnwebgl;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by ulyssemizrahi on 09/11/2017.
 */

public class RNWebGLLoopManager extends ReactContextBaseJavaModule {
    @Override
    public String getName() {
        return "RNWebGLLoopManager";
    }

    public RNWebGLLoopManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void endFrame(final int ctxId) {
        RNWebGLView.endFrame(ctxId);
    }

}
