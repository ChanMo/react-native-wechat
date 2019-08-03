package com.mdian.wechat;

import org.json.JSONObject;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.modelpay.PayReq;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.sql.Timestamp;
import androidx.annotation.Nullable;

public class WechatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
  private static final String APP_ID = "wx94f545ec755785e4";
  private static final String TAG = "MDian";
  private static ArrayList<WechatModule> modules = new ArrayList<>();
  private IWXAPI api;

  public WechatModule(ReactApplicationContext reactContext) {
    super(reactContext);
    api = WXAPIFactory.createWXAPI(reactContext, APP_ID);
    api.registerApp(APP_ID);
  }

  @Override
  public String getName() {
    return "Wechat";
  }

  @Override
  public void initialize() {
    super.initialize();
    modules.add(this);
  }

  @Override
  public void onCatalystInstanceDestroy() {
      super.onCatalystInstanceDestroy();
      if (api != null) {
          api = null;
      }
      modules.remove(this);
  }

  public static void handleIntent(Intent intent) {
    for(WechatModule mod: modules) {
      mod.api.handleIntent(intent, mod);
    }
  }

  @ReactMethod
  public void login() {
    Log.d(TAG, "wechat login");
    // send oauth request
    final SendAuth.Req req = new SendAuth.Req();
    req.scope = "snsapi_userinfo";
    req.state = "none";
    api.sendReq(req);
  }

  @ReactMethod
  public void pay(ReadableMap data) {
    Log.d(TAG, "payInfo: " + data);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    PayReq req = new PayReq();
    req.appId = data.getString("appid");
    req.partnerId = data.getString("partnerid");
    req.prepayId = data.getString("prepayid");
    req.nonceStr = data.getString("noncestr");
    req.timeStamp = data.getString("timestamp");
    req.packageValue = data.getString("package");
    req.sign = data.getString("sign");
    req.extData = "app data";
    api.sendReq(req);
  }

  private void sendEvent(ReactContext reactContext,
                       String eventName,
                       @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @Override
  public void onReq(BaseReq req) {
  }

  @Override
  public void onResp(BaseResp resp) {
    Log.d(TAG, "type=" + resp.getType());
    Log.d(TAG, "onPayFinish, errCode="+resp.errCode);
    if(resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
        SendAuth.Resp authResp = (SendAuth.Resp)resp;
        WritableMap params = Arguments.createMap();
        params.putString("code", authResp.code);
        params.putString("state", authResp.state);
        this.sendEvent(getReactApplicationContext(), "wechatLoginResult", params);
    }
    if(resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
      WritableMap params = Arguments.createMap();
      params.putInt("code", resp.errCode);
      this.sendEvent(getReactApplicationContext(), "wechatPayResult", params);
    }
  }
}
