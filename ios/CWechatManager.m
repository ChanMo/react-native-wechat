//
//  CWechatManager.m
//
//  Created by chen on 2018/8/4.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTLog.h>
#import "CWechatManager.h"
#import <React/RCTConvert.h>

@implementation CWechatManager

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(pay:(NSDictionary *)orderInfo)
{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

  NSLog(@"wechat pay start");
  //PayReq* req = [PayReq new];
  PayReq *req = [[PayReq alloc] init];
    req.partnerId = [RCTConvert NSString:orderInfo[@"partnerid"]];
  req.prepayId = orderInfo[@"prepayid"];
  req.package = orderInfo[@"package"];
  req.nonceStr = orderInfo[@"noncestr"];
  req.timeStamp = [orderInfo[@"timestamp"] unsignedIntValue];
  req.sign = orderInfo[@"sign"];
  [WXApi sendReq:req];
  });
}

+ (id)allocWithZone:(struct _NSZone *)zone
{
  static CWechatManager *wechat = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    wechat = [super allocWithZone:zone];
  });
  return wechat;
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"wechatPayResult"];
}

- (void) onResp:(BaseResp*)resp {
  NSLog(@"wechat onresp:%d", resp.errCode);
  if([resp isKindOfClass:[PayResp class]]) {
    PayResp *r = (PayResp *)resp;
    NSMutableDictionary *body = @{@"code":@(r.errCode)}.mutableCopy;
    [self sendEventWithName:@"wechatPayResult" body:body];
  }
}



@end
