package fr.greweb.rnwebgl;

import android.util.Log;

import com.facebook.soloader.SoLoader;

// Java bindings for URNWebGL.h interface
public class RNWebGL {
  static {
    SoLoader.loadLibrary("rnwebgl");
    RNWebGL.RNWebGLInit();
  }

  public static native void RNWebGLInit();

  public static native int RNWebGLContextCreate(long jsCtxPtr);
  public static native void RNWebGLContextDestroy(int ctxId);
  public static native void RNWebGLContextFlush(int ctxId);

  public static native int RNWebGLContextCreateObject(int ctxId);
  public static native void RNWebGLContextDestroyObject(int ctxId, int objId);
  public static native void RNWebGLContextMapObject(int ctxId, int objId, int glObj);

  public static void requestFlush(int ctxId){
    Log.d("RNWebGL", "requesting a flush for ctxId : " + ctxId);
    RNWebGLView.requestFlush(ctxId);
  }
}
