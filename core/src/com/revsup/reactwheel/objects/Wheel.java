package com.revsup.reactwheel.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class Wheel extends ShapeRenderer {
    private Vector2 center;
    private float radius;
    private Vector2 arm;
    private boolean isClockwise;
    private float dir;
    private Vector2 target;
    private Vector2 prevTarget;
    private float targetRadius;
    private boolean gameRunning;
    private float angleSpeed;
    private boolean targetWasInRange;
    private Vector2 hitPoint;
    private float screenWidth = Gdx.graphics.getWidth();
    private float screenHeight = Gdx.graphics.getHeight();
    private BitmapFont scoreFont;
    private BitmapFont highScoreFont;
    private int score;
    //colors
    private static final Color BACKGROUND = new Color(38/255f, 50/255f, 56/255f, 1f);
    private static final Color RED = new Color(231/255f, 76/255f, 60/255f, 1f);
    private static final Color BLUE = new Color(52/255f, 152/255f, 219/255f, 1f);
    private static final Color WHITE = new Color(1f, 1f, 1f, 1f);

    //animation
    private SpriteBatch batch;
    private static final int FRAME_COLS = 8;
    private static final int FRAME_ROWS = 8;
    private Animation<TextureRegion> explosionAnimation;
    private Texture explosionSheet;

    private GlyphLayout layout;
    private float stateTime;


    private Sound explosionSound;
    private Sound victorySound;
    private Music music;
    private boolean targetHit;

    private Texture tapToPlay;
    private Texture highScore;

    public Wheel(SpriteBatch batch){

        center = new Vector2(screenWidth/2f, screenHeight-screenHeight/3f);
        radius = screenWidth/2.5f;

        arm = new Vector2(center.x+radius/1.1f, center.y);
        angleSpeed = 0.0f;


        isClockwise = false;
        dir = 1f;

        target = new Vector2(center.x, center.y+radius/1.3f);
        targetRadius = screenWidth/25f;

        hitPoint = new Vector2((arm.x*1.1f)/1.165f, arm.y);

        tapToPlay = new Texture("taptoplay.png");
        highScore = new Texture("highscore.png");
        this.batch = batch;
        targetHit = false;

        prevTarget = target;

        gameRunning = false;
        targetWasInRange = false;

        scoreFont = new BitmapFont();
        scoreFont.getRegion().getTexture().setFilter(

        Texture.TextureFilter.Linear,
        Texture.TextureFilter.Linear);
        scoreFont.getData().setScale(6f);

        highScoreFont = new BitmapFont();
        highScoreFont.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        highScoreFont.getData().setScale(6f);
        score = 0;
        SavedDataManager.getInstance().load();
layout = new GlyphLayout(scoreFont, String.valueOf(score));

    explosionSound = Gdx.audio.newSound(Gdx.files.internal("explode.wav"));
    victorySound = Gdx.audio.newSound(Gdx.files.internal("victory.mp3"));


    music = Gdx.audio.newMusic(Gdx.files.internal("That's What I Like.mp3"));
    music.setVolume(0.5f);
    music.play();
    music.setLooping(true);


        initAnimation();
    }

    public void initAnimation(){
        explosionSheet = new Texture("explosion.png");
        TextureRegion[][] tmp = TextureRegion.split(
                explosionSheet,
                explosionSheet.getWidth()/ FRAME_COLS,
                explosionSheet.getHeight()/ FRAME_ROWS
        );
        TextureRegion[] explosionFrames = new TextureRegion[FRAME_COLS*FRAME_ROWS];
        int index = 0;
        for(int i=0; i < FRAME_ROWS; i++){
            for(int j=0; j < FRAME_COLS; j++){
                explosionFrames[index++] = tmp[i][j];
            }
        }

        explosionAnimation = new Animation<TextureRegion>(0.0055f, explosionFrames);
        stateTime = 0.0f;
    }

    private void showExplosion(){
        TextureRegion currentFrame = explosionAnimation.getKeyFrame(stateTime, false);
        if(!explosionAnimation.isAnimationFinished(stateTime)){
            stateTime += Gdx.graphics.getDeltaTime();
            //TODO: change target to prevTarget
            batch.draw(currentFrame, (int)prevTarget.x-256, (int)prevTarget.y-256);
            if(explosionAnimation.isAnimationFinished(stateTime)){
                stateTime = 0.0f;https://www.linuxmint.com/start/sonya/
                targetHit = false;
            }

        }
    }

    public void render(){
        update();
        //draw shapes
        this.begin(ShapeType.Filled);

        // outer ring
        this.setColor(Color.PINK);
        this.circle(center.x, center.y, radius, 8);

        this.setColor(BACKGROUND);
        this.circle(center.x, center.y, radius/1.1f, 100);

        // arm
        setColor(RED);
        this.rectLine(center, arm, 25f);

        //center circle
        setColor(BLUE);
            circle(center.x, center.y, radius/1.6f, 12
            );

        //TODO: delete later
        setColor(WHITE);
        circle(hitPoint.x, hitPoint.y, 10f, 3);

        renderTarget();
        this.end();

        // draw sprites
        batch.begin();

        if(targetHit)
            showExplosion();

        scoreFont.draw(batch, layout, center.x - layout.width/2, center.y+layout.height/2);

        if(!gameRunning)
            batch.draw(tapToPlay, (screenWidth - tapToPlay.getWidth())/2, screenHeight/4);

        batch.draw(highScore, 20f, 50f);
        highScoreFont.draw(batch, String.valueOf(SavedDataManager.getInstance().getHighScore()),
                highScore.getWidth()+50, highScore.getHeight()*2);


        batch.end();



    }

    public void renderTarget() {
        setColor(BLUE);
        circle(target.x, target.y, targetRadius, 100);
        setColor(WHITE);
        circle(target.x, target.y, targetRadius/1.5f, 100);
        setColor(RED);
        circle(target.x, target.y, targetRadius/3.0f, 100);
    }

    public void checkInput(){
        boolean touched = Gdx.input.justTouched();

        if(distance(hitPoint, target) > 20.0 && distance(hitPoint, target) < 40.0)
            targetWasInRange = true;

        if(gameRunning){
            // target was successfully hit
            if(touched && targetInRange()){
                isClockwise = !isClockwise;
                targetWasInRange = false;

                Random rnd = new Random();
                float ang = 0.0f + rnd.nextFloat() * (360f - 0f);
                explosionSound.play();

                targetHit = true;
                prevTarget = new Vector2(target.x, target.y);

                score++;

                do{
                    target = rotate(target, ang);
                }while(distance(target, prevTarget) < 200);


            } else if(touched && !targetInRange()){
                stopGame();
            } else if(!touched && targetWasInRange && distance(hitPoint, target) > 100){
                stopGame();
            }
        } else{
            if(touched)
                startGame();
            else
                resetGame();

        }

    }

    public void resetGame(){
        arm.x = center.x+radius/1.1f;
        arm.y = center.y;
        hitPoint.x = arm.x/1.08f;
        hitPoint.y = arm.y;
        target.x = center.x;
        target.y = center.y+radius/1.3f;
        isClockwise = true;
        targetWasInRange = false;
    }

    public void startGame(){
        gameRunning = true;
        angleSpeed = 0.045f;
        score = 0;

    }

    public void stopGame(){
        gameRunning = false;
        angleSpeed = 0.0f;

        if(score > SavedDataManager.getInstance().getHighScore())
            victorySound.play();
        SavedDataManager.getInstance().setHighScore(score);
        SavedDataManager.getInstance().save();
    }

    private boolean targetInRange(){
        float hitRange = 30f;
        boolean inRange = (distance(hitPoint, target) <= hitRange);

        return inRange;
    }

    public Vector2 rotate(Vector2 p, float theta){
        float s = (float) Math.sin(theta);
        float c = (float) Math.cos(theta);

        p.x -= center.x;
        p.y -= center.y;

        float xNew = (p.x * c - dir*p.y * s);
        float yNew = (dir*p.x * s + p.y * c);

        p.x = (xNew + center.x);
        p.y = (yNew + center.y);

        return p;

    }

    public double distance(Vector2 p1, Vector2 p2){
        return Math.sqrt(Math.pow((p2.x - p1.x), 2) +
                Math.pow((p2.y - p1.y), 2));
    }


    public void update() {


        checkInput();
        layout.setText(scoreFont, String.valueOf(score));
        dir = (isClockwise) ? -1f : 1f;
        arm = rotate(arm, angleSpeed);
        hitPoint = rotate(hitPoint, angleSpeed);

    }
     public void dispose(){
         explosionSound.dispose();
         victorySound.dispose();
         music.dispose();
         this.dispose();
        }


    }


