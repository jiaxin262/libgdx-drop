package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {

    private final int VIEW_PORT_HEIGHT = 1080;
    private final int VIEW_PORT_WIDTH = 1920;
    private final int BUCKET_WIDTH = 120;
    private final int BUCKET_HEIGHT = 136;
    private final int DROPLET_WIDTH = 24;
    private final int DROPLET_HEIGHT = 46;
    private final float DROPLET_SPEED = 500f;
    private final long DROPLET_GENERATE_INTERVAL = 100000000;

    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Rectangle bucket;
    Vector3 touchPos = new Vector3();
    private Array<Rectangle> raindrops;
    private long lastDropTime;

    @Override
    public void create() {
        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.jpg"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_PORT_WIDTH, VIEW_PORT_HEIGHT);

        batch = new SpriteBatch();

        bucket = new Rectangle();
        bucket.x = VIEW_PORT_WIDTH / 2 - BUCKET_WIDTH / 2;
        bucket.y = 20;
        bucket.width = BUCKET_WIDTH;
        bucket.height = BUCKET_HEIGHT;

        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();

        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - BUCKET_WIDTH / 2;
        }

        if(TimeUtils.nanoTime() - lastDropTime > DROPLET_GENERATE_INTERVAL) {
            spawnRaindrop();
        }
        Iterator<Rectangle> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= DROPLET_SPEED * Gdx.graphics.getDeltaTime();
            if(raindrop.y + DROPLET_HEIGHT < 0) {
                iter.remove();
            }
            if(raindrop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
            }
        }
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, VIEW_PORT_WIDTH - DROPLET_WIDTH);
        raindrop.y = VIEW_PORT_HEIGHT;
        raindrop.width = DROPLET_WIDTH;
        raindrop.height = DROPLET_HEIGHT;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }
}
