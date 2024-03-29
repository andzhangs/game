package com.kachat.game.libdata.mvp.presenters;


import android.support.annotation.NonNull;

import com.kachat.game.libdata.CodeType;
import com.kachat.game.libdata.model.BaseBean;
import com.kachat.game.libdata.model.MessageBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.models.HpModel;
import com.kachat.game.libdata.mvp.models.StatsPagesModel;

public class StatPagesPresenter {
    public enum StatType{ GAME,CHAT }

    private StatsPagesModel mModel;
    private OnPresenterListeners.OnViewListener<MessageBean> mView;

    public StatPagesPresenter(OnPresenterListeners.OnViewListener<MessageBean> view) {
        this.mModel=new StatsPagesModel();
        this.mView = view;
    }

    public void attachPresenter(@NonNull StatType type){
        String param="-1";
        switch (type) {
            case GAME:param="0";break;
            case CHAT:param="1";break;
        }

        this.mModel.postStatPages(param, new OnPresenterListeners.OnModelListener<BaseBean<MessageBean>>() {
            @Override
            public void onSuccess(BaseBean<MessageBean> result) {
                if (mView != null) {
                    if (result.getCode() == CodeType.CODE_RESPONSE_SUCCESS) {
                        StatPagesPresenter.this.mView.onSuccess(result.getResult());
                    }else {
                        StatPagesPresenter.this.mView.onFailed(result.getCode(),result.getError());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable != null) {
                    if (mView != null) mView.onError(throwable);
                }
            }
        });
    }

    public void detachPresenter(){
        if (mModel != null) {
            mModel.close();
        }

        if (mView != null) {
            mView = null;
        }
    }
}
