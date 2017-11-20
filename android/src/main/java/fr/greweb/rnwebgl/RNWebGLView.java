package fr.greweb.rnwebgl;

import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static fr.greweb.rnwebgl.RNWebGL.*;

public class RNWebGLView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private boolean onSurfaceCreateCalled = false;
    private int ctxId = -1;
    private ThemedReactContext reactContext;

    public RNWebGLView(ThemedReactContext context) {
        super(context);
        reactContext = context;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    private static SparseArray<RNWebGLView> mGLViewMap = new SparseArray<>();

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW),
                EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);

        final RNWebGLView glView = this;
        if (!onSurfaceCreateCalled) {
            // On JS thread, get JavaScriptCore context, create RNWebGL context, call JS callback
            final ReactContext reactContext = (ReactContext) getContext();
            reactContext.runOnJSQueueThread(new Runnable() {
                @Override
                public void run() {
                    ctxId = RNWebGLContextCreate(reactContext.getJavaScriptContext());
                    mGLViewMap.put(ctxId, glView);
                    WritableMap arg = Arguments.createMap();
                    arg.putInt("ctxId", ctxId);
                    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "surfaceCreate", arg);

                }
            });
            onSurfaceCreateCalled = true;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    private void flush() {
        // ctxId may be unset if we get here (on the GL thread) before RNWebGLContextCreate(...) is
        // called on the JS thread to create the RNWebGL context and save its id (see above in
        // the implementation of `onSurfaceCreated(...)`)
        if (ctxId > 0) {
            RNWebGLContextFlush(ctxId);
        }
    }

    void endFrame() {
        requestRender();
    }

    public void onDrawFrame(GL10 unused) {
        flush();
    }

    public void onDetachedFromWindow() {
        mGLViewMap.remove(ctxId);
        reactContext.getNativeModule(RNWebGLTextureLoader.class).unloadWithCtxId(ctxId);
        RNWebGLContextDestroy(ctxId);
        super.onDetachedFromWindow();
    }

    public synchronized static void runOnGLThread(int ctxId, Runnable r) {
        RNWebGLView glView = mGLViewMap.get(ctxId);
        if (glView != null) {
            glView.queueEvent(r);
        }
    }


    public static void endFrame(int ctxId) {
        RNWebGLView glView = mGLViewMap.get(ctxId);
        if (glView != null) {
            glView.endFrame();
        }
    }

    public static void requestFlush(final int ctxId){
        final RNWebGLView glView = mGLViewMap.get(ctxId);
        if (glView != null) {
            glView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    glView.flush();
                }
            });
        }
    }

}
