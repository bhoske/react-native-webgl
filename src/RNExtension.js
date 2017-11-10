//@flow
import { NativeModules } from 'react-native';
import { RNWebGLTexture } from './webglTypes';

const { RNWebGLTextureManager, RNWebGLLoopManager } = NativeModules;

type RNWebGLRenderingContext = WebGLRenderingContext & {
  __endFrame: *,
  __readPixelsToTemporaryFile: *
};

type Config = Object;

export type Extension = {
  // NB more methods can be extended with middlewares
  loadTexture: (
    config: Config
  ) => Promise<{ texture: RNWebGLTexture, width: number, height: number }>,
  unloadTexture: (texture: RNWebGLTexture) => void,
  endFrame: () => void
};

type Middleware = (extIn: Extension) => Extension;

const middlewares: Array<Middleware> = [];

export default {
  addMiddleware: (middleware: Middleware) => {
    middlewares.push(middleware);
  },
  createWithContext: (gl: RNWebGLRenderingContext, ctxId: number): Extension =>
    middlewares.reduce((ext, middleware) => middleware(ext), {
      loadTexture: config =>
        // WARNING can only load 8 textures at a time
        // Loading 9 textures will break the app
        RNWebGLTextureManager.create({
          ...config,
          ctxId
        }).then(({ objId, width, height }) => {
          const texture = new RNWebGLTexture(objId);
          return { texture, width, height };
        }),
      unloadTexture: texture => RNWebGLTextureManager.destroy(texture.id),
      endFrame: () => {
        RNWebGLLoopManager.endFrame(ctxId);
        gl.__endFrame();
      }
    })
};
