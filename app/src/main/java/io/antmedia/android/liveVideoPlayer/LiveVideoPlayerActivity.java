/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.antmedia.android.liveVideoPlayer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.SQLOutput;
import java.util.UUID;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInList;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.login.SignUp;
import io.antmedia.android.liveVideoBroadcaster.LiveVideoBroadcasterActivity;


/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class LiveVideoPlayerActivity extends AppCompatActivity implements OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

  public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;
  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private Handler mainHandler;
  private EventLogger eventLogger;
  private SimpleExoPlayerView simpleExoPlayerView;
  private LinearLayout debugRootView;
  private TextView debugTextView;
  private Button retryButton;

  private Button play_live_video;

  private DataSource.Factory mediaDataSourceFactory;
  private SimpleExoPlayer player;
  private DefaultTrackSelector trackSelector;
  private DebugTextViewHelper debugViewHelper;
  private boolean needRetrySource;

  private boolean shouldAutoPlay;
  private int resumeWindow;
  private long resumePosition;
  private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
  protected String userAgent;
  private TextView videoNameEditText;
  private View videoStartControlLayout;

  //네티서버용 변수
  //네티 서버 사용시 기본적으로 필요한 변수 및 클래스 선언
  Handler handler;
  String data;
  SocketChannel socketChannel;
  //host, port
  //로컬
  //private static final String HOST = "192.168.137.1";
  //서버
  private static final String HOST = "ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com";
  private static final int PORT = 9000;
  // Activity lifecycle

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    shouldAutoPlay = true;
    clearResumePosition();
    mediaDataSourceFactory = buildDataSourceFactory(true);
    rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();
    mainHandler = new Handler();
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    setContentView(R.layout.activity_live_video_player);
    View rootView = findViewById(R.id.root);
    rootView.setOnClickListener(this);
    debugRootView = (LinearLayout) findViewById(R.id.controls_root);
    debugTextView = (TextView) findViewById(R.id.debug_text_view);
    retryButton = (Button) findViewById(R.id.retry_button);
    retryButton.setOnClickListener(this);

    videoNameEditText = findViewById(R.id.video_name_edit_text);
    videoStartControlLayout = findViewById(R.id.video_start_control_layout);
    //방제목
      Intent gIntent = getIntent();
    videoNameEditText.setText(gIntent.getStringExtra("liveName"));

    play_live_video = findViewById(R.id.play_live_video);

    simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
    simpleExoPlayerView.setControllerVisibilityListener(this);
    simpleExoPlayerView.requestFocus();
    play(videoStartControlLayout);

    //네티 접속
