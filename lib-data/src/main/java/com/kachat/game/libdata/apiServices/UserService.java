package com.kachat.game.libdata.apiServices;

import com.kachat.game.libdata.model.FeedBacksBean;
import com.kachat.game.libdata.model.GetCaptchaBean;
import com.kachat.game.libdata.model.LivesBean;
import com.kachat.game.libdata.model.MessageBean;
import com.kachat.game.libdata.model.PropsBean;
import com.kachat.game.libdata.model.ScenesBean;
import com.kachat.game.libdata.model.SingsBean;
import com.kachat.game.libdata.model.UserBean;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 *
 */
public interface UserService {


    //手机号注册获取验证码
    @FormUrlEncoded
    @POST("/captcha/register")
    Observable<GetCaptchaBean> postPhoneCaptchaImpl(@Field("mobile") String mobile);

    //注册
    @FormUrlEncoded
    @POST("/users")
    Observable<UserBean> postRegister(@Field("mobile") String mobile, @Field("password") String password,
                                              @Field("gender") String gender, @Field("age") String age,
                                              @Field("username") String username, @Field("system") int system);
//
//    // 重置密码请求验证码
//    @FormUrlEncoded
//    @POST("/captcha/reset")
//    Observable<BaseBean<GetCaptchaBean>> postPhoneCaptchaResetImpl(@Field("mobile") String mobile);

    //校验验证码
    @FormUrlEncoded
    @POST("/captcha/verify")
    Observable<MessageBean> postVerifyCaptcha(@Field("mobile") String mobile, @Field("captcha") String captcha);
//
//    //重置密码
//    @FormUrlEncoded
//    @POST("/password/reset")
//    Observable<BaseBean<MessageBean>> postResetPwd(@Field("mobile") String mobile, @Field("captcha") String captcha, @Field("password") String password);
//
//
    //登录
    @FormUrlEncoded
    @POST("/login")
    Observable<UserBean> postLoginImpl(@Field("mobile") String mobile, @Field("password") String password);

    //用户反馈
    @FormUrlEncoded
    @POST("/feedbacks")
    Observable<FeedBacksBean> postFeedBacks(@Field("token") String token, @Field("content") String content);


    //用户券数
    @GET("/users/{uid}/tickets")
    Observable<MessageBean> getUserTickets(@Path("uid") String uid);

    //用户拥有场景
    @GET("/users/{uid}/scenes")
    Observable<ScenesBean> getUserScenes(@Path("uid") String uid);

    //用户拥有场景
    @GET("/users/{uid}/props")
    Observable<PropsBean> getUserProps(@Query("token") String token, @Path("uid") String uid);

    //  http://api.e3webrtc.com:8004/
    //用户拥有场景
    @GET("/users/{uid}/lives")
    Observable<LivesBean> getUserLives(@Path("uid") String uid);


    // http://api.e3webrtc.com:8004/signs?user=1&user=1
    //用户签到
    @FormUrlEncoded
    @POST("/signs")
    Observable<SingsBean> postSigns(@Field("user") String uid, @Field("device") String deviceId);


}
