package com.kachat.game.libdata.mvp.presenters;


import android.support.annotation.NonNull;

import com.kachat.game.libdata.CodeType;
import com.kachat.game.libdata.model.MessageBean;
import com.kachat.game.libdata.model.ScenesBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.models.ScenesModel;
import com.kachat.game.libdata.mvp.models.TicketModel;

public class ScenesPresenter {

    private ScenesModel mModel;
    private OnPresenterListeners.OnViewListener<ScenesBean> mView;

    public ScenesPresenter(OnPresenterListeners.OnViewListener<ScenesBean> view) {
        this.mModel=new ScenesModel();
        this.mView = view;
    }

    public void attachPresenter(@NonNull String uid){
        this.mModel.getUserScenes(uid, new OnPresenterListeners.OnModelListener<ScenesBean>() {
            @Override
            public void onSuccess(ScenesBean result) {
                if (mView != null) {
                    if (result.getCode() == CodeType.REQUEST_SUCCESS) {
                        ScenesPresenter.this.mView.onSuccess(result);
                    }else {
                        ScenesPresenter.this.mView.onFailed(result.getCode(),result.getError());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable != null) {
                    if (mView != null) {
                        mView.onError(throwable);
                    }
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
