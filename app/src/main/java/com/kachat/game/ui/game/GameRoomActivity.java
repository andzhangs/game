package com.kachat.game.ui.game;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.dnion.VAGameAPI;
import com.kachat.game.events.PublicEventMessage;
import com.kachat.game.libdata.model.ErrorBean;
import com.kachat.game.libdata.model.MessageBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.presenters.StatPagesPresenter;
import com.kachat.game.utils.OnCheckNetClickListener;
import com.kachat.game.utils.widgets.AlterDialogBuilder;
import com.alibaba.fastjson.JSON;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.kachat.game.R;
import com.kachat.game.base.BaseActivity;
import com.kachat.game.SdkApi;
import com.kachat.game.events.DNGameEventMessage;
import com.kachat.game.libdata.controls.DaoQuery;
import com.kachat.game.libdata.dbmodel.DbLive2DBean;
import com.kachat.game.libdata.dbmodel.DbUserBean;
import com.kachat.game.utils.widgets.DialogTextView;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import butterknife.BindView;


public class GameRoomActivity extends BaseActivity {

    private static final String TAG = "GameRoomActivity";

    public static final String Html_Url = "html_url";
    public static final String GAME_TYPE="game_type";

    @BindView(R.id.fl_LoadingView)
    FrameLayout mLoadLayout;
    @BindView(R.id.toolbar_base)
    Toolbar mToolbarBase;
    @BindView(R.id.sdv_RoomBg)
    SimpleDraweeView roomBG;
    @BindView(R.id.bridgeWebView)
    BridgeWebView mBridgeWebView;
    @BindView(R.id.fl_local)
    FrameLayout flLocalView;
    @BindView(R.id.fl_remote)
    FrameLayout flRemoteView;

