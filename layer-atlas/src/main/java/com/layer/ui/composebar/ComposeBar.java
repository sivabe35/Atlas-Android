package com.layer.ui.composebar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.ui.R;
import com.layer.ui.databinding.UiComposeBarAttachmentMenuItemBinding;
import com.layer.ui.databinding.UiComposeBarBinding;
import com.layer.ui.message.messagetypes.AttachmentSender;
import com.layer.ui.message.messagetypes.MessageSender;
import com.layer.ui.message.text.TextSender;
import com.layer.ui.message.text.RichTextSender;
import com.layer.ui.util.EditTextUtil;

import java.util.ArrayList;
import java.util.List;

public class ComposeBar extends FrameLayout implements TextWatcher {

    protected EditText mEditText;
    protected Button mSendButton;

    protected OnFocusChangeListener mExternalEditTextOnFocusChangeListener;

    protected ImageButton mLeftButton1;
    protected ImageButton mLeftButton2;
    protected ImageButton mLeftButton3;
    protected ImageButton mDefaultAttachButton;

    protected ImageButton mRightButton1;
    protected ImageButton mRightButton2;
    protected ImageButton mRightButton3;
    protected ImageButton mRightButton4;

    protected LayerClient mLayerClient;
    protected Conversation mConversation;

    protected List<AttachmentSender> mAttachmentSenders = new ArrayList<AttachmentSender>();
    protected TextSender mTextSender;
    protected MessageSender.Callback mMessageSenderCallback;

    protected PopupWindow mAttachmentMenu;

    // style
    protected Drawable mAttachmentSendersBackground;

    public ComposeBar(Context context) {
        this(context, null);
    }

    public ComposeBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComposeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        UiComposeBarBinding binding = UiComposeBarBinding.inflate(LayoutInflater.from(getContext()), this, true);

        mEditText = binding.layerUiComposeBarEditText;
        mSendButton = binding.layerUiComposeBarSendButton;

        mLeftButton1 = binding.layerUiComposeBarButtonLeft1;
        mLeftButton2 = binding.layerUiComposeBarButtonLeft2;
        mLeftButton3 = binding.layerUiComposeBarButtonLeft3;
        mDefaultAttachButton = binding.layerUiComposeBarButtonLeft4;

        mRightButton1 = binding.layerUiComposeBarButtonRight1;
        mRightButton2 = binding.layerUiComposeBarButtonRight2;
        mRightButton3 = binding.layerUiComposeBarButtonRight3;
        mRightButton4 = binding.layerUiComposeBarButtonRight4;

        mEditText.addTextChangedListener(this);

