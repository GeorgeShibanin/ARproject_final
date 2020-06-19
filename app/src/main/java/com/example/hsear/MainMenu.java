package com.example.hsear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.renderscript.Script;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainMenu extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArSceneView arView;
    private Session session;
    private boolean shouldConfigureSession=false;
    private TextView textView;
    private ExternalTexture texture;
    private MediaPlayer mediaPlayer;
    private ModelRenderable videorend;
    int flag = 0;
    int cnt = 0;
    private Config config;
    AugmentedImageDatabase augmentedImageDatabase;
    String pathtoImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View
        arView = (ArSceneView)findViewById(R.id.arView);

        textView = findViewById(R.id.textView);

        texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this, R.raw.video);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);
        pathtoImage = getIntent().getStringExtra("path");

        findViewById(R.id.serializeBtn)
                .setOnClickListener(v -> {

                    if(ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        return;
                    }
                    serialize();
                });


        //Request  permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainMenu.this, "Permossion meed to display camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();



        initSceneView();
    }

    private void serialize() {

        if (pathtoImage == null) {
            Toast.makeText(this, "Can't serialize database cos image is null", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(getExternalFilesDir(null) + "/db.imgdb");


        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            Bitmap bitmap = BitmapFactory.decodeFile(pathtoImage);
            augmentedImageDatabase.addImage(String.valueOf(cnt), bitmap);
            cnt += 1;
            flag = 1;
            augmentedImageDatabase.serialize(outputStream);
            outputStream.close();
            configSession();
            textView.setText("Image number " + String.valueOf(cnt - 1) + " saved to Database ");
            Toast.makeText(this, "Database serialized", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void initSceneView() {
        arView.getScene().addOnUpdateListener(this::onUpdate);
    }

    private void setupSession() {
        if(session == null) {
            try {
                session = new Session(this);
            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            }
            shouldConfigureSession = true;

        }
        if(shouldConfigureSession) {
            configSession();
            shouldConfigureSession=false;
            arView.setupSession(session);
        }

        try {
            session.resume();
            arView.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
            session = null;
            return;
        }
    }

    private void configSession() {
        config = new Config(session);
        if (!buildDatabase(config)) {
            Toast.makeText(this, "Error database", Toast.LENGTH_SHORT).show();
        }
        if (flag == 1) {
            buildDatabase(config);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
    }

    private boolean buildDatabase(Config config) {


        try {
            if (flag == 1) {
                File file = new File(getExternalFilesDir(null) + "/db.imgdb");
                FileInputStream dbStream = new FileInputStream(file);
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, dbStream);
            } else {
                InputStream inputStream = getAssets().open("imageDB.imgdb");
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, inputStream);
            }
            config.setAugmentedImageDatabase(augmentedImageDatabase);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arView.getArFrame();
        Collection<AugmentedImage> updateAugmentedImg = frame.getUpdatedTrackables(AugmentedImage.class);

        for(AugmentedImage image:updateAugmentedImg) {
            if(image.getTrackingState() == TrackingState.TRACKING) {

                if(image.getName().equals("pcImage.jpg")) {
                    textView.setText("pc model is visible");
                    MyARNode node = new MyARNode(this, R.raw.pc2_model);
                    node.setImage(image);
                    arView.getScene().addChild(node);
                    break;
                } else if(image.getName().equals("dinoImage.jpg")) {
                    textView.setText("dino is visible");
                    MyARNode node = new MyARNode(this, R.raw.dino);
                    node.setImage(image);
                    arView.getScene().addChild(node);
                    break;
                } else if(image.getName().equals("delorian.jpg")) {
                    textView.setText("Video");
                    ModelRenderable
                            .builder()
                            .setSource(this, R.raw.video_screen)
                            .build()
                            .thenAccept(modelRenderable -> {
                                modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                                modelRenderable.getMaterial().setFloat4("keycolor", new Color(0.01843f, 1f,0.098f));

                                videorend = modelRenderable;
                            });
                    playVideo (image.createAnchor(image.getCenterPose()), image.getExtentX(), image.getExtentZ());
                    break;
                } else if (image.getName().equals("0")) {
                    textView.setText("your 1 image is detected");

                } else if (image.getName().equals("1")) {
                    textView.setText("your 2 image is detected");
                } else if (image.getName().equals("2")) {
                    textView.setText("your 3 image is detected");
                } else if (image.getName().equals("3")) {
                    textView.setText("your 4 image is detected");
                }

            }
        }
    }





    private void playVideo(Anchor anchor, float extentX, float extentZ) {
        mediaPlayer.start();
        AnchorNode anchorNode = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(videorend);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });

        anchorNode.setWorldScale(new Vector3(extentX, 1f, extentZ));

        arView.getScene().addChild(anchorNode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainMenu.this, "Permossion meed to display camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null) {
            arView.pause();
            session.pause();
        }
    }

}