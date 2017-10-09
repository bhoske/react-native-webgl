#import <UIKit/UIKit.h>
#import <GLKit/GLKit.h>
//#import "RNWebGLTextureWithGPUImage.h"
//#import "GPUImage.h"
#import "RNWebGLTexture.h"

@interface RNWebGLTextureUIImage: RNWebGLTexture
//- (instancetype)initWithConfig:(NSDictionary *)config withImage:(UIImage *)image;
- (instancetype)initWithConfig:(NSDictionary *)config withImageURL:(NSURL *)imageURL;
@end
