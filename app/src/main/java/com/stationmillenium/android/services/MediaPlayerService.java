/**
 * 
 */
package com.stationmillenium.android.services;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.activities.PlayerActivity.PlayerState;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.intents.LocalIntents;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

/**
 * Service to play audio stream
 * @see http://developer.android.com/guide/components/services.html
 * @author vincent
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MediaPlayerService extends Service implements OnAudioFocusChangeListener, OnPreparedListener, OnErrorListener, OnInfoListener {

	/**
	 * Class to receive intent about the presses playback control buttons
	 * @author vincent
	 *
	 */
	public static class PlaybackControlButtonsBroadcastReceiver extends BroadcastReceiver {

		private static final String TAG = "PlaybackControlButtonsBroadcastReceiver";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Received intent about pressed control buttons");
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Play/pause button pressed");
					Intent playPauseIntent = new Intent(LocalIntents.PLAYER_PLAY_PAUSE.toString());
					context.startService(playPauseIntent);
					break;
				
				case KeyEvent.KEYCODE_MEDIA_PLAY:
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Play button pressed");
					Intent playIntent = new Intent(LocalIntents.PLAYER_PLAY.toString());
					context.startService(playIntent);
					break;
	
				
				case KeyEvent.KEYCODE_MEDIA_PAUSE:
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Pause button pressed");
					Intent pauseIntent = new Intent(LocalIntents.PLAYER_PAUSE.toString());
					context.startService(pauseIntent);
					break;
					
				case KeyEvent.KEYCODE_MEDIA_STOP:
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Stop button pressed");
					Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
					context.startService(stopIntent);
					break;
				}
			}
		}

	}
	
	/**
	 * Class to receive intent about audio becomming noisy
	 * @author vincent
	 *
	 */
	private class AudioBecommingNoisyBroadcastReceiver extends BroadcastReceiver {
		
		private static final String TAG = "AudioBecommingNoisyBroadcastReceiver";
		
		private boolean registered;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Received intent that audio becomes noisy");
			stopMediaPlayer();
		}

		/**
		 * @return the registered
		 */
		public boolean isRegistered() {
			return registered;
		}

		/**
		 * @param registered the registered to set
		 */
		public void setRegistered(boolean registered) {
			this.registered = registered;
		}
		
	}
	
	/**
	 * Class to update the current playing title through intent
	 * @author vincent
	 *
	 */
	private class UpdateCurrentTitleBroadcastReceiver extends BroadcastReceiver {
		
		/**
		 * Async loader for title image
		 * @author vincent
		 *
		 */
		private class AsyncImageLoader extends AsyncTask<File, Void, Bitmap> {

			private CurrentTitleDTO song;
			
			/**
			 * Create a new {@link AsyncImageLoader} with the song to update params
			 * @param song the {@link CurrentTitleDTO}
			 */
			public AsyncImageLoader(CurrentTitleDTO song) {
				this.song = song;
			}
			
			@Override
			protected Bitmap doInBackground(File... params) {
				if ((params.length >= 1) && (params[0] != null))
					return BitmapFactory.decodeFile(params[0].getAbsolutePath());
				else
					return null;
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				synchronized (currentSongImageLock) { //save image
					currentSongImage = result;
				}

				//update the notification
				boolean notificationUpdateNeeded = (((currentSong != null) 
							&& (song != null) 
							&& (!currentSong.getCurrentSong().equals(song.getCurrentSong()))) 
						|| ((currentSong == null) && (song != null))); 
				currentSong = song;
				if (notificationUpdateNeeded) {
					Notification notification = createNotification(playerState == PlayerState.PLAYING);
					((NotificationManager) MediaPlayerService.this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
				}
				
				//set metadata for remote control
				if ((Utils.isAPILevel14Available()) && (remoteControlClient != null)) {
					MetadataEditor metadataEditor = remoteControlClient.editMetadata(false);
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getCurrentSong().getArtist());
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getCurrentSong().getArtist());
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getCurrentSong().getTitle());
					if (result != null)
						metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, result);
					else
						metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, BitmapFactory.decodeResource(getResources(), R.drawable.player_default_image));
					metadataEditor.apply();
				}
			}
			
		}
		
		private static final String TAG = "UpdateCurrentTitleBroadcastReceiver";
		
		private boolean registered;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Update the current playing title");
			CurrentTitleDTO songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());
			if (songData != null)
				new AsyncImageLoader(songData).execute(songData.getCurrentSong().getImage());
			else
				Log.w(TAG, "Current title DTO was null !");
		}

		/**
		 * @return the registered
		 */
		public boolean isRegistered() {
			return registered;
		}

		/**
		 * @param registered the registered to set
		 */
		public void setRegistered(boolean registered) {
			this.registered = registered;
		}
		
	}
	
	/**
	 * Class to get the current position of the media player, wrapper through a weak reference
	 * Static access needed because {@link WeakReference} and {@link MediaPlayer} not serializable
	 * @author vincent
	 *
	 */
	public static class MediaPlayerCurrentPositionGrabber {
		
		private static final String TAG = "CurrentMediaPlayerTimeGrabber";
		
		private static WeakReference<MediaPlayer> mediaPlayerRef;
		
		/**
		 * Create a new {@link MediaPlayerCurrentPositionGrabber}
		 * @param mediaPlayer the {@link MediaPlayer} to make ref
		 */
		public static void setMediaPlayerReference(MediaPlayer mediaPlayer) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Set the media player reference");
			mediaPlayerRef = new WeakReference<MediaPlayer>(mediaPlayer);
		}
		
		/**
		 * Get the {@link MediaPlayer} current position
		 * @see MediaPlayer#getCurrentPosition()
		 * @return the current position or 0 if not available
		 */
		public static int getMediaPlayerCurrentPosition() {
			if ((mediaPlayerRef != null) && (mediaPlayerRef.get() != null)) {
				try {
					return mediaPlayerRef.get().getCurrentPosition();
				} catch (IllegalStateException e) {
					Log.w(TAG, "Error while getting media player current position", e);
					return 0;
				}
			} else
				return 0;
		}

	}
	
	//class constants
	private static final String TAG = "MediaPlayerService";
	private static final String UPDATE_CURRENT_TITLE_TIMER_NAME = "UpdateCurrentTitleTimer";
	private static final int NOTIFICATION_ID = 1;
	private static final int UPDATE_TITLE_START_TIME = 500;
	private static final int UPDATE_TITLE_PERIOD_TIME = 10000;
	
	//instance vars
	//service internal vars
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	//broadcast receivers
	private ComponentName pcbbrComponentName;
	private AudioBecommingNoisyBroadcastReceiver abnbr = new AudioBecommingNoisyBroadcastReceiver();
	private UpdateCurrentTitleBroadcastReceiver uctbr = new UpdateCurrentTitleBroadcastReceiver();
	
	//intents and lock for update
	private Timer updateCurrentTitleTimer;
	private WifiLock wifiLock;
	private boolean playerActivityResumed;
	
	//vars to manage stream
	private AudioManager audioManager;
	private MediaPlayer mediaPlayer;
	private int originalVolume;
	private PlayerState playerState;
	private RemoteControlClient remoteControlClient;
	
	//stream metadata
	private Object currentSongImageLock = new Object();
	private Bitmap currentSongImage;
	private CurrentTitleDTO currentSong;
	
	/**
	 * Service handler to run in another thread
	 * @author vincent
	 *
	 */
	private static class ServiceHandler extends Handler {
		
		//rerefences to service
		//see : http://stackoverflow.com/questions/12084382/what-is-handlerleak
		private WeakReference<MediaPlayerService> mediaPlayerServiceRef;
		
		public ServiceHandler(Looper looper, MediaPlayerService service) {
			super(looper);
			mediaPlayerServiceRef = new WeakReference<MediaPlayerService>(service);
		}
		
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public void handleMessage(Message msg) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Start the MediaPlayerService");
			if (mediaPlayerServiceRef.get() != null) {
				try {
					mediaPlayerServiceRef.get().audioManager = (AudioManager) mediaPlayerServiceRef.get().getSystemService(Context.AUDIO_SERVICE);
					int result = mediaPlayerServiceRef.get().audioManager.requestAudioFocus(mediaPlayerServiceRef.get(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		
					if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						if (BuildConfig.DEBUG)
							Log.d(TAG, "Audio focus request granted - start the stream");
		
						if (Utils.isNetworkAvailable(mediaPlayerServiceRef.get().getApplicationContext())) { //check if network is up
							try {
								//init player
								mediaPlayerServiceRef.get().initMediaPlayer();
								
								//add handlers
								mediaPlayerServiceRef.get().audioManager.registerMediaButtonEventReceiver(mediaPlayerServiceRef.get().pcbbrComponentName);
								mediaPlayerServiceRef.get().registerReceiver(mediaPlayerServiceRef.get().abnbr, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
								mediaPlayerServiceRef.get().abnbr.setRegistered(true);
								LocalBroadcastManager.getInstance(mediaPlayerServiceRef.get()).registerReceiver(mediaPlayerServiceRef.get().uctbr, new IntentFilter(LocalIntents.CURRENT_TITLE_UPDATED.toString()));
								mediaPlayerServiceRef.get().uctbr.setRegistered(true);
								if (msg.arg2 == 1) { //use volume manager from shared preferences
									if (BuildConfig.DEBUG)
										Log.d(TAG, "Use volume from shared preferences");
									int volumeValue = PreferenceManager.getDefaultSharedPreferences(mediaPlayerServiceRef.get())
											.getInt(AlarmSharedPreferencesConstants.ALARM_VOLUME, mediaPlayerServiceRef.get().audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
									mediaPlayerServiceRef.get().audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeValue, 0);
								}									
								mediaPlayerServiceRef.get().originalVolume = mediaPlayerServiceRef.get().audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
								//remote control if api level 14
								if (Utils.isAPILevel14Available()) {
									//init the remote control client
									Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
									mediaButtonIntent.setComponent(mediaPlayerServiceRef.get().pcbbrComponentName);
									PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(mediaPlayerServiceRef.get().getApplicationContext(), 0, mediaButtonIntent, 0);
									mediaPlayerServiceRef.get().remoteControlClient = new RemoteControlClient(mediaPendingIntent);
									mediaPlayerServiceRef.get().remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY 
											| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
											| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE 
											| RemoteControlClient.FLAG_KEY_MEDIA_STOP);
									mediaPlayerServiceRef.get().audioManager.registerRemoteControlClient(mediaPlayerServiceRef.get().remoteControlClient);
								}
								
								//wifi lock
								mediaPlayerServiceRef.get().wifiLock = ((WifiManager) mediaPlayerServiceRef.get().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "StationMilleniumPlayerWifiLock");
								
								//start in foreground
								Notification notification = mediaPlayerServiceRef.get().createNotification(true);
								mediaPlayerServiceRef.get().startForeground(NOTIFICATION_ID, notification);
		
							} catch (Exception e) {
								Log.w(TAG, "Error while trying to init media player", e);
								Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();
		
								//stop the service
								mediaPlayerServiceRef.get().audioManager.abandonAudioFocus(mediaPlayerServiceRef.get());
								mediaPlayerServiceRef.get().stopSelf();
							} 
		
						} else {
							Log.w(TAG, "No Internet connection - can't play stream");
							Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_network_unavailable), Toast.LENGTH_SHORT).show();
		
							//stop the service
							mediaPlayerServiceRef.get().audioManager.abandonAudioFocus(mediaPlayerServiceRef.get());
							mediaPlayerServiceRef.get().stopSelf();
						}
		
					} else {
						Log.w(TAG, "Audio focus request failed - can't play stream");
						Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();
		
						//stop the service
						mediaPlayerServiceRef.get().audioManager.abandonAudioFocus(mediaPlayerServiceRef.get());
						mediaPlayerServiceRef.get().stopSelf();
					}
				
				} catch (Exception npe) {
					Log.e(TAG, "Exception in MediaPlayerService init", npe);
				}
				
			} else
				Log.e(TAG, "Reference to MediaPlayerService is null ! Nothing can be done");
		}
		
	}
	
	/**
	 * Create a notification
	 * @param pauseAction <code>true</code> to add pause action in {@link Notification}, <code>false</code> to add play action 
	 * @return the {@link Notification}
	 */
	private Notification createNotification(boolean pauseAction) {
		//create intent for notification
		Intent playerIntent = new Intent(MediaPlayerService.this, PlayerActivity.class); 
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(MediaPlayerService.this);
		stackBuilder.addParentStack(PlayerActivity.class);
		stackBuilder.addNextIntent(playerIntent);						
		PendingIntent playerPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		//manage state text
		String stateText;
		switch (playerState) {
		case BUFFERING:
			stateText = getResources().getString(R.string.player_notification_loading);
			break;

		case PLAYING:
			stateText = getResources().getString(R.string.player_notification_play);
			break;
			
		case PAUSED:
			stateText = getResources().getString(R.string.player_notification_pause);
			break;
			
		default:
			stateText = "";
			break;
		}
		
		//set the current title
		String currentTitle = ((currentSong == null) || (currentSong.getCurrentSong().getArtist() == null) || (currentSong.getCurrentSong().getTitle() == null)) 
				? getResources().getString(R.string.player_current_title_unavailable_notification)
				: getResources().getString(R.string.player_current_title_notification, currentSong.getCurrentSong().getArtist(), currentSong.getCurrentSong().getTitle());
				
		//set the player image
		Bitmap playerImage = null;
		synchronized (currentSongImageLock) {
			playerImage = (currentSongImage == null) ? BitmapFactory.decodeResource(getResources(), R.drawable.player_default_image) : currentSongImage;
		}
		
		//create notification
		Builder notificationBuilder = new NotificationCompat.Builder(MediaPlayerService.this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setLargeIcon(playerImage)
			.setTicker(getResources().getString(R.string.notification_ticker_text))
			.setContentTitle(getResources().getString(R.string.app_name))
			.setContentText(currentTitle)
			.setStyle(new BigPictureStyle()
				.bigPicture(playerImage)
				.setSummaryText(currentTitle))
			.setContentInfo(stateText)
			.setContentIntent(playerPendingIntent);
		
		//add proper action (pause or play)
		//pending intent for pausing or playing player
		Intent pausePlayIntent = new Intent(((pauseAction) ? LocalIntents.PLAYER_PAUSE : LocalIntents.PLAYER_PLAY).toString());
		PendingIntent pausePlayPendingIntent = PendingIntent.getService(this, 0, pausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);		
		notificationBuilder.addAction((pauseAction) ? R.drawable.ic_player_pause : R.drawable.ic_player_play,
				getResources().getString((pauseAction) ? R.string.player_pause : R.string.player_play), 
				pausePlayPendingIntent);
				
		//pending intent for stopping player
		Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
		PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.addAction(R.drawable.ic_player_stop, getResources().getString(R.string.player_stop), stopPendingIntent);
		
		return notificationBuilder.build();
	}

	/**
	 * Create an {@link Intent} to open the {@link PlayerActivity} with data
	 * @return the {@link Intent}
	 */
	private Intent createPlayerActivityIntent() {
		Intent playerIntent = new Intent(MediaPlayerService.this, PlayerActivity.class); 
		playerIntent.setAction(LocalIntents.ON_PLAYER_OPEN.toString());
		playerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		playerIntent.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), currentSong);
		playerIntent.putExtra(LocalIntentsData.CURRENT_STATE.toString(), playerState);
		return playerIntent;
	}

	@Override
	public void onCreate() {
		pcbbrComponentName = new ComponentName(this, PlaybackControlButtonsBroadcastReceiver.class);
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler 
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper, this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//process specific intent
			if (LocalIntents.PLAYER_PAUSE.toString().equals(intent.getAction())) 
				pauseMediaPlayer(getApplicationContext());
			else if (LocalIntents.PLAYER_PLAY.toString().equals(intent.getAction()))
				playMediaPlayer(getApplicationContext());
			else if (LocalIntents.PLAYER_PLAY_PAUSE.toString().equals(intent.getAction())) {
				if (mediaPlayer != null) {
					if (mediaPlayer.isPlaying())
						pauseMediaPlayer(getApplicationContext());
					else
						playMediaPlayer(getApplicationContext());
				}
			} else if (LocalIntents.PLAYER_STOP.toString().equals(intent.getAction()))
				stopMediaPlayer();
			else if (LocalIntents.PLAYER_OPEN.toString().equals(intent.getAction())) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Open the player with data");
				playerActivityResumed = true; //player resumed at same time
				Intent playerIntent = createPlayerActivityIntent();
				startActivity(playerIntent);
				
			} else if (LocalIntents.PLAYER_ACTIVITY_PAUSE.toString().equals(intent.getAction())) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Player activity paused - don't send intents");
				playerActivityResumed = false;
				
			} else if (LocalIntents.PLAYER_ACTIVITY_RESUME.toString().equals(intent.getAction())) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Player activity resumed - send intents");
				playerActivityResumed = true;
				
			} else {
			    // For each start request, send a message to start a job and deliver the
			    // start ID so we know which request we're stopping when we finish the job
			    Message msg = serviceHandler.obtainMessage();
			    msg.arg1 = startId;
			    if (intent.getBooleanExtra(LocalIntentsData.GET_VOLUME_FROM_PREFERENCES.toString(), false))
			    	msg.arg2 = 1;
			    else 
			    	msg.arg2 = 0;
			    serviceHandler.sendMessage(msg);
			    
			    //if start media player is required, activity is resumed
			    playerActivityResumed = intent.getBooleanExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), true);
			}
		}
	    
	    // If we get killed, after returning from here, restart
	    return START_STICKY;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Audio focus changed - process it...");
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Audio focus gain - start playing");
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
			playMediaPlayer(this);
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Audio focus loss - stop playing");
			stopMediaPlayer();
			break;
			
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Audio focus loss transient - pause playing");
			pauseMediaPlayer(this);
			break;
			
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Audio focus loss transient with duck - duck volume");
			if ((mediaPlayer != null) && (mediaPlayer.isPlaying())) {
				originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); //save the original volume value for volume restore
				int duckVolume = (int) (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.2); //duck volume is 20% of max volume
				if (originalVolume > duckVolume) //if original volume is lower than duck volume, no need to duck (duck volume would be higher than dyck volume)
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, duckVolume, 0);
				else if (BuildConfig.DEBUG)
					Log.d(TAG, "Original volume lower than duck volume - no need to duck");
			}
			break;
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Player is ready - let's start");
		mp.start();
		Toast.makeText(this, getResources().getString(R.string.player_play_toast), Toast.LENGTH_SHORT).show();
		
		//send state intent
		sendStateIntent(PlayerState.PLAYING);
				
		//wifi lock
		if ((wifiLock != null) && (!wifiLock.isHeld()))
			wifiLock.acquire();
		
		//player current time update start
		setupCurrentTitlePlayerServiceTimer();
	}

	/**
	 * Setup the {@link CurrentTitlePlayerService} timer
	 */
	private void setupCurrentTitlePlayerServiceTimer() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Register current title service timer");
		updateCurrentTitleTimer = new Timer(UPDATE_CURRENT_TITLE_TIMER_NAME);
		updateCurrentTitleTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Intent currentTitleServiceIntent = new Intent(MediaPlayerService.this, CurrentTitlePlayerService.class);
				MediaPlayerService.this.startService(currentTitleServiceIntent);
			}
			
		}, UPDATE_TITLE_START_TIME, UPDATE_TITLE_PERIOD_TIME);
	}
	
	/**
	 * Play the media player, if not already playing
	 * @param context the {@link Context} to update notification
	 */
	private void playMediaPlayer(Context context) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Play media player");
		if ((mediaPlayer != null) && (!mediaPlayer.isPlaying()))
			mediaPlayer.start();
		
		//send state intent
		sendStateIntent(PlayerState.PLAYING);
				
		//update notification
		Notification notification = createNotification(true);
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
		Toast.makeText(this, getResources().getString(R.string.player_play_toast), Toast.LENGTH_SHORT).show();
		
		setupCurrentTitlePlayerServiceTimer();
	}
	
	/**
	 * Pause the media player, if playing
	 * @param context the {@link Context} to update notification
	 */
	private void pauseMediaPlayer(Context context) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Pause media player");
		if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
			mediaPlayer.pause();
		
		//send state intent
		sendStateIntent(PlayerState.PAUSED);
				
		//update notification
		Notification notification = createNotification(false);
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
		Toast.makeText(this, getResources().getString(R.string.player_pause_toast), Toast.LENGTH_SHORT).show();
		
		//stop current title update
		cancelCurrentTitleTimerServiceTimer();
	}
	
	/**
	 * Stop the player and stop service
	 */
	private void stopMediaPlayer() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Stop the media player");
		if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
			mediaPlayer.stop();
		Toast.makeText(this, getResources().getString(R.string.player_stop_toast), Toast.LENGTH_SHORT).show();
				
		//send state intent
		sendStateIntent(PlayerState.STOPPED);
		
		//stop current title update
		cancelCurrentTitleTimerServiceTimer();
				
		//stop service
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Stop service");
		stopForeground(true);
		stopSelf();
	}
	
	/**
	 * Send the state intent
	 * @param state the {@link PlayerState}
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void sendStateIntent(PlayerState state) {
		//send the state
		playerState = state;
		if (playerActivityResumed) { //send intents only if activity is resumed
			Intent stateIntent = new Intent(this, PlayerActivity.class);
			stateIntent.setAction(state.getAssociatedIntent().toString());
			stateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(stateIntent);
		}

		//send local broadcast intent for widget
		Intent widgetIntent = new Intent(state.getAssociatedIntent().toString());
		sendBroadcast(widgetIntent);
		
		//adjust the remote control state
		if ((Utils.isAPILevel14Available()) && (remoteControlClient != null)) {
			switch (playerState) {
			case BUFFERING:
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_BUFFERING);
				break;

			case PAUSED:
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
				break;
				
			case PLAYING:
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
				break;
				
			case STOPPED:
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
				break;
			}
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "Media player error occured");
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Log.e(TAG, "Unknown media player error - stopping media player");
			stopMediaPlayer();
			break;

		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Log.w(TAG, "Media player server died error - restarting");
			if (mediaPlayer != null)
				mediaPlayer.release();
			mediaPlayer = null;
			
			try { //reinit media player
				initMediaPlayer();
			} catch (IOException e) {
				Log.w(TAG, "Error while trying to init media player", e);
				Toast.makeText(MediaPlayerService.this, getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();

				//stop the service
				if (mediaPlayer != null)
					mediaPlayer.stop();
				
				//stop service
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Stop service");
				stopForeground(true);
				stopSelf();
			}

		}
		
		return true; //error handled
	}

	@Override
	public IBinder onBind(Intent intent) {
		//no binding service - return null
		return null;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Destroying service");
		//release media player
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		
		//player current time and title update stop
		cancelCurrentTitleTimerServiceTimer();
				
		//free resources
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Free handlers");
		if (audioManager != null) {
			if (Utils.isAPILevel14Available()) 
				audioManager.unregisterRemoteControlClient(remoteControlClient);
			
			audioManager.abandonAudioFocus(this);
			audioManager.unregisterMediaButtonEventReceiver(pcbbrComponentName);
		}
		if (abnbr.isRegistered()) {
			unregisterReceiver(abnbr);
			abnbr.setRegistered(false);
		}
		if (uctbr.isRegistered()) {
			LocalBroadcastManager.getInstance(MediaPlayerService.this).unregisterReceiver(uctbr);
			uctbr.setRegistered(false);
		}
		
		//wifi lock
		if ((wifiLock != null) && (wifiLock.isHeld()))
			wifiLock.release();
				
		super.onDestroy();
	}

	/**
	 * Cancel {@link CurrentTitlePlayerService} timer
	 */
	private void cancelCurrentTitleTimerServiceTimer() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Cancel current title timer");
		if (updateCurrentTitleTimer != null)
			updateCurrentTitleTimer.cancel();
	}

	/**
	 * Init the media player
	 * @throws IOException if any error occurs
	 */
	private void initMediaPlayer() throws IOException {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Init media player");
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setDataSource(getResources().getString(R.string.player_stream_url));
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.prepareAsync();
		
		//set reference
		MediaPlayerCurrentPositionGrabber.setMediaPlayerReference(mediaPlayer);
		
		//send state intent
		sendStateIntent(PlayerState.BUFFERING);
		Toast.makeText(this, getResources().getString(R.string.player_loading_toast), Toast.LENGTH_SHORT).show();
		
		//send tracking info
		Intent statsTrackerServiceIntent = new Intent(this, StatsTrackerService.class);
		startService(statsTrackerServiceIntent);
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Media player start buffering...");
			sendStateIntent(PlayerState.BUFFERING);
			return true;
		} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Media player end buffering...");
			sendStateIntent(PlayerState.PLAYING);
			return true;
		}
			
		return false;
	}

}
