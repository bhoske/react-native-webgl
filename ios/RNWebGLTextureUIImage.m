#import <React/RCTConvert.h>
#import "RNWebGLTextureUIImage.h"


@implementation RNWebGLTextureUIImage

//- (instancetype)initWithConfig:(NSDictionary *)config withImage:(UIImage *)image {
//  GPUImagePicture *picture = [[GPUImagePicture alloc] initWithImage:image];
//  [picture processImage];
//  if ((self = [super initWithConfig:config withGPUImageOutput:picture withWidth:image.size.width withHeight:image.size.height])) {
//  }
//  return self;
//}

- (instancetype)initWithConfig:(NSDictionary *)config withImageURL:(NSURL *)imageURL {
  NSError *error;
  NSDictionary *textureOptions = @{
                                   @"GLKTextureLoaderOriginBottomLeft" : config[@"yflip"],
                                   };
  GLKTextureInfo* glkTexture = [GLKTextureLoader textureWithContentsOfURL:imageURL options:textureOptions error:&error];
  if ((self = [super initWithConfig:config withWidth:glkTexture.width withHeight:glkTexture.height]))
  {
    [self attachTexture:glkTexture.name];
  }
  
  return self;
}

@end
