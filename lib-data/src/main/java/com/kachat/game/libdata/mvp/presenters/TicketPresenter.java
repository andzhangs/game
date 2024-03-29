package com.kachat.game.libdata.mvp.presenters;


import android.support.annotation.NonNull;

import com.kachat.game.libdata.CodeType;
import com.kachat.game.libdata.model.BaseBean;
import com.kachat.game.libdata.model.MessageBean;
import com.kachat.game.libdata.mvp.OnPresenterListeners;
import com.kachat.game.libdata.mvp.models.CaptchaModel;
import com.kachat.game.libdata.mvp.models.TicketModel;

public class TicketPresenter {

    private TicketModel mModel;
    private OnPresenterListeners.OnViewListener<MessageBean> mView;

    public TicketPresenter(OnPresenterListeners.OnViewListener<MessageBean> view) {
        this.mModel=new TicketModel();
        this.mView = view;
    }

    public void attachPresenter(){
        this.mModel.getUserTicket(new OnPresenterListeners.OnModelListener<BaseBean<MessageBean>>() {
            @Override
            public void onSuccess(BaseBean<MessageBean> result) {
                if (mView != null) {
                    if (result.getCode() == CodeType.CODE_RESPONSE_SUCCESS) {
                        TicketPresenter.this.mView.onSuccess(result.getResult());
                    }else {
                        TicketPresenter.this.mView.onFailed(result.getCode(),result.getError());
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
