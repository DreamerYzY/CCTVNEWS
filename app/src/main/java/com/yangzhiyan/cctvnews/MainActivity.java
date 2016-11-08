package com.yangzhiyan.cctvnews;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;
import com.squareup.picasso.Picasso;
import com.yangzhiyan.cctvnews.interfaces.Constants;

public class MainActivity extends AppCompatActivity {
    private AuthInfo myauthinfo;
    private Oauth2AccessToken myaccesstoken;
    private SsoHandler ssohandler;
    private ImageView ivcover;
    private TextView tvusername;
    private UsersAPI userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivcover = (ImageView) findViewById(R.id.ivcover);
        tvusername = (TextView) findViewById(R.id.tvusername);

        myauthinfo = new AuthInfo(this, Constants.APP_KEY,Constants.REDIRECT_URL,Constants.SCOPE);

        weiboShareApi = WeiboShareSDK.createWeiboAPI(this,Constants.APP_KEY);
        weiboShareApi.registerApp();

        ivcover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssohandler = new SsoHandler(MainActivity.this,myauthinfo);
                ssohandler.authorize(new AuthListener());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ssohandler != null){
            ssohandler.authorizeCallBack(requestCode,resultCode,data);
        }
    }

    class AuthListener implements WeiboAuthListener{

        @Override
        public void onComplete(Bundle bundle) {
            myaccesstoken = Oauth2AccessToken.parseAccessToken(bundle);
            if (myaccesstoken.isSessionValid()){
                String userToken = myaccesstoken.getToken();
                String userId = myaccesstoken.getUid();

                Log.i("Tag","userToken is "+ userToken+", userId is "+userId);

                userApi = new UsersAPI(MainActivity.this,Constants.APP_KEY,myaccesstoken);
                userApi.show(Long.parseLong(userId),requestListener);
            }else {
                String code = bundle.getString("code","");
            }

        }

        @Override
        public void onWeiboException(WeiboException e) {
            e.printStackTrace();
        }

        @Override
        public void onCancel() {

        }
    }
    private RequestListener requestListener = new RequestListener() {
        @Override
        public void onComplete(String s) {
            if (!TextUtils.isEmpty(s)){
                User user = User.parse(s);
                Log.i("Tag","name is"+user.name+", imageurl is"+user.avatar_hd);
                tvusername.setText(user.name);
                Picasso.with(MainActivity.this).load(user.avatar_hd).into(ivcover);
                ivcover.setClickable(false);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            e.printStackTrace();
        }
    };

    private IWeiboShareAPI weiboShareApi;
    public void share(View view){
        WeiboMultiMessage message = new WeiboMultiMessage();

        TextObject textObject = new TextObject();
        textObject.text = "这个APP很好用，大家一起来下载吧";
        message.textObject = textObject;

        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(BitmapFactory.decodeResource(getResources(),R.mipmap.app_icon));
        message.imageObject = imageObject;

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = message;
        weiboShareApi.sendRequest(this,request);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        weiboShareApi.handleWeiboResponse(intent,response);
    }

    private IWeiboHandler.Response response = new IWeiboHandler.Response() {
        @Override
        public void onResponse(BaseResponse baseResponse) {
            switch (baseResponse.errCode){
                case WBConstants.ErrorCode.ERR_OK:
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    break;
            }
        }
    };
}
