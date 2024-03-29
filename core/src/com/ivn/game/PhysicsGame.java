package com.ivn.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.round;

public class PhysicsGame extends ApplicationAdapter {
	private static final float STEP_TIME = 1f / 60f;
	private static final int VELOCITY_ITERATIONS = 6;
	private static final int POSITION_ITERATIONS = 2;
	private static final float SCALE = 0.05f;

	private TextureAtlas       textureAtlas;
	private SpriteBatch        batch;
	private final HashMap<String, Sprite> sprites = new HashMap<>();

	private OrthographicCamera camera;
	private ExtendViewport     viewport;

	private World              world;
	private Box2DDebugRenderer debugRenderer;
	private PhysicsShapeCache  physicsBodies;

	private float              accumulator = 0;

	// private Body banana;
	private Body ground;
	private Body paredIzq;
	private Body paredDer;
	private Body techo;

	private static final int COUNT = 20;
	private Body[] fruitBodies = new Body[COUNT];
	private String[] names = new String[COUNT];

	@Override
	public void create() {
		ShaderProgram.pedantic = false;

		camera = new OrthographicCamera();
		viewport = new ExtendViewport(50, 50, camera);

		batch = new SpriteBatch();
		textureAtlas = new TextureAtlas("sprites.txt");
		addSprites();

		Box2D.init();
		world = new World(new Vector2(0, -120), true);
		physicsBodies = new PhysicsShapeCache("physics.xml");

		debugRenderer = new Box2DDebugRenderer();

		//banana = createBody("banana", 10, 50, 0);
		generateFruit();
	}


	private void generateFruit() {
		String[] fruitNames = new String[]{"banana", "cherries", "orange","crate"};

		Random random = new Random();

		for (int i = 0; i < fruitBodies.length; i++) {
			String name = fruitNames[random.nextInt(fruitNames.length)];

			//float x = random.nextFloat() * 50;
			//float y = random.nextFloat() * 50 + 50;
			float x = i*4;
			float y = 45;

			names[i] = name;
			fruitBodies[i] = createBody(name, x, y, 0);
		}
	}

	private void createGround() {

		// Suelo
		if (ground != null) world.destroyBody(ground);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 1;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth, 1);

		fixtureDef.shape = shape;

		ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);
		ground.setTransform(0, -1, 0);

		shape.dispose();

		// Techo
		if (techo != null) world.destroyBody(techo);

		techo = world.createBody(bodyDef);
		techo.createFixture(fixtureDef);
		techo.setTransform(0, 51, 0);

		// Paredes
		if (paredIzq != null) world.destroyBody(paredIzq);
		if (paredDer != null) world.destroyBody(paredDer);

		BodyDef bodyDefIzq = new BodyDef();
		BodyDef bodyDefDer = new BodyDef();
		bodyDefIzq.type = BodyDef.BodyType.StaticBody;
		bodyDefDer.type = BodyDef.BodyType.StaticBody;


		FixtureDef fixtureDefIzq = new FixtureDef();
		FixtureDef fixtureDefDer = new FixtureDef();
		fixtureDefIzq.friction = 1;
		fixtureDefDer.friction = 1;

		PolygonShape shapeIzq = new PolygonShape();
		shapeIzq.setAsBox(1, camera.viewportHeight);
		PolygonShape shapeDer = new PolygonShape();
		shapeDer.setAsBox(1, camera.viewportHeight);

		fixtureDefDer.shape = shapeDer;
		fixtureDefIzq.shape = shapeIzq;

		paredIzq = world.createBody(bodyDefIzq);
		paredIzq.createFixture(fixtureDefIzq);
		paredIzq.setTransform(-1, 0, 0);

		paredDer = world.createBody(bodyDefDer);
		paredDer.createFixture(fixtureDefDer);
		paredDer.setTransform(camera.viewportWidth+1, 0, 0);

		shapeIzq.dispose();
		shapeDer.dispose();

	}

	private Body createBody(String name, float x, float y, float rotation) {
		Body body = physicsBodies.createBody(name, world, SCALE, SCALE);
		body.setTransform(x, y, rotation);

		return body;
	}

	private void addSprites() {
		Array<AtlasRegion> regions = textureAtlas.getRegions();

		for (AtlasRegion region : regions) {
			Sprite sprite = textureAtlas.createSprite(region.name);

			float width = sprite.getWidth() * SCALE;
			float height = sprite.getHeight() * SCALE;

			sprite.setSize(width, height);

			sprites.put(region.name, sprite);
		}
	}

	private void stepWorld() {
		float delta = Gdx.graphics.getDeltaTime();

		accumulator += Math.min(delta, 0.25f);

		if (accumulator >= STEP_TIME) {
			accumulator -= STEP_TIME;
			world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);

		batch.setProjectionMatrix(camera.combined);

		createGround();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stepWorld();

		batch.begin();

		for (int i = 0; i < fruitBodies.length; i++) {
			Body body = fruitBodies[i];
			String name = names[i];

			Vector2 position = body.getPosition();
			float degrees = (float) Math.toDegrees(body.getAngle());
			drawSprite(name, position.x, position.y, degrees);
		}

		// Pruebaas
		int hg=round( Gdx.input.getAccelerometerY());
		int vg=round( Gdx.input.getAccelerometerX());
		System.out.println("Gravedad Horizontal : "+hg+" Gravedad Vertical: "+vg);
		world.setGravity(new Vector2(hg*15,-vg*15));

		batch.end();

		debugRenderer.render(world, camera.combined);

	}

	private void drawSprite(String name, float x, float y) {
		Sprite sprite = sprites.get(name);
		sprite.setPosition(x, y);
		sprite.draw(batch);
	}

	private void drawSprite(String name, float x, float y, float degrees) {
		Sprite sprite = sprites.get(name);
		sprite.setPosition(x, y);
		sprite.setRotation(degrees);
		sprite.setOrigin(0f,0f);
		sprite.draw(batch);
	}

	@Override
	public void dispose() {
		textureAtlas.dispose();
		sprites.clear();
		world.dispose();
		debugRenderer.dispose();
	}
}