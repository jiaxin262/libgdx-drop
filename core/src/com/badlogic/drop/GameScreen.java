/************
 * Copyright (C) 2004 - 2017 UCWeb Inc. All Rights Reserved.
 * Description :
 * <p>
 * Creation    : 2017/2/9
 * Author      : jiaxin, jx124336@alibaba-inc.com
 */
package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

import static com.badlogic.drop.DropConst.BUCKET_HEIGHT;
import static com.badlogic.drop.DropConst.BUCKET_WIDTH;
import static com.badlogic.drop.DropConst.DROPLET_GENERATE_INTERVAL;
import static com.badlogic.drop.DropConst.DROPLET_HEIGHT;
import static com.badlogic.drop.DropConst.DROPLET_SPEED;
import static com.badlogic.drop.DropConst.DROPLET_WIDTH;
import static com.badlogic.drop.DropConst.VIEW_PORT_HEIGHT;
import static com.badlogic.drop.DropConst.VIEW_PORT_WIDTH;

public class GameScreen implements Screen {

    final Drop game;

    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;

    private Rectangle bucket;
    Vector3 touchPos = new Vector3();
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    int dropsGathered;

    public GameScreen(Drop game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.jpg"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        // start the playback of the background music immediately
        rainMusic.setLooping(true);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_PORT_WIDTH, VIEW_PORT_HEIGHT);

        bucket = new Rectangle();
        bucket.x = VIEW_PORT_WIDTH / 2 - BUCKET_WIDTH / 2;
        bucket.y = 20;
        bucket.width = BUCKET_WIDTH;
        bucket.height = BUCKET_HEIGHT;

        raindrops = new Array<Rectangle>();
        spawnRaindrop();
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
    public void show() {
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, VIEW_PORT_HEIGHT);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - BUCKET_WIDTH / 2;
        }
        if (bucket.x < 0) {
            bucket.x = 0;
        }
        if (bucket.x > VIEW_PORT_WIDTH - BUCKET_WIDTH) {
            bucket.x = VIEW_PORT_WIDTH - BUCKET_HEIGHT;
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
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
