package dev.sebastian.companion;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.speech.tts.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;

public final class MainActivity extends Activity {
    private static final int IMPORT_STATUS = 42;
    private final int bg=0xFF111015, card=0xFF1D1924, white=0xFFF7F1FF, muted=0xFFB7ABCA, purple=0xFFC4A0FF;
    private LinearLayout content;
    private PetView pet;
    private TextView stateLabel, sourceLabel;
    private SharedPreferences prefs;
    private SnapshotStore store;
    private CompanionState preview;
    private TextToSpeech tts;
    private boolean speechReady;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() { public void run() { refresh(); handler.postDelayed(this, 10000); } };

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs=getSharedPreferences("companion", MODE_PRIVATE); store=new SnapshotStore(this);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(bg);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24),dp(16),dp(24),dp(28));
        scroll.addView(content); setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets system=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(system.left,system.top,system.right,system.bottom); return insets;
        });
        label(content,"YOUR LITTLE COMPANION",11,purple,Typeface.BOLD,12);
        label(content,"Sebastian",34,white,Typeface.BOLD,4);
        label(content,"A familiar presence. One tap from a conversation.",15,muted,Typeface.NORMAL,22);

        LinearLayout stage=panel(); stage.setGravity(Gravity.CENTER); stage.setPadding(dp(12),dp(8),dp(12),dp(20));
        TextView badge=label(stage,"CHARACTER PREVIEW",10,purple,Typeface.BOLD,0); badge.setGravity(Gravity.CENTER);
        pet=new PetView(this); stage.addView(pet,new LinearLayout.LayoutParams(-1,dp(230)));
        pet.setOnClickListener(v->openChat()); pet.setFocusable(true);
        stateLabel=label(stage,"Sebastian · Idle",20,white,Typeface.BOLD,5); stateLabel.setGravity(Gravity.CENTER);
        sourceLabel=label(stage,"Not connected to ChatGPT",12,muted,Typeface.NORMAL,0); sourceLabel.setGravity(Gravity.CENTER);
        content.addView(stage); gap(16);

        Button open=button("Open ChatGPT  ↗",true); content.addView(open); open.setOnClickListener(v->openChat()); gap(20);
        label(content,"EXPRESSIONS",11,purple,Typeface.BOLD,8);
        label(content,"Preview the character’s animation states.",13,muted,Typeface.NORMAL,10);
        HorizontalScrollView states=new HorizontalScrollView(this); states.setHorizontalScrollBarEnabled(false);
        LinearLayout row=new LinearLayout(this);
        for(CompanionState s:CompanionState.values()) {
            Button b=button(s.label,false); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(48)); lp.setMarginEnd(dp(8)); row.addView(b,lp);
            b.setOnClickListener(v->{stopSpeech(); preview=s; refresh();});
        }
        states.addView(row); content.addView(states); gap(10);
        Button reset=button("Return to source status",false); content.addView(reset); reset.setOnClickListener(v->{stopSpeech(); preview=null; refresh();}); gap(20);

        LinearLayout home=panel(); label(home,"At home on your phone",18,white,Typeface.BOLD,8);
        label(home,"Add a small Sebastian widget to your home screen. It stays within its own space and opens ChatGPT when tapped.",14,muted,Typeface.NORMAL,12);
        Button pin=button("Add home widget",false); home.addView(pin); pin.setOnClickListener(v->pinWidget()); content.addView(home); gap(12);

        LinearLayout connection=panel(); label(connection,"Connection",18,white,Typeface.BOLD,8);
        label(connection,"Live Sebastian status is not connected. You can save a private ChatGPT conversation link or import a timestamped status snapshot.",14,muted,Typeface.NORMAL,12);
        addButton(connection,"Conversation link",this::editLink);
        addButton(connection,"Import status snapshot",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,IMPORT_STATUS);});
        addButton(connection,"Clear imported snapshot",()->{store.clear(); preview=null; refresh(); SebastianWidget.refresh(this);});
        content.addView(connection); gap(12);

        LinearLayout comfort=panel(); label(comfort,"Make yourself comfortable",18,white,Typeface.BOLD,10);
        Switch motion=new Switch(this); motion.setText("Reduced motion"); motion.setTextColor(white); motion.setChecked(prefs.getBoolean("reducedMotion",false));
        pet.setReducedMotion(motion.isChecked()); motion.setOnCheckedChangeListener((b,value)->{prefs.edit().putBoolean("reducedMotion",value).apply();pet.setReducedMotion(value);}); comfort.addView(motion); gapInside(comfort,8);
        Button speech=button("Hear a speech preview",false); comfort.addView(speech); speech.setOnClickListener(v->speakPreview());
        addButton(comfort,"Stop speech",()->{stopSpeech(); preview=null;refresh();});
        label(comfort,"Uses an installed offline Android voice, not the ChatGPT voice. No microphone or background listening.",12,muted,Typeface.NORMAL,0);
        content.addView(comfort); gap(20);
        label(content,"Independent prototype · original character artwork\nExact dot model, memory, voice and animations are not connected or included.",12,muted,Typeface.NORMAL,0);
        tts=new TextToSpeech(this,result->{
            speechReady=result==TextToSpeech.SUCCESS;
            if(speechReady) {
                Set<Voice> voices=tts.getVoices(); Voice offline=null;
                if(voices!=null) for(Voice voice:voices) if(!voice.isNetworkConnectionRequired() && "en".equals(voice.getLocale().getLanguage())) {offline=voice;break;}
                speechReady=offline!=null; if(speechReady) tts.setVoice(offline);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
                    public void onStart(String id) {}
                    public void onDone(String id) { runOnUiThread(()->{preview=null; refresh();}); }
                    public void onError(String id) { runOnUiThread(()->{preview=null; refresh(); toast("Speech preview unavailable.");}); }
                });
            }
        });
        refresh();
    }
    private void openChat(){stopSpeech();startActivity(new Intent(this,OpenChatActivity.class));}
    private void refresh() {
        if(pet==null)return;
        StatusSnapshot snapshot=store.latest(); boolean fresh=snapshot!=null && snapshot.isFresh(System.currentTimeMillis());
        CompanionState state=preview!=null ? preview : fresh ? snapshot.state : CompanionState.IDLE;
        pet.setState(state); stateLabel.setText("Sebastian · "+state.label);
        if(preview!=null) sourceLabel.setText("Animation preview · not live status");
        else if(snapshot==null) sourceLabel.setText("Not connected to ChatGPT");
        else sourceLabel.setText((fresh ? "Imported snapshot · " : "Snapshot expired · ")+DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(snapshot.updatedAt))+"\n"+snapshot.message);
    }
    private void editLink() {
        EditText field=new EditText(this); field.setSingleLine(true); field.setText(prefs.getString("conversation","")); field.setHint("https://chatgpt.com/c/…"); field.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(24),dp(8),dp(24),0);
        label(box,"Optional: paste your private conversation URL. Android may open it in ChatGPT or a browser. Exact dot routing is unverified. Stored only on this device.",14,muted,Typeface.NORMAL,12);box.addView(field);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Open your conversation").setView(box).setPositiveButton("Save",null).setNegativeButton("Cancel",null).setNeutralButton("Use app home",(d,w)->{prefs.edit().remove("conversation").apply();toast("Taps will open ChatGPT home.");}).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String value=field.getText().toString().trim();if(!ConversationLink.isAllowed(value)){field.setError("Use https://chatgpt.com/c/ followed by the conversation ID, without query or fragment.");return;} prefs.edit().putString("conversation",value).apply();dialog.dismiss();toast("Conversation link saved privately.");}));dialog.show();
    }
    private void pinWidget(){AppWidgetManager manager=AppWidgetManager.getInstance(this);if(manager.isRequestPinAppWidgetSupported()) manager.requestPinAppWidget(new ComponentName(this,SebastianWidget.class),null,null);else toast("Long-press your home screen, choose Widgets, then Sebastian.");}
    private void speakPreview(){
        if(!speechReady){toast("No offline English voice is available. Check Android text-to-speech settings.");return;}
        preview=CompanionState.SPEAKING;refresh();
        int result=tts.speak("At your service, sir. This is a speech and animation preview. Tap me to open ChatGPT.",TextToSpeech.QUEUE_FLUSH,null,"preview");
        if(result==TextToSpeech.ERROR){preview=null;refresh();toast("Speech preview unavailable.");}
    }
    private void stopSpeech(){if(tts!=null)tts.stop();}
    @Override protected void onResume(){super.onResume();if(pet!=null)pet.setActive(true);handler.post(ticker);SebastianWidget.refresh(this);}
    @Override protected void onPause(){handler.removeCallbacks(ticker);if(pet!=null)pet.setActive(false);stopSpeech();if(preview==CompanionState.SPEAKING)preview=null;super.onPause();}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=IMPORT_STATUS||result!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        try(InputStream input=getContentResolver().openInputStream(uri)){
            if(input==null)throw new IOException("No data");ByteArrayOutputStream buffer=new ByteArrayOutputStream();byte[] chunk=new byte[1024];int count;
            while((count=input.read(chunk))!=-1){buffer.write(chunk,0,count);if(buffer.size()>16384)throw new IOException("Too large");}byte[] bytes=buffer.toByteArray();
            store.importJson(new String(bytes,StandardCharsets.UTF_8)); preview=null;refresh();SebastianWidget.refresh(this);toast("Snapshot imported. This is a manual update.");
        }catch(Exception e){toast("Could not import. Choose a valid status JSON from the past five minutes (under 16 KB).");}
    }
    private LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(18),dp(18),dp(18),dp(18));p.setBackground(shape(card,24));return p;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xFF342B40);return d;}
    private TextView label(LinearLayout parent,String text,int size,int color,int style,int bottom){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans-serif",style));v.setLineSpacing(dp(2),1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(bottom);parent.addView(v,lp);return v;}
    private Button button(String text,boolean primary){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(primary?0xFF241331:white);b.setMinHeight(dp(50));b.setPadding(dp(16),dp(10),dp(16),dp(10));b.setBackground(shape(primary?purple:0xFF282131,16));b.setStateListAnimator(null);return b;}
    private void addButton(LinearLayout parent,String text,Runnable action){Button b=button(text,false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);parent.addView(b,lp);b.setOnClickListener(v->action.run());}
    private void gap(int height){gapInside(content,height);}
    private void gapInside(LinearLayout parent,int height){View v=new View(this);parent.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
}
