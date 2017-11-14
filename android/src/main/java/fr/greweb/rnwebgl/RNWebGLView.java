package fr.greweb.rnwebgl;

import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
        setRenderer(this);
    }

    private static SparseArray<RNWebGLView> mGLViewMap = new SparseArray<>();
    private ConcurrentLinkedQueue<Runnable> mEventQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean readyToDraw = new AtomicBoolean(false);
    private AtomicBoolean loopEnded = new AtomicBoolean(false);
    private AtomicBoolean shouldDraw = new AtomicBoolean(true);

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d("webgl", "surface created (renderer)");
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

    public void startFrame() {
        Log.w("webgl", "starting frame in ctxId : " + ctxId);
        reactContext.getJSModule(RCTEventEmitter.class)
                .receiveEvent(getId(), "frameDrawn", Arguments.createMap());
    }

    private void flush() {
        // Flush any queued events
        for (Runnable r : mEventQueue) {
            r.run();
        }
        mEventQueue.clear();

        // ctxId may be unset if we get here (on the GL thread) before RNWebGLContextCreate(...) is
        // called on the JS thread to create the RNWebGL context and save its id (see above in
        // the implementation of `onSurfaceCreated(...)`)
        if (ctxId > 0) {
            RNWebGLContextFlush(ctxId);
        }
    }

    public void endFrame() {
        Log.w("webgl", "ending frame in webgl context !");
        readyToDraw.set(true);
    }

    public void endLoop() {
        Log.w("webgl", "ending loop in webgl context !");
        shouldDraw.set(false);
    }

    public void onPause(){
        Log.d("webgl", "pausing");
        shouldDraw.set(false);
        super.onPause();
    }

    public void onResume(){
        Log.d("webgl", "resuming");
        shouldDraw.set(true);
        super.onResume();
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        Log.d("webgl", "destroyed");
        shouldDraw.set(false);
        super.surfaceDestroyed(holder);
    }

    public void surfaceCreated(SurfaceHolder holder){
        Log.d("webgl", "created");
        shouldDraw.set(true);
        super.surfaceCreated(holder);
    }

    public void finalize() throws Throwable {
        shouldDraw.set(false);
        super.finalize();
    }

    public void onDrawFrame(GL10 unused) {
        if(shouldDraw.get()) {
            //startFrame();
            Log.w("webgl", "thread in draw : " + Thread.currentThread().getName());
            while (!readyToDraw.get()) {
                if (!shouldDraw.get()) {
                    Log.d("webgl", "shouldn't have drawn, returning");
                    //flush();
                    return;
                }
                try {
                    Log.w("webgl", "Sleeping for ctxId : " + ctxId + ", not ready.");
                    Thread.sleep(100);
                    flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (shouldDraw.get()) {
                flush();
                Log.w("webgl", "frame ended");
                readyToDraw.set(false);
            }
        } else {
            Log.d("webgl", "should not draw");
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
    }

    public void onDetachedFromWindow() {
        Log.w("webgl", "detaching");
        mGLViewMap.remove(ctxId);
        shouldDraw.set(false);
        reactContext.getNativeModule(RNWebGLTextureLoader.class).unloadWithCtxId(ctxId);
        RNWebGLContextDestroy(ctxId);
        super.onDetachedFromWindow();
    }

    public synchronized void runOnGLThread(Runnable r) {
        mEventQueue.add(r);
    }

    public synchronized static void runOnGLThread(int ctxId, Runnable r) {
        RNWebGLView glView = mGLViewMap.get(ctxId);
        if (glView != null) {
            glView.runOnGLThread(r);
        }
    }


    public static void endFrame(int ctxId) {
        Log.w("webgl", "ending frame");
        RNWebGLView glView = mGLViewMap.get(ctxId);
        Log.w("webgl", "thread in end frame :" + Thread.currentThread().getName());
        if (glView != null) {
            Log.w("webgl", "context found, ending frame");
            glView.endFrame();
        }
    }

    public static void endLoop(int ctxId) {
        Log.w("webgl", "ending frame");
        RNWebGLView glView = mGLViewMap.get(ctxId);
        Log.w("webgl", "thread in end frame :" + Thread.currentThread().getName());
        if (glView != null) {
            Log.w("webgl", "context found, ending frame");
            glView.endLoop();
        }
    }


}
