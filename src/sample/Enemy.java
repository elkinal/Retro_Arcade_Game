package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by alxye on 29-Oct-18.
 */
public class Enemy extends Entity implements Hostile {
    private Image enemyImage;
    private Image speedEnemyImage;
    private Image fastestEnemyImage;
    private Image obeseEnemyImage;
    private Image kamikazeEnemyImage;
    private Image behemothEnemyImage;
    private float health;
    private float initHealth;
    private long tickCount;
    public boolean spawners = false;

    public Enemy(float x, float y, float width, float height, EnemyType enemyType) {
        super(x, y, width, height, null, 1.0f);




        //initialize the images available to enemies
        try {
            enemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\worm.png"));
            speedEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\enemy.png"));
            fastestEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\enemyFastest.png"));
            obeseEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\enemyObese.png"));
            kamikazeEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\kamikaze.png"));
            behemothEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\behemoth.png"));

            //creating different enemy types
            if(enemyType == EnemyType.NORMAL) {
                setImage(enemyImage);
                setSpeed(2.0f);
                this.health = 15;
            }
            if(enemyType == EnemyType.FAST) {
                setImage(speedEnemyImage);
                setSpeed(3.0f);
                this.health = 10;
            }
            if(enemyType == EnemyType.TOUGH) {
                setImage(fastestEnemyImage);
                setSpeed(1.5f);
                this.health = 20;
            }
            if(enemyType == EnemyType.OBESE) {
                setImage(obeseEnemyImage);
                setSpeed(1.0f);
                this.health = 100;
                spawners = true;
            }
            if(enemyType == EnemyType.KAMIKAZE) {
                setImage(kamikazeEnemyImage);
                setSpeed(4.0f);
                this.health = 1;
            }
            if(enemyType == EnemyType.BEHEMOTH) {
                setImage(behemothEnemyImage);
                setSpeed(0.5f);
                this.health = 1000;
                spawners = true;
            }
            initHealth = health;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void tick() {
        //movement
        float deltaX = Main.player.getX()+Main.player.getWidth()/2-this.getHeight()/2 - getX();
        float deltaY = Main.player.getY()+Main.player.getHeight()/2-this.getHeight()/2 - getY();
        float angle = (float) Math.atan2( deltaY, deltaX );

        addX((float) (Main.deltaTime/10 * getSpeed() * Math.cos( angle )));
        addY((float) (Main.deltaTime/10 * getSpeed() * Math.sin( angle )));

        //checking if the enemy has run out of health
        if(health <= 0) {
            Main.hostiles.remove(this);
        }

        //experimental development
        tickCount++;
        if(spawners && tickCount % 300 == 0) {
            Main.addEnemies(getKamikaze(getX(), getY()));
        }
        /**creating the illusion that the enemies are getting closer
        (in reality they are simple getting larger when they are getting closer to the edge of the screen)*/

        setSize(getY() * 1/(Main.SCREENWIDTH) * 200 + 10, getY() * 1/(Main.SCREENWIDTH) * 200 + 10);
    }

    public static int[] generateNewPosition() {
        if(Main.rand(0,1) == 0) {
            return new int[] {(int) (Main.rand(0, Main.SCREENWIDTH) - 50), 0};
        }
        else {
            return new int[] {(int) (Main.rand(0, Main.SCREENHEIGHT) - 50), 0};
        }
    }

    public Enemy getKamikaze(float x, float y) {
        return new Enemy(x, y, 50, 50, EnemyType.KAMIKAZE);
    }

    public void draw(GraphicsContext graphics) {
        graphics.drawImage(this.getImage(), getX(), getY(), getWidth(), getHeight());
        //implement a health-bar above each enemy


        if(health < initHealth) {
            graphics.setFill(Color.BLACK);
            graphics.fillRect(getX()-1, getY()-1, getWidth(), 5);
            float healthRatio = (health / initHealth);
            if(healthRatio < 0.3)
                graphics.setFill(Color.DEEPPINK);
            else if(healthRatio >= 0.3 && healthRatio < 0.7)
                graphics.setFill(Color.BLUEVIOLET);
            else
                graphics.setFill(Color.CYAN);
            graphics.fillRect(getX(), getY(), healthRatio * getWidth(), 3);
        }
//        graphics.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    public void removeHealth(float damage) {
        this.health -= damage;
    }

    public float getHealth() {
        return health;
    }
}
