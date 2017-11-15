package fr.greweb.rnwebgl;

import android.opengl.GLSurfaceView;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

import javax.annotation.Nullable;

public class RNWebGLViewManager extends SimpleViewManager<RNWebGLView> {
  public static final String REACT_CLASS = "RNWebGLView";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public RNWebGLView createViewInstance(ThemedReactContext context) {
    RNWebGLView view = new RNWebGLView(context);
    view.setRenderer(view);
    view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    return view;
  }

  @Override
  public @Nullable Map getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of(
            "surfaceCreate",
            MapBuilder.of("registrationName", "onSurfaceCreate"),
            "frameDrawn",
            MapBuilder.of("registrationName", "onDrawnFrame"));
  }
}
