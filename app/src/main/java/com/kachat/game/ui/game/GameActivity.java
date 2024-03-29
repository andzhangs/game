package com.kachat.game.ui.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kachat.game.Config;
import com.kachat.game.Constant;
import com.kachat.game.R;
import com.kachat.game.base.BaseActivity;
import com.kachat.game.events.DNGameEventMessage;
import com.kachat.game.events.PublicEventMessage;
import com.kachat.game.libdata.controls.DaoQuery;
import com.kachat.game.libdata.dbmodel.DbUserBean;
import com.kachat.game.libdata.model.ErrorBean;
import com.kachat.game.libdata.model.GamesBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.presenters.GameListPresenter;
import com.kachat.game.ui.shop.ShopActivity;
import com.kachat.game.ui.user.MeActivity;
import com.kachat.game.utils.OnCheckNetClickListener;
import com.kachat.game.utils.widgets.AlterDialogBuilder;
import com.kachat.game.utils.widgets.DialogTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import cn.lemon.view.SpaceItemDecoration;

public class GameActivity extends BaseActivity {

    private static final String TAG = "GameActivity";

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, GameActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.toolbar_base)
    Toolbar mToolbar;
    @BindView(R.id.sdv_UserLogo)
    SimpleDraweeView mSdvUserLogo;
    @BindView(R.id.acTv_UserName)
    AppCompatTextView mAcTvUserName;
    @BindView(R.id.acTv_UserLevel)
    AppCompatTextView mAcTvUserLevel;
    @BindView(R.id.acTv_UserDiamonds)
    AppCompatTextView mAcTvUserDiamonds;
    @BindView(R.id.acTv_UserSport)
    AppCompatTextView mAcTvUserSport;
    @BindView(R.id.acTv_UserGold)
    AppCompatTextView mAcTvUserGold;
    @BindView(R.id.rv_GameList)
    RecyclerView mRvGameList;

    private List<GamesBean.GameBean> mList = null;
    private GameListAdapter mAdapter;
    private LinearLayoutManager manager;

    private GameListPresenter mPresenter;

    @Override
    protected int onSetResourceLayout() {
        return R.layout.activity_game;
    }

    @Override
    protected boolean onSetStatusBar() {
        return true;
    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (getImmersionBar() != null) {
            getImmersionBar().navigationBarColor(R.color.black).titleBar(mToolbar).transparentStatusBar().init();
        }
    }

    @Override
    protected void onInitView() {
        getToolBarBack().setOnClickListener(v -> this.finish());
        getToolbarMenu().setImageResource(R.drawable.icon_ranklist);
        getToolbarMenu().setOnClickListener(v -> RankListActivity.newInstance(this));
        SimpleDraweeView sdvView=findViewById(R.id.sdv_ToolBar_BaseMenu2);
        sdvView.setImageResource(R.drawable.icon_hint);
        sdvView.setOnClickListener(v -> {
            new AlterDialogBuilder(this,"游戏规则",new DialogTextView(this,"GameTower温馨提示：\n也许都是一些你耳熟能详的小游戏，但是要牢记以下3点：\n1-受岛屿奇怪雾状粒子的影响，你挂了没关系，只要你队友不挂，你就有复活的机会。不到最后时刻千万不要放弃哦。\n2-羽菱财团给你们准备了丰厚的奖励！只要突破一定的分数界限，你和你的队友就能得到宝箱！！！宝箱品质也会随你们突破的界限提升哦。\n3-玩游戏的时候就不要互撩了，想要给对方偷偷送玫瑰送巧克力是不可能滴")).hideRootSure();
        });

        mList = new ArrayList<>();
        mAdapter = new GameListAdapter();
        manager = new LinearLayoutManager(this);
        mRvGameList.addItemDecoration(new SpaceItemDecoration(0, 10, 0, 0));
        mRvGameList.setLayoutManager(manager);
        mRvGameList.setAdapter(mAdapter);

        mPresenter = new GameListPresenter(new GameListCallBack());
    }

    public void onShopClick(View v){  ShopActivity.newInstance(this);  }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
        DbUserBean dbUserBean = DaoQuery.queryUserData();
        if (dbUserBean != null) {
            if (dbUserBean.getGender().equals("male")) {
                mSdvUserLogo.setBackgroundResource(R.drawable.icon_male_logo);
            } else {
                mSdvUserLogo.setBackgroundResource(R.drawable.icon_female_logo);
            }
            mAcTvUserName.setText(dbUserBean.getUsername());
            mAcTvUserLevel.setText("LV" + dbUserBean.getLevel() + "");
            mAcTvUserSport.setText(dbUserBean.getHp() + "");
            mAcTvUserDiamonds.setText(dbUserBean.getDiamond() + "");
            mAcTvUserGold.setText(dbUserBean.getGold() + "");
        }

        if (mList != null) {
            mList.clear();
        }
        mPresenter.attachPresenter();
    }

    private class GameListCallBack implements OnPresenterListeners.OnViewListener<GamesBean> {
        @Override
        public void onSuccess(GamesBean result) {
            if (result != null && result.getGames() != null && result.getGames().size() > 0) {
                int size=result.getGames().size();
                Log.i(TAG, "onSuccess: "+result.getGames().toString());
                for (int i = 0; i < size; i++) {
                    switch (result.getGames().get(i).getIndex()) {
                        case 903: //  娃娃机
                            result.getGames().get(i).setImage(R.drawable.icon_game_bg_chat);
                            result.getGames().get(i).setImgStart(R.drawable.icon_game_start);
                            result.getGames().get(i).setImgTimeLimit(R.drawable.icon_game_slice);
                            break;
                        case 902: //  晕头转向
                            result.getGames().get(i).setImage(R.drawable.icon_game_bg_fourytzx);
                            result.getGames().get(i).setImgHint(R.drawable.icon_game_hint);
                            result.getGames().get(i).setImgStart(R.drawable.icon_game_start);
                            break;
                        case 901:  //  消灭星星
                            result.getGames().get(i).setImage(R.drawable.icon_game_bg_star);
                            result.getGames().get(i).setImgHint(R.drawable.icon_game_hint);
                            result.getGames().get(i).setImgStart(R.drawable.icon_game_start);
                            break;
                        case 900:  //  盖房子
                            result.getGames().get(i).setImage(R.drawable.icon_game_bg_house);
                            result.getGames().get(i).setImgHint(R.drawable.icon_game_hint);
                            result.getGames().get(i).setImgStart(R.drawable.icon_game_start);
                            break;
                    }
                }
                Log.i(TAG, "onSuccess: "+result.toString());
                mList.addAll(result.getGames());
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailed(int errorCode, ErrorBean error) {
            if (!TextUtils.isEmpty(error.getToast())) {
                Toast(error.getToast());
            }
        }

        @Override
        public void onError(Throwable e) {
            if (e != null) {
                Log.i(TAG, "onError: "+e.getMessage());
                Toast(e.getMessage());
            }
        }
    }

    private class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.GameViewHolder> {
        @NonNull
        @Override
        public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GameViewHolder(LayoutInflater.from(GameActivity.this).inflate(R.layout.item_game_list, parent, false));
        }

        @SuppressLint("InflateParams")
        @Override
        public void onBindViewHolder(@NonNull GameViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.sdvMark.setOnClickListener(v -> {
                View containerView=LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_custom_title_info,null);
                AppCompatTextView titleView=containerView.findViewById(R.id.acTv_Title);
                AppCompatTextView infoView=containerView.findViewById(R.id.acTv_Details);
                if (mList.get(position).getIndex() == 900) { //  盖房子
                    titleView.setText("盖房子");
                    infoView.setText("叠一个房子25分,但是叠得好有奖励分哦！");
                }else if (mList.get(position).getIndex() == 901) {  //  消灭星星
                    titleView.setText("消灭星星");
                    infoView.setText("同样颜色的星星2个起步，一口气消的越多得分数就越高哦！");
                }else if (mList.get(position).getIndex() == 902) {  //  晕头转向
                    titleView.setText("晕头转向");
                    infoView.setText("因为是升级版,所以同样的颜色3个起步,且至少一定要有2个相邻。");
                }
                new AlterDialogBuilder(GameActivity.this,"游戏说明",containerView).hideRootSure();
            });

            holder.sdvImg.setImageResource(mList.get(position).getImage());
            holder.sdvMark.setImageResource(mList.get(position).getImgHint());
            if (mList.get(position).getIndex() == 903) {
                holder.sdvMark.setVisibility(View.GONE);
                holder.sdvTimeLimit.setImageResource(mList.get(position).getImgTimeLimit());
            }
            holder.sdvStart.setImageResource(mList.get(position).getImgStart());


            holder.sdvStart.setOnClickListener(new OnCheckNetClickListener() {
                @Override
                public void onMultiClick(View v) {
                    if (Config.getIsFiguresMask()) {
                        if (mList.size() > 0) {
                            if (mList.get(position).getStatus() == 0 &&  !TextUtils.isEmpty(mList.get(position).getUrl())) {
                                loadGame(mList.get(position).getIndex(),mList.get(position).getUrl(),mList.get(position).getSdk_index());
                            }else {
                                new AlterDialogBuilder(GameActivity.this,new DialogTextView(GameActivity.this,"限时游戏，敬请期待!")).hideRootSure();
                            }
                        }else {
                            Toast("网络异常！");
                        }
                    }else {
                        new AlterDialogBuilder(GameActivity.this,new DialogTextView(GameActivity.this,"暂无人物形象，请前往 '研究院' 创建人物！")).hideRootSure();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mList.size() > 0) ? mList.size() : 0;
        }

        class GameViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView sdvImg, sdvMark,sdvTimeLimit, sdvStart;
            GameViewHolder(View itemView) {
                super(itemView);
                sdvImg = itemView.findViewById(R.id.sdv_img);
                sdvMark = itemView.findViewById(R.id.sdv_mark);
                sdvTimeLimit=itemView.findViewById(R.id.sdv_TimeLimit);
                sdvStart = itemView.findViewById(R.id.sdv_Start);
            }
        }
    }

    private void loadGame(int index, String url,int matchType){
        Bundle bundle = new Bundle();
        bundle.putString(GameRoomActivity.Html_Url, url);
        bundle.putInt(GameRoomActivity.GAME_TYPE, matchType);
//        switch (index) {
//            case 902: //晕头转向
//                bundle.putInt(GameRoomActivity.GAME_TYPE, Constant.MATCH_TYPE_HEXTRIS);
//                break;
//            case 901://消灭星星
//                bundle.putInt(GameRoomActivity.GAME_TYPE, Constant.MATCH_TYPE_POPSTART);
//                break;
//            case 900:  //盖房子
//                bundle.putInt(GameRoomActivity.GAME_TYPE, Constant.MATCH_TYPE_TOWER);
//                break;
//        }
        GameRoomActivity.newInstance(GameActivity.this, bundle);
    }

//    int broken=0;
//    @SuppressLint("InflateParams")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(DNGameEventMessage event) {
//        switch (event.getEvent()) {
//            case SESSION_BROKEN: {
//                Log.i(TAG, "onEvent: SESSION_BROKEN");
//                broken++;
//                if (broken==7) {
//                    AlterDialogBuilder dialogOccupy=new AlterDialogBuilder(this, new DialogTextView(this, "数据连接异常，请重新登录！"),"退出").hideClose();
//                    dialogOccupy.getRootSure().setOnClickListener(v -> {
//                        broken=0;
//                        dialogOccupy.dismiss();
//                        PublicEventMessage.ExitAccount(this);
//                        finish();
//                    });
//                }
//                break;
//            }
//            case SESSION_OCCUPY: {
//                Log.i(TAG, "onEvent: SESSION_OCCUPY");
//                AlterDialogBuilder dialogOccupy=new AlterDialogBuilder(this, new DialogTextView(this, "账号异地登录，请重新登录！"),"退出").hideClose();
//                dialogOccupy.getRootSure().setOnClickListener(v -> {
//                    dialogOccupy.dismiss();
//                    PublicEventMessage.ExitAccount(this);
//                    finish();
//                });
//                break;
//            }
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
        mList.clear();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachPresenter();
            mPresenter = null;
        }
        if (mList != null) {
            mList.clear();
            mList = null;
        }

        if (manager != null) {
            manager = null;
        }
        if (mAdapter != null) {
            mAdapter = null;
        }

        super.onDestroy();
    }
}