        mSendButton.setEnabled(mEditText.getEditableText().length() > 0);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTextSender.requestSend(mEditText.getText().toString())) return;
                mEditText.setText("");
            }
        });

        mDefaultAttachButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!mAttachmentSenders.isEmpty()) {
                    LinearLayout menu = (LinearLayout) mAttachmentMenu.getContentView();
                    menu.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                    mAttachmentMenu.showAsDropDown(v, 0, -menu.getMeasuredHeight() - v.getHeight());
                }
            }
        });

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                setEditTextEnabledState(hasFocus);

                if (mExternalEditTextOnFocusChangeListener != null && isEnabled()) {
                    mExternalEditTextOnFocusChangeListener.onFocusChange(view, hasFocus);
                }
            }
        });

        // Attachment Menu

        if (mAttachmentMenu != null) throw new IllegalStateException("Already initialized menu");

        if (attrs == null) {
            mAttachmentMenu = new PopupWindow(context);
        } else {
            mAttachmentMenu = new PopupWindow(context, attrs, defStyleAttr);
        }
        mAttachmentMenu.setContentView(LayoutInflater.from(context).inflate(R.layout.ui_compose_bar_attachment_menu, null));
        mAttachmentMenu.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAttachmentMenu.setOutsideTouchable(true);

        mAttachmentMenu.setFocusable(true);

        parseAndApplyStyle(context, attrs, defStyleAttr);
    }

    protected void parseAndApplyStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ComposeBar, R.attr.ComposeBar, defStyle);
        setEnabled(ta.getBoolean(R.styleable.ComposeBar_android_enabled, true));

        mEditText.setTextColor(ta.getColor(R.styleable.ComposeBar_android_textColor, context.getResources().getColor(R.color.layer_ui_compose_bar_text)));

        int textSize = ta.getDimensionPixelSize(R.styleable.ComposeBar_android_textSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_compose_bar_text_size));
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        String typeFaceName = ta.getString(R.styleable.ComposeBar_inputTextTypeface);
        Typeface typeface = typeFaceName != null ? Typeface.create(typeFaceName, ta.getInt(R.styleable.ComposeBar_android_textStyle, Typeface.NORMAL)) : null;
        mEditText.setTypeface(typeface);

        int underlineColor = ta.getColor(R.styleable.ComposeBar_inputUnderlineColor, context.getResources().getColor(R.color.layer_ui_compose_bar_underline));
        int cursorColor = ta.getColor(R.styleable.ComposeBar_inputCursorColor, context.getResources().getColor(R.color.layer_ui_compose_bar_cursor));

        EditTextUtil.setCursorDrawableColor(mEditText, cursorColor);
        EditTextUtil.setUnderlineColor(mEditText, underlineColor);

        Drawable attachmentSendersBackground = ta.getDrawable(R.styleable.ComposeBar_attachmentSendersBackground);
        if (mAttachmentSendersBackground == null) {
            mAttachmentSendersBackground = ContextCompat.getDrawable(context, R.drawable.ui_popup_background);
        }

        mAttachmentMenu.setBackgroundDrawable(attachmentSendersBackground);

        mEditText.setHint(ta.getString(R.styleable.ComposeBar_android_hint));
        mEditText.setMaxLines(ta.getInt(R.styleable.ComposeBar_android_maxLines, 5));

        ta.recycle();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (mAttachmentSenders.isEmpty()) return superState;
        SavedState savedState = new SavedState(superState);
        for (AttachmentSender sender : mAttachmentSenders) {
            Parcelable parcelable = sender.onSaveInstanceState();
            if (parcelable == null) continue;
            savedState.put(sender.getClass(), parcelable);
        }
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        for (AttachmentSender sender : mAttachmentSenders) {
            Parcelable parcelable = savedState.get(sender.getClass());
            if (parcelable == null) continue;
            sender.onRestoreInstanceState(parcelable);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setEditTextEnabledState(enabled);

        mLeftButton1.setEnabled(enabled);
        mLeftButton2.setEnabled(enabled);
        mLeftButton3.setEnabled(enabled);
        mDefaultAttachButton.setEnabled(enabled);

        mRightButton1.setEnabled(enabled);
        mRightButton2.setEnabled(enabled);
        mRightButton3.setEnabled(enabled);
        mRightButton4.setEnabled(enabled);

        int color = enabled ? ContextCompat.getColor(this.getContext(), R.color.transparent) : ContextCompat.getColor(this.getContext(), R.color.layer_ui_color_primary_gray_1);
        this.setBackgroundColor(color);
    }

    protected void setEditTextEnabledState(boolean hasFocus) {
        if (isEnabled()) {
            boolean isSendEnabled = mEditText.getText().length() > 0;

            mEditText.setEnabled(hasFocus);
            mEditText.setFocusable(hasFocus);

            mSendButton.setEnabled(isSendEnabled);
        } else {
            mEditText.setEnabled(false);
            mEditText.setFocusable(false);
            mSendButton.setEnabled(false);
        }
    }

    public void setConversation(LayerClient layerClient, Conversation conversation) {
        mConversation = conversation;
        mLayerClient = layerClient;
        mTextSender = new RichTextSender(getContext(), layerClient);
        mTextSender.setConversation(conversation);
        for (AttachmentSender attachmentSender : mAttachmentSenders) {
            attachmentSender.setConversation(conversation);
        }
    }

    /**
     * Sets the TextSender used for sending composed text messages.
     *
     * @param textSender TextSender used for sending composed text messages.
     */

    public void setTextSender(TextSender textSender) {
        mTextSender = textSender;
        mTextSender.setConversation(mConversation);
    }

    /**
     * Adds AttachmentSenders to this ComposeBar's attachment menu.
     *
     * @param senders AttachmentSenders to add to this ComposeBar's attachment menu.
     */
    public void addAttachmentSendersToDefaultAttachmentButton(AttachmentSender... senders) {
        for (AttachmentSender sender : senders) {
            if (sender.getTitle() == null && sender.getIcon() == null) {
                throw new NullPointerException("Attachment handlers must have at least a mTitle or icon specified.");
            }
            if (mMessageSenderCallback != null) sender.setCallback(mMessageSenderCallback);
            mAttachmentSenders.add(sender);
            addAttachmentMenuItem(sender);
        }
        if (!mAttachmentSenders.isEmpty()) mDefaultAttachButton.setVisibility(View.VISIBLE);
    }

    protected void addAttachmentMenuItem(AttachmentSender sender) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout menuLayout = (LinearLayout) mAttachmentMenu.getContentView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            menuLayout.setBackground(mAttachmentSendersBackground);
        } else {
            menuLayout.setBackgroundDrawable(mAttachmentSendersBackground);
        }

        UiComposeBarAttachmentMenuItemBinding binding = UiComposeBarAttachmentMenuItemBinding.inflate(inflater, menuLayout, false);
        binding.setSender(sender);
        binding.executePendingBindings();

        if (sender.getIcon() != null) {
            Drawable iconDrawable = ContextCompat.getDrawable(getContext(), sender.getIcon());
            DrawableCompat.setTint(iconDrawable, getResources().getColor(R.color.layer_ui_icon_enabled));
            binding.title.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
        }

        menuLayout.addView(binding.getRoot());
    }

    public void setOnMessageEditTextFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        mExternalEditTextOnFocusChangeListener = onFocusChangeListener;
    }

    public void removeOnMessageEditTextFocusChangeListener() {
        mExternalEditTextOnFocusChangeListener = null;
    }

    /**
     * Must be called from Activity's onActivityResult to allow attachment senders to manage results
     * from e.g. selecting a gallery photo or taking a camera image.
     *
     * @param activity    Activity receiving the result.
     * @param requestCode Request code from the Activity's onActivityResult.
     * @param resultCode  Result code from the Activity's onActivityResult.
     * @param data        Intent data from the Activity's onActivityResult.
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    /**
     * Must be called from Activity's onRequestPermissionsResult to allow attachment senders to
     * manage dynamic permissions.
     *
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets an optional callback for receiving MessageSender events.  If non-null, overrides any
     * callbacks already set on MessageSenders.
     *
     * @param callback Callback to receive MessageSender events.
     * @return This ComposeBar.
     */
    public void setMessageSenderCallback(MessageSender.Callback callback) {
        mMessageSenderCallback = callback;
        if (mMessageSenderCallback == null) return;
        if (mTextSender != null) mTextSender.setCallback(callback);
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.setCallback(callback);
        }
    }

    // TextWatcher
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mConversation == null || mConversation.isDeleted()) return;
        if (editable.length() > 0) {
            if (!mSendButton.isEnabled()) {
                mSendButton.setEnabled(true);
            }
            mConversation.send(LayerTypingIndicatorListener.TypingIndicator.STARTED);
        } else {
            mSendButton.setEnabled(false);
            mConversation.send(LayerTypingIndicatorListener.TypingIndicator.FINISHED);
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    public Button getSendButton() {
        return mSendButton;
    }

    public ImageButton getLeftButton1() {
        return mLeftButton1;
    }

    public ImageButton getLeftButton2() {
        return mLeftButton2;
    }

    public ImageButton getLeftButton3() {
        return mLeftButton3;
    }

    public ImageButton getDefaultAttachButton() {
        return mDefaultAttachButton;
    }

    public ImageButton getRightButton1() {
        return mRightButton1;
    }

    public ImageButton getRightButton2() {
        return mRightButton2;
    }

    public ImageButton getRightButton3() {
        return mRightButton3;
    }

    public ImageButton getRightButton4() {
        return mRightButton4;
    }

    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Saves a map from AttachmentSender class to AttachmentSender saved instance.
     */
    protected static class SavedState extends BaseSavedState {
        Bundle mBundle = new Bundle();

        public SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState put(Class<? extends AttachmentSender> cls, Parcelable parcelable) {
            mBundle.putParcelable(cls.getName(), parcelable);
            return this;
        }

        Parcelable get(Class<? extends AttachmentSender> cls) {
            return mBundle.getParcelable(cls.getName());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(mBundle);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            mBundle = in.readBundle();
        }
    }

}
