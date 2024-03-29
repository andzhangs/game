package com.kachat.game.ui.graduate.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kachat.game.R;
import com.kachat.game.base.BaseFragment;
import com.kachat.game.events.PublicEventMessage;
import com.kachat.game.libdata.controls.DaoQuery;
import com.kachat.game.libdata.model.ErrorBean;
import com.kachat.game.libdata.model.LivesBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.presenters.LivesPresenter;
import com.kachat.game.model.Live2DModel;
import com.kachat.game.ui.graduate.GraduateSchoolActivity;
import com.kachat.game.utils.widgets.AlterDialogBuilder;
import com.kachat.game.utils.widgets.DialogTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import cn.lemon.view.SpaceItemDecoration;

public class LivePersonModeListFragment extends BaseFragment {
    public static final String TAG = "LivePersonModeList";

    @BindView(R.id.rv_switch_bg)
    RecyclerView mRvSwitchBg;

    private List<Live2DModel> mListLocal;
    private List<LivesBean.ChildLivesBean> mListOnline = null;
    private Live2DBgAdapter mAdapter = null;

    private LivesPresenter mLivesPresenter = null;

    public LivePersonModeListFragment() {
    }

    public static LivePersonModeListFragment getInstance() {
        return Live2DModeListFragmentHolder.instance;
    }

    private static class Live2DModeListFragmentHolder {
        @SuppressLint("StaticFieldLeak")
        private static final LivePersonModeListFragment instance = new LivePersonModeListFragment();
    }

    @Override
    public int onSetResourceLayout() {
        return R.layout.fragment_live2_mode_list;
    }

    @Override
    public void onInitView(@NonNull View view) {
//        mRvSwitchBg = view.findViewById(R.id.rv_switch_bg);
//        mListLocal = new ArrayList<>();
        mListOnline = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        mRvSwitchBg.setLayoutManager(manager);
        mRvSwitchBg.addItemDecoration(new SpaceItemDecoration(2, 0, 2, 0));
        mAdapter = new Live2DBgAdapter();
        mRvSwitchBg.setAdapter(mAdapter);

        mLivesPresenter = new LivesPresenter(new MaskCallBack());
        mLivesPresenter.attachPresenter();
    }

    private class MaskCallBack implements OnPresenterListeners.OnViewListener<LivesBean> {
        @Override
        public void onSuccess(LivesBean result) {
            mListOnline.clear();
            if (result != null && result.getLives() != null && result.getLives().size() > 0) {

                int size=result.getLives().size();
                for (int i = 0; i < size; i++) {
                    if (DaoQuery.queryListModelListSize() > 0) {
                        if (Objects.requireNonNull(DaoQuery.queryModelListData()).get(0).getLiveFileName().equals(result.getLives().get(i).getLive().getName())) {
                            result.getLives().get(i).setIsFlag(true);
                        }else {
                            result.getLives().get(i).setIsFlag(false);
                        }
                    }else {
                        if (i != 0) {
                            result.getLives().get(i).setIsFlag(false);
                        }else {
                            result.getLives().get(i).setIsFlag(true);
                        }
                    }

                }
                mListOnline.addAll(result.getLives());
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailed(int errorCode, ErrorBean error) {
            if (error != null && !TextUtils.isEmpty(error.getToast())) {
                Toast(error.getToast());
            }
        }

        @Override
        public void onError(Throwable e) {
            if (e != null) {
                Logger(e.getMessage());
                Toast(e.getMessage());
            }
        }
    }

    private int LAYOUT_UNCLOCKED=0, LAYOUT_CLOCKED=1;
    public class Live2DBgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (mListOnline.get(position).getLive_number() == 0) {
                return LAYOUT_CLOCKED;
            }
            return LAYOUT_UNCLOCKED;
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == LAYOUT_CLOCKED) {
                return new ClockViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_figures_mask_clock, null));
            }
            return new UNClockViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_figures_mask_unclock, null));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ClockViewHolder) {
                ((ClockViewHolder) holder).mSdvLive2d.setImageURI(Uri.parse(mListOnline.get(position).getLive().getImage_url()));
                if (mListOnline.get(position).getIsFlag()) {
                    ((ClockViewHolder) holder).mLayoutCompat.setBackgroundResource(R.drawable.radius_5_light_white);
                } else {
                    ((ClockViewHolder) holder).mLayoutCompat.setBackgroundResource(R.drawable.radius_5);
                }
                ((ClockViewHolder) holder).acTvNumbers.setText(mListOnline.get(position).getLive_chip_number() + "/" + mListOnline.get(position).getLive().getUnlock_chip());
            }else if (holder instanceof UNClockViewHolder) {
                ((UNClockViewHolder) holder).mSdvLive2d.setImageURI(Uri.parse(mListOnline.get(position).getLive().getImage_url()));
                if (mListOnline.get(position).getIsFlag()) {
                    ((UNClockViewHolder) holder).mLayoutCompat.setBackgroundResource(R.drawable.radius_5_light_white);
                } else {
                    ((UNClockViewHolder) holder).mLayoutCompat.setBackgroundResource(R.drawable.radius_5);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (mListOnline.get(position).getLive_number()==0) {
                    AlterDialogBuilder dialogBuilder = new AlterDialogBuilder(Objects.requireNonNull(getContext()), new DialogTextView(getContext(), "碎片不足!"));
                    dialogBuilder.getRootSure().setOnClickListener(v1 -> dialogBuilder.dismiss());
                } else {
                    EventBus.getDefault().post(new PublicEventMessage.OnGraduateEvent(GraduateSchoolActivity.LAYOUT_FIGURES_MASK,mListOnline.get(position).getLive().getName()));
                }

                for (int i = 0; i < mListOnline.size(); i++) {
                    if (i == position) {
                        mListOnline.get(i).setIsFlag(true);
                    } else {
                        mListOnline.get(i).setIsFlag(false);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mListOnline.size() > 0) ? mListOnline.size() : 0;
        }

        class UNClockViewHolder extends RecyclerView.ViewHolder { //解锁
            LinearLayoutCompat mLayoutCompat;
            SimpleDraweeView mSdvLive2d;

            UNClockViewHolder(View itemView) {
                super(itemView);
                mLayoutCompat = itemView.findViewById(R.id.ll_Item_Container);
                mSdvLive2d = itemView.findViewById(R.id.sdv_live2d);
            }
        }

        class ClockViewHolder extends RecyclerView.ViewHolder {  //未解锁
            LinearLayoutCompat mLayoutCompat;
            SimpleDraweeView mSdvLive2d;
            RelativeLayout rlMask;
            AppCompatTextView acTvNumbers;

            ClockViewHolder(View itemView) {
                super(itemView);
                mLayoutCompat = itemView.findViewById(R.id.ll_Item_Container);
                mSdvLive2d = itemView.findViewById(R.id.sdv_live2d);
                rlMask = itemView.findViewById(R.id.rl_MaskClock);
                acTvNumbers = itemView.findViewById(R.id.acTv_numbers);
            }

        }

    }


    @Override
    public void onDestroyView() {

        if (mLivesPresenter != null) {
            mLivesPresenter.detachPresenter();
            mLivesPresenter = null;
        }

        if (mListOnline != null) {
            mListOnline.clear();
            mListOnline = null;
        }

        super.onDestroyView();
    }
}
