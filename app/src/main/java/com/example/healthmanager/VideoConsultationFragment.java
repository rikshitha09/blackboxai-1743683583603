package com.example.healthmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class VideoConsultationFragment extends Fragment {
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private RtcEngine agoraEngine;
    private FrameLayout localVideoContainer;
    private FrameLayout remoteVideoContainer;
    private boolean isJoined = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_consultation, container, false);
        
        localVideoContainer = view.findViewById(R.id.localVideoView);
        remoteVideoContainer = view.findViewById(R.id.remoteVideoView);
        
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(requireActivity(), REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        } else {
            setupVideoSDKEngine();
        }
        
        view.findViewById(R.id.fabStartCall).setOnClickListener(v -> {
            if (!isJoined) {
                joinChannel();
            } else {
                leaveChannel();
            }
        });
        
        return view;
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(requireContext(), REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = requireContext();
            config.mAppId = "YOUR_AGORA_APP_ID";
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            requireActivity().runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined channel " + channel);
            requireActivity().runOnUiThread(() -> setupLocalVideo());
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            requireActivity().runOnUiThread(() -> remoteVideoContainer.removeAllViews());
        }
    };

    private void setupLocalVideo() {
        SurfaceView videoView = RtcEngine.CreateRendererView(requireContext());
        videoView.setZOrderMediaOverlay(true);
        localVideoContainer.addView(videoView);
        agoraEngine.setupLocalVideo(new VideoCanvas(videoView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        SurfaceView videoView = RtcEngine.CreateRendererView(requireContext());
        remoteVideoContainer.addView(videoView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(videoView, VideoCanvas.RENDER_MODE_FIT, uid));
    }

    private void joinChannel() {
        if (!checkSelfPermission()) {
            showMessage("Permissions not granted");
            return;
        }
        agoraEngine.joinChannel(null, "consultation_channel", null, 0);
    }

    private void leaveChannel() {
        if (!isJoined) return;
        agoraEngine.leaveChannel();
        isJoined = false;
        requireActivity().runOnUiThread(() -> {
            localVideoContainer.removeAllViews();
            remoteVideoContainer.removeAllViews();
        });
    }

    private void showMessage(String message) {
        requireActivity().runOnUiThread(() -> 
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        agoraEngine = null;
    }
}