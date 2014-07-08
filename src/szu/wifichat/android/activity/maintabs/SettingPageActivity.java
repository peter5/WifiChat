package szu.wifichat.android.activity.maintabs;

import szu.wifichat.android.BaseApplication;
import szu.wifichat.android.BaseDialog;
import szu.wifichat.android.R;
import szu.wifichat.android.activity.AboutActivity;
import szu.wifichat.android.activity.SettingMyInfoPageActivity;
import szu.wifichat.android.socket.udp.UDPSocketThread;
import szu.wifichat.android.sql.SqlDBOperate;
import szu.wifichat.android.util.FileUtils;
import szu.wifichat.android.view.HeaderLayout;
import szu.wifichat.android.view.HeaderLayout.HeaderStyle;
import szu.wifichat.android.view.SettingSwitchButton;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SettingPageActivity extends TabItemActivity implements
        OnClickListener, OnCheckedChangeListener,
        DialogInterface.OnClickListener {

    private HeaderLayout mHeaderLayout;

    private Button mAboutUsButton;
    private Button mDeleteAllChattingInfoButton;
    private Button mExitApplicationButton;

    private ImageView mSettingInfoButton;
    private SettingSwitchButton mSoundSwitchButton;
    private SettingSwitchButton mVibrateSwitchButton;
    private RelativeLayout mSettingInfoLayoutButton;

    private BaseDialog mDeleteCacheDialog; // 提示窗口
    private BaseDialog mExitDialog;
    private SqlDBOperate mSqlDBOperate;

    private int mDialogFlag;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settting_page);
        initViews();
        initEvents();
        init();
        mContext = this;
    }

    @Override
    protected void initViews() {
        // TODO Auto-generated method stub
        mHeaderLayout = (HeaderLayout) findViewById(R.id.setting_page_header);
        mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
        mHeaderLayout.setDefaultTitle("设置", null);

        mSettingInfoButton = (ImageView) findViewById(R.id.btn_setting_my_information);
        mSettingInfoLayoutButton = (RelativeLayout) findViewById(R.id.setting_my_info_layout);
        mSoundSwitchButton = (SettingSwitchButton) findViewById(R.id.checkbox_sound);
        mVibrateSwitchButton = (SettingSwitchButton) findViewById(R.id.checkbox_vibration);
        mDeleteAllChattingInfoButton = (Button) findViewById(R.id.btn_delete_all_chattinginfo);
        mAboutUsButton = (Button) findViewById(R.id.btn_about_us);
        mExitApplicationButton = (Button) findViewById(R.id.btn_exit_application);
    }

    @Override
    protected void initEvents() {
        // TODO Auto-generated method stub
        mSettingInfoButton.setOnClickListener(this);
        mSettingInfoLayoutButton.setOnClickListener(this);
        mSoundSwitchButton.setOnCheckedChangeListener(this);
        mVibrateSwitchButton.setOnCheckedChangeListener(this);
        mDeleteAllChattingInfoButton.setOnClickListener(this);
        mAboutUsButton.setOnClickListener(this);
        mExitApplicationButton.setOnClickListener(this);

    }

    @Override
    protected void init() {
        // TODO Auto-generated method stub
        mDeleteCacheDialog = BaseDialog.getDialog(SettingPageActivity.this,
                R.string.dialog_tips, "删除聊天记录会删除所有接收的图片、语音和文件。", "确 定", this,
                "取 消", this);
        mDeleteCacheDialog.setButton1Background(R.drawable.btn_default_popsubmit);

        mExitDialog = BaseDialog.getDialog(SettingPageActivity.this,
                R.string.dialog_tips, "确定要退出软件", "确 定", this, "取 消", this);
        mExitDialog.setButton1Background(R.drawable.btn_default_popsubmit);

        mSoundSwitchButton.setChecked(BaseApplication.getSoundFlag());
        mVibrateSwitchButton.setChecked(BaseApplication.getVibrateFlag());
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        // case R.id.btn_setting_my_information:
        case R.id.setting_my_info_layout:
            startActivity(SettingMyInfoPageActivity.class);
            break;

        case R.id.btn_delete_all_chattinginfo:
            mDialogFlag = 1;
            mDeleteCacheDialog.show();
            break;

        case R.id.btn_about_us:
//            startActivity(AboutActivity.class);
            break;

        case R.id.btn_exit_application:
            mDialogFlag = 2;
            mExitDialog.show();
            break;

        default:
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        switch (buttonView.getId()) {
        case R.id.checkbox_sound:
            buttonView.setChecked(isChecked);
            BaseApplication.setSoundFlag(isChecked);
            break;

        case R.id.checkbox_vibration:
            buttonView.setChecked(isChecked);
            BaseApplication.setVibrateFlag(isChecked);
            break;

        default:
            break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub

        switch (mDialogFlag) {
        case 1:
            if (which == 0) {
                setAsyncTask(1);
            } else if (which == 1) {
                mDeleteCacheDialog.dismiss();
            }
            break;
        case 2:
            if (which == 0) {
                setAsyncTask(2);
            } else if (which == 1) {
                mExitDialog.dismiss();
            }
            break;
        }
    }

    private void setAsyncTask(final int flag) {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                switch (flag) {
                case 1:
                    mDeleteCacheDialog.dismiss();
                    showLoadingDialog("正在删除聊天记录...");
                    break;
                case 2:
                    mExitDialog.dismiss();
                    showLoadingDialog("正在退出程序...");
                    break;
                default:
                    break;
                }

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    switch (flag) {
                    case 1:
                        mSqlDBOperate = new SqlDBOperate(
                                SettingPageActivity.this);
                        mSqlDBOperate.deteleAllChattingInfo(); // 删除所有聊天记录
                        mSqlDBOperate.close();
                        FileUtils.delAllFile(BaseApplication.SAVE_PATH);
                        break;

                    case 2:
                        mUDPSocketThread = UDPSocketThread.getInstance(mApplication, mContext);
                        mUDPSocketThread.notifyOffline();
                        break;

                    default:
                        break;
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
                if (result) {
                    dismissLoadingDialog();
                    switch (flag) {
                    case 1:
                        showLongToast("删除聊天记录成功");
                        break;

                    case 2:
                        finish();
                        break;

                    default:
                        break;
                    }

                } else {
                    showShortToast("操作失败,请尝试重启程序。");
                }
            }
        });
    }

    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub

    }
}
