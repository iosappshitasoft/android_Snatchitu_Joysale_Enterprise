package com.ni.addons;

import static android.content.Context.MODE_PRIVATE;
import static com.ni.utils.Constants.isCallPopup;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.ni.helper.callback.OkCallback;
import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;

public class DialogOverLayPermission extends DialogFragment {

    private static final String TAG = DialogOverLayPermission.class.getSimpleName();
    private AppCompatTextView txtTitle;
    private ImageView dimenImage;
    private AppCompatTextView txtDescription;
    private Button btnAllow;
    private Button btnDeny;
    private CheckBox btnDontAsk;
    private Context context;
    private OkCallback callBack;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_white)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_overlay_permission, container, false);
        preferences = context.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = preferences.edit();
        findViews(itemView);
        btnDontAsk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
//                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_white));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.secondaryText));
                    btnAllow.setEnabled(false);
                } else {
//                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_primary));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.white));
                    btnAllow.setEnabled(true);
                }
            }
        });
        return itemView;

    }

    private void findViews(View itemView) {
        txtTitle = (AppCompatTextView) itemView.findViewById(R.id.txtTitle);
        dimenImage = (ImageView) itemView.findViewById(R.id.dimenImage);
        txtDescription = (AppCompatTextView) itemView.findViewById(R.id.txtDescription);
        btnAllow = (Button) itemView.findViewById(R.id.btnAllow);
        btnDeny = (Button) itemView.findViewById(R.id.btnDeny);
        btnDontAsk = (CheckBox) itemView.findViewById(R.id.btnDontAsk);

        btnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.onOkClicked(true);
            }
        });
        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean(Constants.POP_UP_WINDOW_PERMISSION, !btnDontAsk.isChecked());
                if (btnDontAsk.isChecked()) {
                    editor.putString(Constants.POP_UP_WINDOW_PERMISSION_ASK_AGAIN, "true");
                } else {
                    isCallPopup = true;
                    editor.putString(Constants.POP_UP_WINDOW_PERMISSION_ASK_AGAIN, "false");
                }
                editor.apply();
                Log.d(TAG, "onClick: " + btnDontAsk.isChecked());
                callBack.onOkClicked(false);
            }
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OkCallback callBack) {
        this.callBack = callBack;
    }
}
