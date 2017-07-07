/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.ui.utils.Utils;
import co.chatsdk.core.dao.DaoCore;
import com.braunster.chatsdk.network.TwitterManager;
import co.chatsdk.core.types.ChatError;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringUtils;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import java.io.File;
import java.util.concurrent.Callable;

import timber.log.Timber;
import uk.co.senab.photoview.PhotoView;

public class DialogUtils {

    public static final String TAG = DialogUtils.class.getSimpleName();
    public static final boolean DEBUG = Debug.DialogUtils;

    /** A dialog that contain editText, Response from dialog is received through the interface.*/
    public static class ChatSDKEditTextDialog extends DialogFragment implements TextView.OnEditorActionListener {

        private EditText mEditText;
        private String dialogTitle = "Title";
        private EditTextDialogInterface listener;



        private UIHelper UIHelper;

        public static ChatSDKEditTextDialog getInstace(){
            ChatSDKEditTextDialog f = new ChatSDKEditTextDialog();

            return f;
        }

        public ChatSDKEditTextDialog() {
            // Empty constructor required for DialogFragment
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            UIHelper = UIHelper.getInstance();

            View view = inflater.inflate(R.layout.chat_sdk_dialog_edit_text, container);
            mEditText = (EditText) view.findViewById(R.id.et_enter);

            getDialog().setTitle(dialogTitle);

            // Show soft keyboard automatically
            mEditText.requestFocus();
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            // Listen to Done press on the keyboard.
            mEditText.setOnEditorActionListener(this);

            return view;
        }

        public void setTitleAndListen(String title, EditTextDialogInterface listener){
            this.dialogTitle = title;
            this.listener = listener;
        }

        /** Option to add more callbacks to the dialog.*/
        public interface EditTextDialogInterface extends DialogInterface<String>{

        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                if (mEditText.getText().toString().isEmpty())
                {
                    SuperToast toast = UIHelper.getAlertToast();
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.setText("Please enter chat name");
                    toast.show();
                    return true;
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                // Return input text to activity
                listener.onFinished(mEditText.getText().toString());
                this.dismiss();
                return true;
            }
            return false;
        }
    }

    public static class ChatSDKTwitterLoginDialog extends DialogFragment {

        private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";

        protected EditText etPin;
        private WebView webView;
        private OAuthService service;
        private Token requestToken;
        private LinearLayout progressBar;
        private Completion completion;

        public interface Completion {
            void complete(Throwable e);
        }

        public void addCompletionHandler (Completion c) {
            completion = c;
        }

        /** indicator that the login process has started, It is used to keep the webview hiding when the onPageFinished mehod is evoked.*/
        private boolean loginIn = false;

        public static ChatSDKTwitterLoginDialog getInstance(){
            ChatSDKTwitterLoginDialog dialog = new ChatSDKTwitterLoginDialog();
            return dialog;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.chat_sdk_dialog_twitter_login, null);

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            view.findViewById(R.id.content).setLayoutParams(new RelativeLayout.LayoutParams(width, height));

            webView = (WebView) view.findViewById(R.id.webView);
            etPin =  ((EditText)view.findViewById(R.id.chat_sdk_et_pin_code));
            progressBar = (LinearLayout) view.findViewById(R.id.chat_sdk_progressbar);

            webView.setWebViewClient(new WebViewClient(){

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (DEBUG) Timber.v("shouldOverrideUrlLoading, Url: %s", url);

                    if (url.startsWith(getString(R.string.twitter_callback_url) + "?denied"))
                    {
                        if(completion != null) {
                            completion.complete(ChatError.getError(ChatError.Code.OPERATION_FAILED, "Cancelled."));
                        }
                        dismiss();
                        return false;
                    }

                    if (!url.startsWith(getString(R.string.twitter_callback_url) + "?oauth_token"))
                        return false;

                    Uri uri = Uri.parse(url);
                    String ver = uri.getQueryParameter("oauth_verifier");

                    runVerifierThread(ver);

                    ((TextView) progressBar.findViewById(R.id.chat_sdk_progressbar_text)).setText(getActivity().getResources().getString(R.string.connecting));
                    webView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    loginIn = true;

                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (DEBUG) Timber.v("onPageFinished, Url: %s", url );

                    if (loginIn)
                        return;

                    progressBar.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            });

            etPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (etPin.getText().toString().isEmpty())
                            return true;

