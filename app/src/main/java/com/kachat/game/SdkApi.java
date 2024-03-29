package com.kachat.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.RtcVideoProcess.FaceRigItf;
import com.RtcVideoProcess.VideoProcessItf;
import com.blankj.utilcode.util.ToastUtils;
import com.dnion.RenderProxy;
import com.dnion.VAGameAPI;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.kachat.game.events.PublicEventMessage;
import com.kachat.game.libdata.controls.DaoDelete;
import com.kachat.game.libdata.controls.DaoInsert;
import com.kachat.game.libdata.controls.DaoQuery;
import com.kachat.game.libdata.dbmodel.DbLive2DBean;
import com.kachat.game.ui.MainActivity;
import com.kachat.game.utils.widgets.AlterDialogBuilder;
import com.kachat.game.utils.widgets.DialogTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SdkApi {

    private static final String TAG = "MurphyBarActivity";
    
    private Context mContext;
    private WebSettings mWebSettings = null;
    private BridgeWebView mBridgeWebView = null;
    private RenderProxy localProxy = null, remoteProxy = null;
    private SurfaceView localView = null, remoteView = null;
    private VideoProcessItf videoProcessorToCamera = null;
    private FaceRigItf faceRigItf = null;
    private DbLive2DBean mDbLive2DBean =null;
    private List<Float> chatList = null, gameList = null;

    public interface OnFaceDetectListener { void onEvent(boolean isHave);}
    private OnFaceDetectListener mFaceDetectListener = null;
    public void setOnFaceDetectListener(OnFaceDetectListener listener) { mFaceDetectListener = listener; }

    private SdkApi() { }

    @SuppressLint("StaticFieldLeak")
    public static SdkApi getInstance() {
        return Html5GameHolder.instance;
    }

    private static class Html5GameHolder {
        @SuppressLint("StaticFieldLeak")
        private static final SdkApi instance = new SdkApi();
    }

    public void loadGame(BridgeWebView bridgeWebView) {
        this.mBridgeWebView = bridgeWebView;

        this.mWebSettings = this.mBridgeWebView.getSettings();
        this.mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        this.mWebSettings.setPluginState(WebSettings.PluginState.ON);
        this.mWebSettings.setUseWideViewPort(true);
        this.mWebSettings.setLoadWithOverviewMode(true);
        this.mWebSettings.setSupportZoom(true);
        this.mWebSettings.setBuiltInZoomControls(true);
        this.mWebSettings.setDisplayZoomControls(false);
        this.mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        this.mWebSettings.setSupportMultipleWindows(true);
        this.mWebSettings.setNeedInitialFocus(true);
        this.mWebSettings.setLoadsImagesAutomatically(true);
        this.mWebSettings.setDefaultTextEncodingName("utf-8");
        this.mWebSettings.setDomStorageEnabled(true);
        this.mWebSettings.setAppCacheEnabled(true);
        this.mWebSettings.setAppCacheMaxSize(1024 * 1024 * 8);
//        String appCachePath = this.mContext.getCacheDir().getAbsolutePath();
//        this.mWebSettings.setAppCachePath(appCachePath);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mBridgeWebView.evaluateJavascript(js,null);
//        }else {
//            mBridgeWebView.loadUrl("http://demo.e3webrtc.com:9004");
//            mBridgeWebView.reload();
//        }

        this.mBridgeWebView.setOnLongClickListener(v -> true);
    }

    public BridgeWebView getBridgeWebView() {
        if (this.mBridgeWebView == null) {
            ToastUtils.showShort("BridgeWebView初始化异常");
            return null;
        }
        return this.mBridgeWebView;
    }

    public void sdkLogin(int uid, String token) { VAGameAPI.getInstance().login(uid + "", token); }

    public void sdkExit() { VAGameAPI.getInstance().logout(); }

    public void loadGame(String htmlUrl) { this.mBridgeWebView.loadUrl(htmlUrl); }

    /**
     * 0：GAME_TOWER,      //盖房子 9002
     * 1：GAME_POPSTAR,    //消灭星星  9003
     * 2：GAME_HEXSTAR,    //六边形  9004
     * 3：GAME_CHAT,       //聊天
     */
    public void startGameMatch(int gameType) { VAGameAPI.getInstance().startGameMatch(gameType); }

    public void create(Context context) {
        this.mContext=context.getApplicationContext();
        VAGameAPI.getInstance().startPreview();
        mDbLive2DBean =new DbLive2DBean();
    }

    public void loadLocalView(Context context, ViewGroup localContainer) {
        this.localProxy = VAGameAPI.getInstance().createRenderProxy(context.getApplicationContext());
        this.localProxy.setAspectMode(RenderProxy.AspectMode.aspectFill);
        this.localProxy.setEnableMirror(true);
        this.localView = this.localProxy.getDisplay();
        localContainer.addView(this.localView);
    }

    public SurfaceView getLocalView() {
        if (this.localView == null) {
            ToastUtils.showShort("提示：本地视图获取失败！");
            return null;
        }
        return this.localView;
    }

    public void loadRemoteView(Context context, ViewGroup remoteContainer) {
        this.remoteProxy = VAGameAPI.getInstance().createRenderProxy(context.getApplicationContext());
        this.remoteProxy.setAspectMode(RenderProxy.AspectMode.aspectFill);
        this.remoteProxy.setEnableMirror(!remoteProxy.enableMirror());
        this.remoteView = this.remoteProxy.getDisplay();
        remoteContainer.addView(this.remoteView);
    }


    public void enableVideoView() { VAGameAPI.getInstance().enableVideoChatWithLocalAndRemoteView(localProxy, remoteProxy); }

    public void loadFaceRigItf(String filePath, String fileName, String bgPath, String bgName, int matchType) {
        if (TextUtils.isEmpty(fileName)) {
            ToastUtils.showShort("警告：Live2D人物为空！");
            return;
        }

        if (TextUtils.isEmpty(bgName)) {
            ToastUtils.showShort("警告：Live2D背景为空！");
            return;
        }

        this.videoProcessorToCamera = VAGameAPI.getInstance().getVideoProcessorToCamera(); // 摄像头
        if (this.videoProcessorToCamera == null) {
            ToastUtils.showShort("提示：Camera异常");
            return;
        }

        boolean isEnabled = this.videoProcessorToCamera.native_faceRigEnabled();
        this.videoProcessorToCamera.native_start();
        this.videoProcessorToCamera.native_setEnableFaceRigSource(true);

        if (!isEnabled) {
            this.faceRigItf = this.videoProcessorToCamera.native_faceRigItf();
//            String path = getApplicationInfo().sourceDir;
//            this.faceRigItf.native_setLive2DModel(filePath, fileName);
            setLive2DModel(filePath, fileName,matchType);
            this.faceRigItf.native_showFaceTrack(false);
            this.faceRigItf.native_setModelOuputSize(368, 640);
            this.faceRigItf.native_setDetectFPS(1);
//            float[] floats=faceRigItf.native_modelOffset();
            this.faceRigItf.native_setOnFaceDetectListener(have -> {
                Log.i("", have ? "yes" : "no");
                // TODO: 2018/5/30 检测人脸 5s后为检测到人脸 弹提示，需转主线程
                if (this.mFaceDetectListener != null) {
                    this.mFaceDetectListener.onEvent(have);
                }
            });
//            this.faceRigItf.native_setModelZoomFraction(1.0f); // 缩放
//            this.faceRigItf.native_setModelBackgroundImage(bgPath, bgName);
            setModelBackgroundImage(bgPath, bgName);
//            save();
        }
    }

    public void setAudioPitch(int pitch){
        VAGameAPI.getInstance().setAudioPitch(pitch);
        mDbLive2DBean.setPitchLevel(pitch);
    }

    public boolean getAudioEnable(){ return VAGameAPI.getInstance().isAudioEnable(); }

    public void setModelBackgroundImage(String bgPath, String bgName) {
        if (this.faceRigItf == null || TextUtils.isEmpty(bgName)) {
            ToastUtils.showShort("faceRigItf or fileName is null");
            return;
        }
        mDbLive2DBean.setBgFilePath(bgPath);
        mDbLive2DBean.setBgFileName(bgName);
        this.faceRigItf.native_setModelBackgroundImage(bgPath, bgName );
    }

    public void setLive2DModel(String filePath, String fileName,int matchType) {
        if (this.faceRigItf == null || TextUtils.isEmpty(fileName)) {
            ToastUtils.showShort("提示：Camera异常或人物遮罩不存在！");
            return;
        }
        mDbLive2DBean.setLiveFilePath(filePath);
        mDbLive2DBean.setLiveFileName(fileName);

        this.faceRigItf.native_setLive2DModel(filePath, fileName);
        setFaceRigItf(fileName,matchType);
    }

    private void setFaceRigItf(String fileName,int matchType) {
        chatList=new ArrayList<>();
        gameList=new ArrayList<>();
        if (this.faceRigItf == null) {
            ToastUtils.showShort("警告：初始化Live2D异常！");
            return;
        }
        switch (fileName) {
            case "aLaiKeSi": {
                chatList.add(1.5f);chatList.add(0f);chatList.add(-0.0937f);
                gameList.add(2.5f);gameList.add(0f);gameList.add(-0.2833f);
            } break;
            case "haru": {
                chatList.add(2.3f);chatList.add(0f);chatList.add(-0.625f);
                gameList.add(2.5f);gameList.add(0f);gameList.add(-0.7f);
            } break;
            case "kaPa": {
                chatList.add(1.5f);chatList.add(-0.0416f);chatList.add(0f);
                gameList.add(1.5f);gameList.add(0f);gameList.add(-0.041f);
            } break;
            case "lanTiYa": {
                chatList.add(1.9f);chatList.add(0f);chatList.add(-0.1f);
                gameList.add(2.5f);gameList.add(0f);gameList.add(-0.1933f);
            } break;
            case "murahana": {
                chatList.add(1.7f);chatList.add(0f);chatList.add(-0.1f);
                gameList.add(2.0f);gameList.add(0f);gameList.add(-0.05f);
            } break;
            case "neiLin": {
                chatList.add(1.8f);chatList.add(0f);chatList.add(-0.0937f);
                gameList.add(2.5f);gameList.add(0f);gameList.add(-0.2533f);
            } break;
            case "natori": {
                chatList.add(2.5f);chatList.add(0f);chatList.add(-0.5937f);
                gameList.add(3.2f);gameList.add(0f);gameList.add(-0.85f);
            } break;
            case "tiYaNa": {
                chatList.add(1.9f);chatList.add(0f);chatList.add(-0.125f);
                gameList.add(2.5f);gameList.add(0f);gameList.add(-0.1933f);
            } break;
            case "weiKeTa": {
                chatList.add(2.0f);chatList.add(0f);chatList.add(-0.0468f);
                gameList.add(3.2f);gameList.add(0f);gameList.add(-0.2433f);
            } break;
            case "xingChen": {
                chatList.add(1.0f);chatList.add(0f);chatList.add(-0.1f);
                gameList.add(1.7f);gameList.add(0f);gameList.add(-0.1333f);
            } break;
            case "yuLu": {
                chatList.add(1.0f);chatList.add(0f);chatList.add(-0.05f);
                gameList.add(1.0f);gameList.add(-0f);gameList.add(0.0333f);
                //  1.5 0f -0.1333f
            } break;
            case "yangYan":{
                chatList.add(1.0f);chatList.add(0f);chatList.add(-0.1f);
                gameList.add(1.7f);gameList.add(0f);gameList.add(-0.1333f);
            } break;
        }

        mDbLive2DBean.setChatMask(chatList);
        mDbLive2DBean.setGameMask(gameList);
        if (matchType == 3) {
            Log.i("FaceRigItf", "setFaceRigItf聊天："+fileName+"\t\t"+chatList.get(0)+"\t\t"+chatList.get(1)+"\t\t"+chatList.get(2)+"\t\t");
            this.faceRigItf.native_setModelZoomFraction(chatList.get(0));
            this.faceRigItf.native_setModelOffset(chatList.get(1), chatList.get(2));
            chatList.clear();
            gameList.clear();
            chatList=null;
            gameList=null;
            return;
        }
        Log.i("FaceRigItf", "setFaceRigItf: 游戏："+fileName+"\t\t"+gameList.get(0)+"\t\t"+gameList.get(1)+"\t\t"+gameList.get(2)+"\t\t");
        this.faceRigItf.native_setModelZoomFraction(gameList.get(0));
        this.faceRigItf.native_setModelOffset(gameList.get(1),gameList.get(2));
        chatList.clear();
        gameList.clear();
        chatList=null;
        gameList=null;
    }

    public void setAudioLoopBack(boolean enable){ VAGameAPI.getInstance().setAudioLoopback(enable); }

    public void sendMessage(String msg) { VAGameAPI.getInstance().sendGameMessage(msg); }

    public void sendGift(int giftId) { VAGameAPI.getInstance().sendGift(giftId); }

    public boolean save(){
        if (mDbLive2DBean == null) {
            return false;
        }
        String fileName=mDbLive2DBean.getLiveFileName();
        if (TextUtils.isEmpty(fileName)) {
            ToastUtils.showShort("保存失败!");
            return false;
        }

        int size=DaoQuery.queryListModelListSize();
        if (size != 0) {
            DaoDelete.deleteLiveModelAll();
        }
        DaoInsert.insertLive2DModel(mDbLive2DBean);
        return true;
    }

    public void destroy(boolean isJoinRoom) {
        Log.i(TAG, "sdk -->>>  destroy: ");
        if (this.mBridgeWebView != null) {
            if (this.mWebSettings != null) {
                this.mWebSettings = null;
            }
            this.mBridgeWebView.clearFormData(); // 仅清除自动完成填充的表单数据
            this.mBridgeWebView.clearCache(true); //  清除网页访问留下的缓存,针对整个应用程序
            this.mBridgeWebView.clearHistory();              //  清除当前webview访问的历史记录
            this.mBridgeWebView.destroy();
            this.mBridgeWebView = null;
        }

        if (isJoinRoom) {
            Log.i(TAG, "destroy: leave Room");
            VAGameAPI.getInstance().stopGameMatch();
            // TODO: 2018/6/25  离开发送消息
            VAGameAPI.getInstance().leaveGameRoom();
        }

        this.videoProcessorToCamera = VAGameAPI.getInstance().getVideoProcessorToCamera(); // 摄像头
        if (this.videoProcessorToCamera != null) {
            Log.i(TAG, "destroy: videoProcessorToCamera");
            this.videoProcessorToCamera.native_stop();
            this.videoProcessorToCamera = null;
        }

        VAGameAPI.getInstance().stopPreview();

//        if (this.localProxy != null) {
//            this.localProxy = null;
//        }
//        if (this.remoteProxy != null) {
//            this.remoteProxy = null;
//        }

        if (mDbLive2DBean != null) {
            mDbLive2DBean=null;
        }

        if (this.mContext != null) {
            this.mContext=null;
        }
    }

}