//네티 서버에 클라이언트를 접속 시킨다.
    handler = new Handler();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          socketChannel = SocketChannel.open();
          socketChannel.configureBlocking(true);
          socketChannel.connect(new InetSocketAddress(HOST, PORT));

        } catch (Exception ioe) {
          ioe.printStackTrace();

        }
        if(checkUpdate.getState() == checkUpdate.getState().NEW){
          checkUpdate.start();
        }
      }
    }).start();



  }

  @Override
  public void onNewIntent(Intent intent) {
    releasePlayer();
    shouldAutoPlay = true;
    clearResumePosition();
    setIntent(intent);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      play(null);
    } else {
      showToast(R.string.storage_permission_denied);
      finish();
    }
  }

  // Activity input

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Show the controls on any key event.
    simpleExoPlayerView.showController();
    // If the event was not handled then see if the player view can handle it as a media key event.
    return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
  }

  // OnClickListener methods

  @Override
  public void onClick(View view) {
    if (view == retryButton) {
      play(null);
    }
  }

  // PlaybackControlView.VisibilityListener implementation

  @Override
  public void onVisibilityChange(int visibility) {
    debugRootView.setVisibility(visibility);
  }

  // Internal methods

  private void initializePlayer(String rtmpUrl) {
    Intent intent = getIntent();
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {

      boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
      @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
              useExtensionRenderers()
                      ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
      TrackSelection.Factory videoTrackSelectionFactory =
              new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
      player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
              null, extensionRendererMode);
      //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
      //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
      //           null, extensionRendererMode);
      player.addListener(this);

      eventLogger = new EventLogger(trackSelector);
      player.addListener(eventLogger);
      player.setAudioDebugListener(eventLogger);
      player.setVideoDebugListener(eventLogger);
      player.setMetadataOutput(eventLogger);

      simpleExoPlayerView.setPlayer(player);
      player.setPlayWhenReady(shouldAutoPlay);
      debugViewHelper = new DebugTextViewHelper(player, debugTextView);
      debugViewHelper.start();
    }
    if (needNewPlayer || needRetrySource) {
      //  String action = intent.getAction();
      Uri[] uris;
      String[] extensions;

      uris = new Uri[1];
      uris[0] = Uri.parse(rtmpUrl);
      extensions = new String[1];
      extensions[0] = "";
      if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
        // The player will be reinitialized if the permission is granted.
        return;
      }
      MediaSource[] mediaSources = new MediaSource[uris.length];
      for (int i = 0; i < uris.length; i++) {
        mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
      }
      MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
              : new ConcatenatingMediaSource(mediaSources);
      boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(resumeWindow, resumePosition);
      }
      player.prepare(mediaSource, !haveResumePosition, false);
      needRetrySource = false;
    }
  }

  private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
    int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
            : Util.inferContentType("." + overrideExtension);
    switch (type) {
      case C.TYPE_SS:
        return new SsMediaSource(uri, buildDataSourceFactory(false),
                new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_DASH:
        return new DashMediaSource(uri, buildDataSourceFactory(false),
                new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_HLS:
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
      case C.TYPE_OTHER:
        if (uri.getScheme().equals("rtmp")) {
          return new ExtractorMediaSource(uri, rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV(),
                  mainHandler, eventLogger);
        }
        else {
          return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                  mainHandler, eventLogger);
        }
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }


  private void releasePlayer() {
    if (player != null) {
      debugViewHelper.stop();
      debugViewHelper = null;
      shouldAutoPlay = player.getPlayWhenReady();
      updateResumePosition();
      player.release();
      player = null;
      trackSelector = null;
      //trackSelectionHelper = null;
      eventLogger = null;
    }
  }

  private void updateResumePosition() {
    resumeWindow = player.getCurrentWindowIndex();
    resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
            : C.TIME_UNSET;
  }

  private void clearResumePosition() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  /**
   * Returns a new DataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new DataSource factory.
   */
  private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
    return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  /**
   * Returns a new HttpDataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new HttpDataSource factory.
   */
  private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
    return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  // ExoPlayer.EventListener implementation

  @Override
  public void onLoadingChanged(boolean isLoading) {
    // Do nothing.
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      showControls();
    }
  }

  @Override
  public void onPositionDiscontinuity() {
    if (needRetrySource) {
      // This will only occur if the user has performed a seek whilst in the error state. Update the
      // resume position so that if the user then retries, playback will resume from the position to
      // which they seeked.
      updateResumePosition();
    }
  }

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest) {
    // Do nothing.
  }

  @Override
  public void onPlayerError(ExoPlaybackException e) {
    videoStartControlLayout.setVisibility(View.VISIBLE);
    String errorString = null;
    if (e.type == ExoPlaybackException.TYPE_RENDERER) {
      Exception cause = e.getRendererException();
      if (cause instanceof DecoderInitializationException) {
        // Special case for decoder initialization failures.
        DecoderInitializationException decoderInitializationException =
                (DecoderInitializationException) cause;
        if (decoderInitializationException.decoderName == null) {
          if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
            errorString = getString(R.string.error_querying_decoders);
          } else if (decoderInitializationException.secureDecoderRequired) {
            errorString = getString(R.string.error_no_secure_decoder,
                    decoderInitializationException.mimeType);
          } else {
            errorString = getString(R.string.error_no_decoder,
                    decoderInitializationException.mimeType);
          }
        } else {
          errorString = getString(R.string.error_instantiating_decoder,
                  decoderInitializationException.decoderName);
        }
      }
    }
    if (errorString != null) {
      showToast(errorString);
    }
    needRetrySource = true;
    if (isBehindLiveWindow(e)) {
      clearResumePosition();
      play(null);
    } else {
      updateResumePosition();
      showControls();
    }
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
    if (mappedTrackInfo != null) {
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_video);
      }
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_audio);
      }
    }
  }

  private void showControls() {
    debugRootView.setVisibility(View.VISIBLE);
  }

  private void showToast(int messageId) {
    showToast(getString(messageId));
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }

  private static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }


  public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(this, bandwidthMeter,
            buildHttpDataSourceFactory(bandwidthMeter));
  }

  public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
  }

  public boolean useExtensionRenderers() {
    return BuildConfig.FLAVOR.equals("withExtensions");
  }

  public void play(View view) {
    Intent intent = getIntent();
    String URL = "rtmp://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/live/" + intent.getStringExtra("liveId");
    //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";
    initializePlayer(URL);
  }

  //네티 서버 전송 클래스
  //개별 클래스 만들기
  private class SendPriceTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {
      try {
        socketChannel
                .socket()
                .getOutputStream()
                .write(strings[0].getBytes("UTF-8")); // 서버로
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
    //내 클라이언트
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          //전송 버튼 결과 코딩

        }
      });
    }
  }

  void receive() {
    while (true) {
      try {
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        //서버가 비정상적으로 종료했을 경우 IOException 발생
        int readByteCount = socketChannel.read(byteBuffer); //데이터받기
        Log.d("readByteCount", readByteCount + "");
        //서버가 정상적으로 Socket의 close()를 호출했을 경우
        if (readByteCount == -1) {
          throw new IOException();
        }

        byteBuffer.flip(); // 문자열로 변환
        Charset charset = Charset.forName("UTF-8");
        data = charset.decode(byteBuffer).toString();
        Log.d("receive", "msg :" + data);
        handler.post(showUpdate);
      } catch (IOException e) {
        Log.d("getMsg", e.getMessage() + "");
        try {
          socketChannel.close();
          break;
        } catch (IOException ee) {
          ee.printStackTrace();
        }
      }
    }
  }

  private Thread checkUpdate = new Thread() {

    public void run() {
      try {
        String line;
        receive();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };
  //tcp 서버에서 상대방 디바이스로 보냈을 때
  private Runnable showUpdate = new Runnable() {

    public void run() {
      String receive = data;
      String type = receive.split("//")[0];

      PhpConnect phpConnect = new PhpConnect();
      if(type.equals("liveEnd")){

        if(receive.split("//")[2].equals(new SessionManager(getApplicationContext()).getUserId())){
          Intent intent = new Intent(getApplicationContext(),ChatInList.class);
          intent.putExtra("userId",receive.split("//")[1]);
          String fileName1 = "chat_room_insert.php";
          String param1 = "userId="+receive.split("//")[1]+"&userId2="+new SessionManager(getApplicationContext()).getUserId();

          try {
            String chatRoomIdResult = phpConnect.execute(fileName1, param1).get();
            intent.putExtra("chatroomId",chatRoomIdResult);
            startActivity(intent);
          }catch (Exception e){
            System.out.println(e);
          }
        }else{
          Toast.makeText(getApplicationContext(),"경매가 종료 되었습니다.",Toast.LENGTH_LONG).show();
          finish();
        }

      }

    }

  };

}