                        runVerifierThread(etPin.getText().toString());
                    }
                    return false;
                }
            });

            view.findViewById(R.id.chat_sdk_btn_done).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runVerifierThread(etPin.getText().toString());
                }
            });

            TwitterManager.getAuthorizationURLThread(getActivity(), handler).start();

            return view;
        }

        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case TwitterManager.ERROR:
                        if(completion != null) {
                            completion.complete((ChatError)msg.obj);
                        }

                        break;

                    case TwitterManager.SUCCESS:
                        webView.loadUrl((String) msg.obj);
                        break;
                }
            }
        };

        public void runVerifierThread(String text) {
            TwitterManager.runVerifierThread(getActivity(), etPin.getText().toString())
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            if(completion != null) {
                                completion.complete(null);
                            }
                        }
                    })
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if(completion != null) {
                                completion.complete(throwable);
                            }
                        }
                    }).subscribe();
        }

    }


    /** A popup to select the type of message to send, "Text", "Image", "Location".*/
    public static PopupWindow getMenuOptionPopup(Context context, View.OnClickListener listener){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.chat_sdk_popup_options, null);
        PopupWindow optionPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupView.findViewById(R.id.chat_sdk_btn_choose_picture).setOnClickListener(listener);
        popupView.findViewById(R.id.chat_sdk_btn_take_picture).setOnClickListener(listener);
        popupView.findViewById(R.id.chat_sdk_btn_location).setOnClickListener(listener);

        if (!Defines.Options.LocationEnabled){
            popupView.findViewById(R.id.chat_sdk_btn_location).setVisibility(View.GONE);
        }
        
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // TODO fix popup size to wrap view size.
        optionPopup.setContentView(popupView);
        optionPopup.setBackgroundDrawable(new BitmapDrawable());
        optionPopup.setOutsideTouchable(true);
        optionPopup.setWidth(popupView.getMeasuredWidth());
        optionPopup.setHeight(popupView.getMeasuredHeight());
        return optionPopup;
    }

    /** Full screen popup for showing an image in greater size.*/
    public static ImagePopupWindow getImageDialog(final Context context, String data, final ImagePopupWindow.LoadTypes loadingType){
        return getImageDialog(context, data, loadingType, false, "");
    }

    public static ImagePopupWindow getImageMessageDialog(final Context context, String data, final ImagePopupWindow.LoadTypes loadingType, String imageName){
        return getImageDialog(context, data, loadingType, true, imageName);
    }

    private static ImagePopupWindow getImageDialog(final Context context, String data, final ImagePopupWindow.LoadTypes loadingType, boolean saveAfterLoad, String imageName){

        if (StringUtils.isEmpty(data))
            return null;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.chat_sdk_popup_touch_image, null);

        // Full screen popup.
        final ImagePopupWindow imagePopup = new ImagePopupWindow(context, popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        imagePopup.setData(data);
        imagePopup.setImageName(imageName);
        imagePopup.setLoadingType(loadingType);
        imagePopup.saveToImageDir(saveAfterLoad);
        imagePopup.setContentView(popupView);
        imagePopup.setBackgroundDrawable(new BitmapDrawable());
        imagePopup.setOutsideTouchable(true);
        imagePopup.setAnimationStyle(R.style.ImagePopupAnimation);

        imagePopup.load();

        return imagePopup;
    }

    public static class ImagePopupWindow extends PopupWindow{
        private boolean saveToDir =false;

        private LoadTypes loadingType;

        private Context context;

        private String data;

        private UIHelper UIHelper;

        private String imageName = "";

        private View popupView;

        /** Type indicate from where to load the file.*/
        public enum LoadTypes{
            LOAD_FROM_PATH, LOAD_FROM_URL;
        }

        public ImagePopupWindow(Context ctx, View popupView, int width, int height, boolean focusable) {
            super(popupView, width, height, focusable);

            this.context = ctx;

            this.popupView = popupView;

            UIHelper = UIHelper.getInstance();
        }

        public void load (){
            // Dismiss popup when clicked.
            popupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            final PhotoView imageView = (PhotoView) popupView.findViewById(R.id.photo_view);
            final ProgressBar progressBar = (ProgressBar) popupView.findViewById(R.id.chat_sdk_popup_image_progressbar);

            switch (loadingType)
            {

                case LOAD_FROM_URL:
                    if (DEBUG) Timber.d("load from url");

                    progressBar.setVisibility(View.VISIBLE);

                    Ion.with(context).load(data).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, Bitmap result) {
                            if(e == null) {
                                imageView.setImageBitmap(result);

                                if (saveToDir) {
                                    File file, dir = Utils.ImageSaver.getAlbumStorageDir(context, Utils.ImageSaver.IMAGE_DIR_NAME);
                                    if (dir != null) {
                                        if(dir.exists()) {
                                            file = new File(dir, imageName + ".jpg");

                                            if (!file.exists()) {
                                                ImageUtils.saveBitmapToFile(file, result);

                                                ImageUtils.scanFilePathForGallery(context, file.getPath());

                                                // If the message name is equal to a message entity id we
                                                // save the image path to the message object so we could use it instead of downloading images.
                                                BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, imageName);

                                                if (message != null) {
                                                    message.setResourcesPath(file.getPath());
                                                    DaoCore.updateEntity(message);
                                                }

                                            }
                                        }
                                    }
                                }
                                animateIn(imageView, progressBar);
                            }
                            else {
                                UIHelper.showToast(R.string.unable_to_fetch_image);
                                dismiss();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    break;

                case LOAD_FROM_PATH:
                    if (DEBUG) Timber.d("load from path");
                    imageView.setImageBitmap(ImageUtils.loadBitmapFromFile(data));
                    animateIn(imageView, progressBar);
                    break;
            }
        }

        private void animateIn(final ImageView imageView, final ProgressBar progressBar){
            imageView.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            imageView.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        public void saveToImageDir(boolean saveToDir) {
            this.saveToDir = saveToDir;
        }

        public void setData(String data) {
            this.data = data;
        }

        public void setLoadingType(LoadTypes loadingType) {
            this.loadingType = loadingType;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
    }

    /** Basic interface for getting callback from the dialog.*/
    public interface DialogInterface<T>{
        public void onFinished(T t);
    }

    public static void showToastDialog(AppCompatActivity activity, String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        // set title if not null
        if (title != null && !title.equals(""))
            alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface dialog, int id) {
                        if (pos != null)
                            try {
                                pos.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(n, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (neg != null)
                            try {
                                neg.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
