package com.game.towerdefense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.*;

/**
 * Основной класс, описывающий действия в игре.
 **/
public class Game extends ApplicationAdapter {
    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private static final float SCALE = 0.10f;
    private static final float SCALE_ENEMY = 0.10f;
    private static final float SCALE_TOWER = 0.15f;
    private static final float SCALE_FRUITS = 0.02f;
    private static final float SCALE_LIFE = 0.05f;

    private TextureAtlas textureAtlas;
    private SpriteBatch batch;
    private final Map<String, Sprite> sprites = new HashMap<String, Sprite>();

    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private PhysicsShapeCache physicsBodies;

    private float accumulator = 0;

    private List<Enemy> listOfEnemies = new ArrayList<>();
    private List<Tower> listOfTowers = new ArrayList<>();
    private List<Weapon> listOfWeapons = new ArrayList<>();
    private List<Integer> indexRemove = new ArrayList<>();

    private long lastTime;
    private float speed = 0.5f;
    private int numberOfMissedEnemies = 0;
    private boolean gameOver = false;


    /**
     * Метод, являющийся входной точкой, для рендеринга окна
     **/
    @Override
    public void create() {
        Vector2 dir;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(100, 100, camera);

        textureAtlas = new TextureAtlas("sprites.txt");
        addSprites();

        Box2D.init();
        dir = new Vector2(0, -10f);
        world = new World(dir, true);
        physicsBodies = new PhysicsShapeCache("physics.xml");

        generateEnemy();

        debugRenderer = new Box2DDebugRenderer();

        Gdx.input.setInputProcessor(input());
    }

    /**
     * Метод для работы с мышью и клавиатурой
     **/
    private InputAdapter input() {
        InputAdapter input = new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                if (gameOver)
                    return true;
                Tower tower = new Tower(ft_map_x(x, 100, 1000), ft_map_y(y, 100, 1000));
                generateWeapon(tower);
                listOfTowers.add(tower);
                return true; // return true to indicate the event was handled
            }