    public static void newInstance(Context context, Bundle bundle) {
        Intent intent = new Intent(context, GameRoomActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    private DbUserBean mDbUserBean = DaoQuery.queryUserData();
    private int type;
    private boolean isFinish=false;

    private StatPagesPresenter mStatPagesPresenter;

    @Override
    protected int onSetResourceLayout() {
        return R.layout.activity_game_room;
    }

    @Override
    protected boolean onSetStatusBar() {
        return true;
    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (getImmersionBar() != null) {
            getImmersionBar().titleBar(mToolbarBase).transparentStatusBar().init();
        }
    }

    @Override
    protected void onInitView() {
        getToolBarBack().setOnClickListener(v -> {
            exitRoom();
            finish();
        });
        getToolbarMenu().setBackgroundResource(R.drawable.icon_report);
        getToolbarMenu().setOnClickListener(new OnCheckNetClickListener() {
            @Override
            public void onMultiClick(View v) {
                new AlterDialogBuilder(GameRoomActivity.this,new DialogTextView(GameRoomActivity.this,"举报成功!")).hideRootSure();
            }
        });
        getToolbarMenu2().setBackgroundResource(R.drawable.icon_audio_open);
        getToolbarMenu2().setOnClickListener(new OnCheckNetClickListener() {
            @Override
            public void onMultiClick(View v) {
//                boolean is=VAGameAPI.getInstance().isAudioEnable();
//                VAGameAPI.getInstance().switchAudio(!is);
//               getToolbarMenu().setBackgroundResource( !is ? R.drawable.icon_audio_open : R.drawable.icon_audio_close);
                boolean enable=SdkApi.getInstance().getAudioEnable();
                VAGameAPI.getInstance().switchAudio(!enable);
                getToolbarMenu2().setBackgroundResource(!enable ? R.drawable.icon_audio_open : R.drawable.icon_audio_close);

            }
        });

        mStatPagesPresenter= new StatPagesPresenter(new StatPageCallBack());
        mStatPagesPresenter.attachPresenter(StatPagesPresenter.StatType.GAME);
        mLoadLayout.setVisibility(View.VISIBLE);
        initGameHtml();
    }

    private void initGameHtml() {
        SdkApi.getInstance().loadGame(mBridgeWebView);
        SdkApi.getInstance().getBridgeWebView().setDefaultHandler(new DefaultHandler());
        SdkApi.getInstance().getBridgeWebView().setWebChromeClient(new MyWebChromeClient());
        SdkApi.getInstance().getBridgeWebView().setWebViewClient(new BridgeWebViewClient(mBridgeWebView));

        //js发送给Android消息   submitFromWeb 是js调用的方法名    安卓\返回给js
        SdkApi.getInstance().getBridgeWebView().registerHandler("ToApp", (data, function) -> {
//            Log.i("GAME_STAT", "接受Js消息: "+data.toString());
//            显示接收的消息
            handleGameRequest(data);
//            返回给html的消息
            function.onCallBack("返回给//Toast的alert");
        });

        loadVideo();

    }

    private void loadVideo(){
        Bundle bundle = getIntent().getExtras();
        String url = Objects.requireNonNull(bundle).getString(Html_Url);
        if (TextUtils.isEmpty(url)) {
            Toast("游戏异常!");
            this.finish();
        }
        type=bundle.getInt(GAME_TYPE);
        Log.i(TAG, "initGameHtml: "+url+"\t\t"+type);
        SdkApi.getInstance().create(this);
        SdkApi.getInstance().loadGame(url);
        SdkApi.getInstance().loadLocalView(this, flLocalView);
        SdkApi.getInstance().loadRemoteView(this, flRemoteView);
        SdkApi.getInstance().enableVideoView();
        DbLive2DBean dbLive2DBean= Objects.requireNonNull(DaoQuery.queryModelListData()).get(0);
        Log.i(TAG, "loadVideo: "+dbLive2DBean.getLiveFilePath()+ dbLive2DBean.getLiveFileName()+ dbLive2DBean.getBgFilePath()+ dbLive2DBean.getBgFileName()+type);
        SdkApi.getInstance().loadFaceRigItf(dbLive2DBean.getLiveFilePath(), dbLive2DBean.getLiveFileName(), dbLive2DBean.getBgFilePath(), dbLive2DBean.getBgFileName(),type);//
        SdkApi.getInstance().startGameMatch(type);
    }

    public void handleGameRequest(String msg) {
        Log.i(TAG, "handleGameRequest: "+msg);
        SdkApi.getInstance().sendMessage(msg);
        if (!TextUtils.isEmpty(msg)) {
            try {
                JSONObject object=new JSONObject(msg);
                String type=object.getString("type");
                if (type.equals("leave")) {
                    exitRoom();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleGameResponse(String msg) { Log.w(TAG, "handleGameResponse: "+msg); }

    @SuppressLint("InflateParams")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DNGameEventMessage event) {
        switch (event.getEvent()) {
            case SESSION_KEEP_ALIVE:
                Log.i(TAG, "onEvent: SESSION_KEEP_ALIVE");
                break;
            case JOIN_SUCCESS:
                Log.i(TAG, "onEvent: JOIN_SUCCESS");
                mLoadLayout.setVisibility(View.GONE);
                break;
            case JOIN_FAILED:
                Log.i(TAG, "onEvent: JOIN_FAILED");
                break;
            case MATCH_SUCCESS:
                Log.i(TAG, "onEvent: MATCH_SUCCESS");
                break;
            case GAME_MESSAGE:
                // {"type":"leave","data":{},"game":"towerGame"}
                Log.i(TAG, "onEvent: GAME_MESSAGE"+event.getString());
                mBridgeWebView.callHandler("ToJS", event.getString(), data -> handleGameResponse(data));
                break;
            case GAME_STAT:
                Log.i(TAG, "onEvent: GAME_STAT"+event.getString());
                // {"Data": {"box": 1, "chipsList": [], "game": 900, "props": [{"number": 2, "prop": 3},{"number": 1, "prop": 301}, {"number": 1, "prop": 0},{"number": 1, "prop": 1}], "user": 236}, "Method": "GameReward", "MsgID": 10103}\
                try {
                    JSONObject object=new JSONObject(event.getString());
                    int msgID=object.getInt("MsgID");
                    if (msgID == 10103) {

                        dialogGetGift(event.getString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case VIDEO_CHAT_START:
                Log.i(TAG, "onEvent: VIDEO_CHAT_START");
                break;
            case VIDEO_CHAT_FINISH:
                Log.i(TAG, "onEvent: VIDEO_CHAT_FINISH");
                break;
            case VIDEO_CHAT_TERMINATE:
                Log.i(TAG, "onEvent: VIDEO_CHAT_TERMINATE");
                if (flRemoteView.getChildCount() > 0) {
                    flRemoteView.removeAllViews();
                }
//                AlterDialogBuilder dialogBuilder1=new AlterDialogBuilder(GameRoomActivity.this,
//                        new DialogTextView(GameRoomActivity.this,"对方已下线！！！"),"退出游戏");
//                dialogBuilder1.getRootSure().setOnClickListener(v -> {
//                    dialogBuilder1.dismiss();
//                    finish();
//                });
                break;
            case VIDEO_CHAT_FAIL:
                Log.i(TAG, "onEvent: VIDEO_CHAT_FAIL");
                break;
            case GOT_GIFT:
                Log.i(TAG, "onEvent: GOT_GIFT");
                break;
            case ERROR_MESSAGE:
                Log.i(TAG, "onEvent: ERROR_MESSAGE");
                break;
        }
    }

    private class BridgeWebViewClient extends com.github.lzyzsd.jsbridge.BridgeWebViewClient {

        BridgeWebViewClient(BridgeWebView webView) {
            super(webView);
//            Log.i(TAG, "BridgeWebViewClient: ");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Log.i(TAG, "shouldOverrideUrlLoading: " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            Log.i(TAG, "onPageStarted: " + url + "\t\t");
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            Log.i(TAG, "onPageFinished: " + url);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            Log.i(TAG, "onReceivedError: " + errorCode + "\t\t" + description + "\t\t" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.i(TAG, "onProgressChanged: " + newProgress);
            if (newProgress == 100) {

            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
//            Log.i(TAG, "onReceivedTitle: " + title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
//            Log.i(TAG, "onReceivedIcon: ");
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
//            Log.i(TAG, "onReceivedTouchIconUrl: " + url + "\t\t" + precomposed);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
//            Log.i(TAG, "onShowCustomView: ");
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            super.onShowCustomView(view, requestedOrientation, callback);
//            Log.i(TAG, "onShowCustomView: ");
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
//            Log.i(TAG, "onHideCustomView: ");
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
//            Log.i(TAG, "onCreateWindow: " + isDialog + "\t\t" + isUserGesture + "\t\t" + resultMsg);
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
//            Log.i(TAG, "onRequestFocus: ");
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
//            Log.i(TAG, "onCloseWindow: ");
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Log.w(TAG, "onJsAlert: " + url + "\t\t" + message + "\t\t" + result.toString());
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//            Log.i(TAG, "onJsConfirm: " + url + "\t\t" + message + "\t\t" + result.toString());
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
//            Log.i(TAG, "onJsPrompt: " + url + "\t\t" + message + "\t\t" + defaultValue + "\t\t" + result.toString());
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
//            Log.i(TAG, "onJsBeforeUnload: " + url + "\t\t" + message + "\t\t" + result.toString());
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
            super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
//            Log.i(TAG, "onExceededDatabaseQuota: " + databaseIdentifier + "\t\t" + quota + "\t\t" + estimatedDatabaseSize + "\t\t" + totalQuota + "\t\t" + quotaUpdater.toString());
        }

        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
//            Log.i(TAG, "onReachedMaxAppCacheSize: " + requiredStorage + "\t\t" + quota + "\t\t" + quotaUpdater.toString());
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
//            Log.i(TAG, "onGeolocationPermissionsShowPrompt: " + origin + "\t\t" + callback.toString());
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
//            Log.i(TAG, "onGeolocationPermissionsHidePrompt: ");
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
//            Log.i(TAG, "onPermissionRequest: ");
        }

        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            super.onPermissionRequestCanceled(request);
//            Log.i(TAG, "onPermissionRequestCanceled: ");
        }

        @Override
        public boolean onJsTimeout() {
//            Log.i(TAG, "onJsTimeout: ");
            return super.onJsTimeout();
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            super.onConsoleMessage(message, lineNumber, sourceID);
//            Log.i(TAG, "onConsoleMessage: " + message + "\t\t" + lineNumber + "\t\t" + sourceID);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//            Log.i(TAG, "onConsoleMessage: " + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Nullable
        @Override
        public Bitmap getDefaultVideoPoster() {
//            Log.i(TAG, "getDefaultVideoPoster: ");
            return super.getDefaultVideoPoster();
        }

        @Nullable
        @Override
        public View getVideoLoadingProgressView() {
//            Log.i(TAG, "getVideoLoadingProgressView: ");
            return super.getVideoLoadingProgressView();
        }

        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
//            Log.i(TAG, "getVisitedHistory: ");
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//            Log.i(TAG, "onShowFileChooser: ");
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
    }

    private void dialogGetGift(String msg){
        Log.i("GAME_STAT", "dialogGetGift: "+msg);
        DNGameEventMessage.GameDataBean boxbean=JSON.parseObject(msg,DNGameEventMessage.GameDataBean.class);
        if (boxbean.getData() != null) {
            roomBG.setVisibility(View.VISIBLE);
            View boxRootView= LayoutInflater.from(this).inflate(R.layout.dialog_box,null);
            ImageView sdvViewBG=boxRootView.findViewById(R.id.sdv_Box_bg);
            ImageView sdvView=boxRootView.findViewById(R.id.sdv_Box);
            Glide.with(this).asGif().load(R.drawable.gif_game_box_bg).into(sdvViewBG);
            switch (boxbean.getData().getBox()) {
                case 0: Glide.with(this).asGif().load(R.drawable.gif_box_white).into(sdvView);break;//白色
                case 1: Glide.with(this).asGif().load(R.drawable.gif_box_blue).into(sdvView);break;//蓝色
                case 2: Glide.with(this).asGif().load(R.drawable.gif_box_purple).into(sdvView);break;//紫色
                case 3: Glide.with(this).asGif().load(R.drawable.gif_box_orange).into(sdvView);break;//橙色
                case 4: Glide.with(this).asGif().load(R.drawable.gif_box_yellow).into(sdvView);break;//金色
            }
            if (boxbean.getData().getProps().size() >0) {
                AlterDialogBuilder builderView=new AlterDialogBuilder(this,boxRootView).hideRootSure();
                boxRootView.setOnClickListener(v -> {
                    builderView.dismiss();
                    DialogView(boxbean.getData().getChips(),boxbean.getData().getProps()).show();
                });
                builderView.getRootClose().setOnClickListener(v -> {
                    builderView.dismiss();
                    finish();
                });
            }
        }else {
            finish();
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private AlertDialog.Builder DialogView(List<DNGameEventMessage.GameDataBean.DataBean.ChipsBean> chipsList,List<DNGameEventMessage.GameDataBean.DataBean.PropsBean> propsList){
        if (propsList != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_box_info, null);
            builder.setView(view);
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();

            ImageView imgBG = view.findViewById(R.id.imgBG);
            SimpleDraweeView propOneView = view.findViewById(R.id.sdv_one);
            AppCompatTextView propOneTvView = view.findViewById(R.id.acTv_One);
            SimpleDraweeView propTwoView = view.findViewById(R.id.sdv_two);
            AppCompatTextView propTwoTvView = view.findViewById(R.id.acTv_Two);
            SimpleDraweeView propThreeView = view.findViewById(R.id.sdv_three);
            AppCompatTextView propThreeTvView = view.findViewById(R.id.acTv_Three);
            SimpleDraweeView propFourView = view.findViewById(R.id.sdv_four);
            AppCompatTextView propFourTvView = view.findViewById(R.id.acTv_Four);
            SimpleDraweeView getView = view.findViewById(R.id.sdv_Get);
            Glide.with(this).asGif().load(R.drawable.gif_game_box_bg).into(imgBG);
            getView.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });

            int chipSize=chipsList.size();
            int propSize=propsList.size();

            if (chipSize > 0) {
                int golds=0;
                for (int i = 0; i < chipSize; i++) {
                    golds+=chipsList.get(i).getNumber();
                }
                propOneView.setImageURI(getDrawableView(chipsList.get(0).getChip()));
                propOneTvView.setText("x"+golds);

                if (propSize != 0) {
                    for (int i = 0; i < propSize; i++) {
                        switch (i) {
                            case 0:
                                propTwoView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propTwoTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                            case 1:
                                propThreeView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propThreeTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                            case 2:
                                propFourView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propFourTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                        }
                    }
                }
            }else {
                if (propSize > 0) {
                    for (int i = 0; i < propSize; i++) {
                        switch (i) {
                            case 0:
                                propOneView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propOneTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                            case 1:
                                propTwoView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propTwoTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                            case 2:
                                propThreeView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propThreeTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                            case 3:
                                propFourView.setImageURI(getDrawableView(propsList.get(i).getProp()));
                                propFourTvView.setText("x"+propsList.get(i).getNumber());
                                break;
                        }
                    }
                }
            }
            return builder;
        }else {
            Toast("服务器异常！");
        }
        return null;
    }

    private Uri getDrawableView(int index){
        String str="";
        switch (index) {
            case 0: str= "res://"+getPackageName()+"/"+R.drawable.img_000_strong;break;
            case 1: str= "res://"+getPackageName()+"/"+R.drawable.img_001_xiaofei;break;
            case 2: str= "res://"+getPackageName()+"/"+R.drawable.img_002_shaizi;break;
            case 3: str= "res://"+getPackageName()+"/"+R.drawable.img_003_gold;break;
            case 100: str= "res://"+getPackageName()+"/"+R.drawable.img_100_yanjing;break;
            case 101: str= "res://"+getPackageName()+"/"+R.drawable.img_101_wawaquan;break;
            case 300: str= "res://"+getPackageName()+"/"+R.drawable.img_300_baba; break;
            case 301: str= "res://"+getPackageName()+"/"+R.drawable.img_301_love;break;
            case 302: str= "res://"+getPackageName()+"/"+R.drawable.img_302_caonima;break;
            case 400: str= "res://"+getPackageName()+"/"+R.drawable.img_400_weikeda;break;
            case 401: str= "res://"+getPackageName()+"/"+R.drawable.img_401_tiyana;break;
            case 402: str= "res://"+getPackageName()+"/"+R.drawable.img_402_alaikesi;break;
            case 403: str= "res://"+getPackageName()+"/"+R.drawable.img_403_niezheng;break;
            case 404: str= "res://"+getPackageName()+"/"+R.drawable.img_404_kapa;break;
            case 405: str= "res://"+getPackageName()+"/"+R.drawable.img_405_landiya;break;
            case 406: str= "res://"+getPackageName()+"/"+R.drawable.img_406_mingqu;break;
            case 407:str= "res://"+getPackageName()+"/"+R.drawable.img_407_chun;break;
            case 408:str= "res://"+getPackageName()+"/"+R.drawable.img_408_cunhua;break;
        }
        return Uri.parse(str);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
//        SdkApi.getInstance().getBridgeWebView().resumeTimers();
//        SdkApi.getInstance().getBridgeWebView().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
//        SdkApi.getInstance().getBridgeWebView().pauseTimers();
//        SdkApi.getInstance().getBridgeWebView().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        exitRoom();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        roomBG.setVisibility(View.GONE);

        if (mStatPagesPresenter != null) {
            mStatPagesPresenter.detachPresenter();
            mStatPagesPresenter=null;
        }

        finish();
        super.onDestroy();
    }

    private void exitRoom(){
        SdkApi.getInstance().destroy(true);
        if (flLocalView.getChildCount() > 0) {
            flLocalView.removeAllViews();
        }
        if (flRemoteView.getChildCount() > 0) {
            flRemoteView.removeAllViews();
        }
    }

    private class StatPageCallBack implements OnPresenterListeners.OnViewListener<MessageBean>{

        @Override
        public void onSuccess(MessageBean result) {
            Log.i(TAG, "onSuccess: ");
        }

        @Override
        public void onFailed(int errorCode, ErrorBean error) {
            Log.i(TAG, "onFailed: ");
        }

        @Override
        public void onError(Throwable e) {
            Log.i(TAG, "onError: ");
        }
    }


}
