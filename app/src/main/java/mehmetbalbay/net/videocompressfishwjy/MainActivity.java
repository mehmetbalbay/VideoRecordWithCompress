package mehmetbalbay.net.videocompressfishwjy;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.vincent.videocompressor.VideoCompress;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mehmetbalbay.net.videocompressfishwjy.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private static final int REQUEST_FOR_VIDEO_FILE = 1000;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private String inputPath;
    private String outputPath;

    private long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mBinding.tvOutput.setText(outputDir);

        mBinding.btnRecordVideo.setOnClickListener(view -> {
            dispatchTakeVideoIntent();
        });

        mBinding.btnSelect.setOnClickListener(view -> {
            Intent intent = new Intent();
            /* Pictures image */
            //intent.setType("video/*;image/*");
            //intent.setType("audio/*");
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_FOR_VIDEO_FILE);
        });

        mBinding.btnCompress.setOnClickListener(view -> {
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_VIDEO_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    inputPath = Util.getFilePath(this, data.getData());
                    mBinding.tvInput.setText(inputPath);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri videoUri = data.getData();
                mBinding.videoView.setVideoURI(videoUri);
                mBinding.videoView.start();

                try {
                    String videoPath = Util.getFilePath(MainActivity.this, videoUri);
                    String destPath = outputDir + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";

                    compressVideoCapture(videoPath, destPath);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private Locale getLocale() {
        Configuration config = getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }

    public static Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void compressVideoCapture(String inputPath, String destPath) {
        VideoCompress.compressVideoLow(inputPath, destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                mBinding.tvIndicator.setText("Compressing..." + "\n"
                        + "Start at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                mBinding.pbCompress.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
                Util.writeFile(MainActivity.this, "Start at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                String previous = mBinding.tvIndicator.getText().toString();
                mBinding.tvIndicator.setText(previous + "\n"
                        + "Compress Success!" + "\n"
                        + "End at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                mBinding.pbCompress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(MainActivity.this, "End at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                Util.writeFile(MainActivity.this, "Total: " + ((endTime - startTime) / 1000) + "s" + "\n");
                Util.writeFile(MainActivity.this);
            }

            @Override
            public void onFail() {
                mBinding.tvIndicator.setText("Compress Failed");
                mBinding.pbCompress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(MainActivity.this, "Failed Compress!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
            }

            @Override
            public void onProgress(float percent) {
                mBinding.tvProgress.setText(String.valueOf(percent) + "%");
            }
        });
    }
}