            @Override
            public boolean keyDown(int keycode) {
                if (listOfTowers.size() > 0 && keycode == 67)
                    listOfTowers.remove(listOfTowers.size() - 1);
                if (keycode == 131)
                    Gdx.app.exit();
                if (keycode == 55)
                    speed += 0.5;
                if (keycode == 56 && speed > 0.5)
                    speed -= 0.5;
                return true;
            }
        };
        return input;
    }

    /**
     * Перевод координат дисплея по x
     **/
    static float ft_map_x(int x, double size, int width) {
        return (float) (x * size / width - 10);
    }

    /**
     * Перевод координат дисплея по y
     **/
    static float ft_map_y(int y, double size, int height) {
        return (float) (-y * size / height + (size));
    }

    /**
     * Создание изображения заданного объекта в виде сущности в окне
     **/
    private Body createBody(String name, float x, float y, float rotation) {
        float scale;

        if (name.equals("life")) {
            scale = SCALE_LIFE;
        } else if (name.equals("tower")) {
            scale = SCALE_TOWER;
        } else if (name.equals("orange") || name.equals("cherries") || name.equals("banana")) {
            scale = SCALE_FRUITS;
        } else if (isEnemy(name)) {
            scale = SCALE_ENEMY;
        } else {
            scale = SCALE;
        }
        Body body = physicsBodies.createBody(name, world, scale, scale);
        body.setTransform(x, y, rotation);

        return body;
    }

    /**
     * Проверка на то, является ли объект врагом по имени
     **/
    private boolean isEnemy(String name) {
        int i = 1;
        while (i++ <= 6) {
            if (name.equals("m" + i))
                return true;
        }
        return false;
    }

    /**
     * Добавление спрайтов(башен, врагов, оружия)
     **/
    private void addSprites() {
        Array<AtlasRegion> regions = textureAtlas.getRegions();
        float width;
        float height;

        for (AtlasRegion region : regions) {
            Sprite sprite = textureAtlas.createSprite(region.name);
            if (region.name.equals("life")) {
                width = sprite.getWidth() * SCALE_LIFE;
                height = sprite.getHeight() * SCALE_LIFE;
            } else if (region.name.equals("tower")) {
                width = sprite.getWidth() * SCALE_TOWER;
                height = sprite.getHeight() * SCALE_TOWER;
            } else if (region.name.equals("orange") || region.name.equals("cherries") || region.name.equals("banana")) {
                width = sprite.getWidth() * SCALE_FRUITS;
                height = sprite.getWidth() * SCALE_FRUITS;
            } else if (isEnemy(region.name)) {
                width = sprite.getWidth() * SCALE_ENEMY;
                height = sprite.getWidth() * SCALE_ENEMY;
            } else {
                width = sprite.getWidth() * SCALE;
                height = sprite.getHeight() * SCALE;
            }

            sprite.setSize(width, height);
            sprite.setOrigin(0, 0);
            sprites.put(region.name, sprite);
        }
    }

    /**
     * Метод, отвечающий за время, относительно которого происходят действия
     **/
    private void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();

        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;

            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    /**
     * Метод, который отвечает за изменение размера основного окна
     **/
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);

    }

    /**
     * Метод по генерации оружия
     **/
    private void  generateWeapon(Tower tower) {
        Weapon weapon = new Weapon();
        String[] enemyNames = new String[]{"banana", "cherries", "orange"};

        Random random = new Random();

        String name = enemyNames[random.nextInt(enemyNames.length)];
        weapon.setName(name);
        float x = tower.getX() + 8;
        float y = tower.getY() + 8;
        weapon.setBody(createBody(name, x, y, 0));
        weapon.setX(x);
        weapon.setY(y);
        listOfWeapons.add(weapon);
        tower.setWeapon(weapon);

    }

    /**
     * Метод по генерации врагов
     **/
    private void generateEnemy() {
        Enemy enemy = new Enemy();
        String[] enemyNames = new String[]{"m1", "m2", "m3", "m4", "m5", "m6"};

        Random random = new Random();

        String name = enemyNames[random.nextInt(enemyNames.length)];
        float x = random.nextFloat() * 10;
        float y = random.nextFloat() * 10;
        enemy.setOffsetX(x);
        enemy.setOffsetY(y);
        enemy.setStartX(x + 10);
        enemy.setStartY(y + 100);
        enemy.setName(name);
        enemy.setBody(createBody(name, enemy.getStartX(), enemy.getStartY(), 0));
        listOfEnemies.add(enemy);
        lastTime = TimeUtils.nanoTime();
    }

    /**
     * Метод, отвечающий за отрисовку врагов
     **/
    private void displayEnemies() {
        for (int i = 0; i < listOfEnemies.size(); i++) {
            Enemy enemy = listOfEnemies.get(i);
            Body body = enemy.getBody();
            String name = enemy.getName();

            Vector2 position = body.getPosition();
            float degrees = (float) Math.toDegrees(body.getAngle());
            route(position, body, enemy.getOffsetX(), enemy.getOffsetY());
            drawSprite(name, position.x, position.y, degrees);
            if (position.y < -10) {
                numberOfMissedEnemies++;
                indexRemove.add(i);
            }
        }
    }

    /**
     * Метод, отвечающий за отрисовку тайлов
     **/
    private void displayTiles() {
        int name_s = 1;
        int y_s = 90;
        for (int i = 0; i < 10; i++) {
            int x_s = 0;
            for (int j = 0; j < 10; j++) {
                drawSprite("" + name_s, x_s, y_s, 0);
                name_s++;
                x_s += 10;
            }
            y_s -= 10;
        }
    }

    /**
     * Метод, отвечающий за отрисовку башен
     **/
    private void displayTowers() {
        for (int i = 0; i < listOfTowers.size(); i++) {
            Tower tower = listOfTowers.get(i);
            Weapon weapon = tower.getWeapon();
            Body bodyWeapon = weapon.getBody();
            getDirectionWeapon(tower);
            if (tower.getWeapon().isFound()) {
                bodyWeapon.applyForce(weapon.getSub(), new Vector2(0, 0), true);
                weapon.setX(bodyWeapon.getPosition().x);
                weapon.setY(bodyWeapon.getPosition().y);
                drawSprite(weapon.getName(), weapon.getX(), weapon.getY(), 0);
                Body body = weapon.getEnemy().getBody();
                Vector2 position = body.getPosition();
                Vector2 newSub = position.sub(weapon.getX(), weapon.getY());
                float len = newSub.len();
                if (len < 10) {
                    listOfEnemies.remove(weapon.getEnemy());
                    listOfWeapons.remove(weapon);
                    generateWeapon(tower);
                } else if (len > 50) {
                    listOfWeapons.remove(weapon);
                    generateWeapon(tower);
                }
            }
            drawSprite("tower", tower.getX(), tower.getY(), 0);
        }
    }

    /**
     * Метод, отвечающий за удаление врагов
     **/
    private void deleteEnemies() {
        for (int i = 0; i < indexRemove.size(); i++) {
            listOfEnemies.remove(listOfEnemies.get(indexRemove.get(i)));
        }
    }

    /**
     * Метод, отвечающий за отображение жизней
     **/
    private void displayLifes() {
        if (numberOfMissedEnemies < 1)
            drawSprite("life", 74, 3, 0);
        if (numberOfMissedEnemies < 2)
            drawSprite("life", 82, 3, 0);
        if (numberOfMissedEnemies < 3)
            drawSprite("life", 90, 3, 0);
    }

    /**
     * Метод, проверяющий количество пропущенных врагов
     **/
    private void checkNumberOfMissedEnemies() {
        if (numberOfMissedEnemies > 2) {
            gameOver = true;
            drawSprite("game_over", 20, 20, 0);
        } else if (TimeUtils.nanoTime() - lastTime > 2000000000) {
            generateEnemy();
        }
    }

    /**
     * Метод, отвечающий за цикл событий
     **/
    @Override
    public void render() {
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stepWorld();

        batch.begin();

        displayTiles();
        displayEnemies();
        displayTowers();
        displayLifes();
        checkNumberOfMissedEnemies();
        deleteEnemies();
        indexRemove.clear();
        batch.end();
//		debugRenderer.render(world, camera.combined);
    }

    /**
     * Метод, отвечающий за направление движения оружия
     **/
    private void getDirectionWeapon(Tower tower) {
        float tmpDistance = 40;
        Vector2 sub;

        for (int j = 0; j < listOfEnemies.size(); j++) {
            Enemy enemy = listOfEnemies.get(j);
            Body body = enemy.getBody();
            Vector2 position = body.getPosition();
            sub = position.sub(tower.getX(), tower.getY());
            float distance = sub.len();
            if (distance < tmpDistance) {
                tmpDistance = distance;
                tower.getWeapon().setFound(true);
                tower.getWeapon().setSub(sub);
                tower.getWeapon().setEnemy(listOfEnemies.get(j));
            }
        }
    }

    /**
     * Метод, отвечающий за изображение спрайтов(башен, врагов, оружия)
     **/
    private void drawSprite(String name, float x, float y, float degrees) {
        Sprite sprite = sprites.get(name);
        sprite.setPosition(x, y);
        sprite.setRotation(degrees);
        sprite.draw(batch);
    }

    /**
     * Метод, отвечающий за маршрут врагов
     **/
    private void route(Vector2 position, Body body, float offsetX, float offsetY) {
        if (position.y <= 15 + offsetY && position.x >= 15 + offsetX && position.y < 50) {
            position.y = 15 + offsetY;
            position.x = position.x - speed;
        } else if (position.y <= 75 + offsetY && position.y > 50 && position.x < 75 + offsetX) {
            position.y = 75 + offsetY;
            position.x = position.x + speed;
        }
        body.setTransform(position.x, position.y, 0);
    }

    /**
     * Метод, отвечающий за очистку памяти
     **/
    @Override
    public void dispose() {
        batch.dispose();
        textureAtlas.dispose();
        sprites.clear();
        world.dispose();
        debugRenderer.dispose();
    }
}

