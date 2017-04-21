package mili.firstsensor;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlaySound extends AppCompatActivity {
    private static final String TAG = "PlaySoundActivity";
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int maxFreq = 500; // hz
    private final int max2Freq = 20000; // hz
    private final double duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = (int) duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final byte generatedSnd[] = new byte[2 * numSamples];

    private double freqOfTone = 262; // hz

    Handler handler = new Handler();

    @Override
    protected void onResume() {
        super.onResume();

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });
        thread.start();
    }

    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_sound);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int optimalFramesPerBuffer = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
        int optimalSampleRate = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        String speakerUltra = am.getProperty(AudioManager.PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND);
        Log.i(TAG, speakerUltra + "\n sample rate: " + optimalSampleRate + ", buffer size: " + optimalFramesPerBuffer);

        final TextView frequencyText = (TextView) findViewById(R.id.frequency_text);

        TextView max1Text = (TextView) findViewById(R.id.max1_text);
        max1Text.setText(maxFreq + " Hz");
        TextView max2Text = (TextView) findViewById(R.id.max2_text);
        max2Text.setText(max2Freq + " Hz");
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                freqOfTone = progressChanged*maxFreq/100;
                frequencyText.setText("Frequency: " + freqOfTone + " Hz");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                genTone();
                playSound();
            }
        });

        SeekBar seekBarLarger = (SeekBar) findViewById(R.id.seekBarLarger);
        seekBarLarger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                freqOfTone = progressChanged*max2Freq/100;
                frequencyText.setText("Frequency: " + freqOfTone + " Hz");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                genTone();
                playSound();
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // get selected radio button from radioGroup
                int selectedId = group.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton checkedButton = (RadioButton) findViewById(selectedId);

                String checkedString = checkedButton.getText().toString();

                // Do (262 Hz) Re (294 Hz) Mi (330 Hz) Fa (349 Hz) Sol (392 Hz) La (440 Hz) Si (494 Hz)
                if (checkedString.equals("do")) {
                    freqOfTone = 262;
                } else if (checkedString.equals("re")) {
                    freqOfTone = 294;
                } else if (checkedString.equals("mi")) {
                    freqOfTone = 330;
                } else if (checkedString.equals("fa")) {
                    freqOfTone = 349;
                } else if (checkedString.equals("so")) {
                    freqOfTone = 392;
                } else if (checkedString.equals("la")) {
                    freqOfTone = 440;
                } else if (checkedString.equals("ti")) {
                    freqOfTone = 494;
                }
//                Toast.makeText(PlaySound.this, Double.toString(freqOfTone)+ ": " + checkedString, Toast.LENGTH_SHORT).show();

                frequencyText.setText("Frequency: " + freqOfTone + " Hz");
                genTone();
                playSound();
            }
        });

    }
}
