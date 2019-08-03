//
//  CWechatManager.h
//
//  Created by chen on 2018/8/4.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#ifndef CWechatManager_h
#define CWechatManager_h

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "WXApi.h"

@interface CWechatManager : RCTEventEmitter <RCTBridgeModule>
- (void) onResp:(BaseResp *)resp;
@end

#endif /* CWechatManager_h */
