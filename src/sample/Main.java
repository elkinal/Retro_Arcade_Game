package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main extends Application {

    /**
     * Main TODO list:
     * When two enemies merge, they should form a new enemy which shows the number of entities inside it on it's front
     * It also changes color and other attributes
     *
     * The player's inertia should be decreased when the user releases the movement keys
     *
     * Proper icons / pictures should be added to the game
     *
     * Multithreading capabilities should be improved in order to make the movement, rendering, collision detection, etc...
     * more efficient
     *
     * Boundaries (player boundaries)
     *
     * Upgrades
     *
     * Ammo crates
     *
     * */
    public static int SCREENWIDTH;
    public static int SCREENHEIGHT;

    //deleteME!
//    private static Level level = new Level(30);

    public static ArrayList<Entity> hostiles = new ArrayList<>();
    public static ArrayList<Entity> friendlies = new ArrayList<>();
    public static ArrayList<Gun> weapons = new ArrayList<>(); //check if this is useless

    public static Player player;
    private Image genericEnemyImage;
    private Image rifleBulletImage;
    private Thread collisionDetectionThread;
    private Thread renderingThread;


    private GraphicsContext gc;

    private boolean gameRunning = true;
    public static boolean gamePaused = false;
    private GameState gameState = GameState.MENU;
    public static Display display = new Display();

    public static double deltaTime;


    @Override
    public void start(Stage primaryStage) throws Exception{
        SCREENHEIGHT = (int) Screen.getPrimary().getBounds().getHeight();
        SCREENWIDTH = (int) Screen.getPrimary().getBounds().getWidth();

        //loading the required level for the game


        //Forces the game to be played full-screen
        primaryStage.setTitle("Elkin's cool game");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        Group root = new Group();
        Scene scene = new Scene(root);

        //Prevents NullPointerReferences
        initializeObjects();
        Sound.playAmbiantSound();

        //Checking for user interaction via the key presses
        scene.setOnKeyPressed(event -> {
            //make this animation smooth - change to set // TODO: 07-Nov-18 Add full-auto capabilities for all guns
            // TODO: 09-Nov-18 make sure that you cannot spam weapons - add a reload time the prevents the user from spamming shotgun pellets
            if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W)
                player.addVelY(-player.getSpeed());
            else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S)
                player.addVelY(player.getSpeed());
            else if(event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A)
                player.addVelX(-player.getSpeed());
            else if(event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D)
                player.addVelX(player.getSpeed());

            if(event.getCode() == KeyCode.DIGIT1)
                player.setSelectedWeapon(SelectedWeapon.RIFLE);
            if(event.getCode() == KeyCode.DIGIT2)
                player.setSelectedWeapon(SelectedWeapon.SHOTGUN);
            if(event.getCode() == KeyCode.DIGIT3)
                player.setSelectedWeapon(SelectedWeapon.SNIPER);
            if(event.getCode() == KeyCode.DIGIT4)
                player.setSelectedWeapon(SelectedWeapon.MISSILE);

            if(event.getCode() == KeyCode.ESCAPE) {
                display.setSelectedPauseMenuItem(0);
                gamePaused = !gamePaused;
            }

            if(event.getCode() == KeyCode.SPACE)
                gameState = GameState.PLAYING;

            //This allows the player to restart once he has lost
            if(!gameRunning && event.getCode() == KeyCode.ENTER) {
                gameOver(false);
                gameRunning = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S)
                player.setVelY(0);
            if(event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D)
                player.setVelX(0);
        });

        scene.setOnScroll(event -> {
            if(event.getDeltaY() > 0)
                display.changeSelectedPauseMenuItem(1);
            else
                display.changeSelectedPauseMenuItem(-1);
        });


        primaryStage.setScene(scene);
        Canvas canvas = new Canvas(SCREENWIDTH, SCREENHEIGHT);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();


        root.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            /**adding bullets to the friendlies ArrayList when the player "shoots" using the left mouse key
             * The bullet's trajectory is also taking recoil into account
             *
             * Possible solutions: making the shooting on the next thread*/
            if(!gamePaused && gameRunning && gameState == GameState.PLAYING) {
                if (player.getSelectedWeapon() == SelectedWeapon.RIFLE) {
                    Sound.playRifleSound();
                    addFriendlies(Guns.rifleBullet());
                }
                if (player.getSelectedWeapon() == SelectedWeapon.SHOTGUN) {
                    Sound.playShotgunSound();
                    addFriendlies(Guns.shotgunBullet(12));
                }
                if (player.getSelectedWeapon() == SelectedWeapon.SNIPER) {
                    Sound.playSniperSound();
                    addFriendlies(Guns.sniperBullet());
                }
                if (player.getSelectedWeapon() == SelectedWeapon.MISSILE) {
                    //Play the sound of you lobbing the missile when the player presses the appropriate key // TODO: 09-Nov-18 add the sound
                    addFriendlies(Guns.missile());
                }
            }
            //controlling what the pause menu selector does
            else if(gamePaused && gameRunning) {
                switch (display.getSelectedPauseMenuItem()) {
                    case 0:
                        gamePaused = false;
                        break;
                    case 1:
                        gameOver(true);
                        break;
                    case 2:
                        gameState = GameState.MENU;
                        break;
                    case 3:
                        System.exit(0);
                        break;
                }


            }

        });






        //This is the main game loop - everything is controlled from here
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc); // TODO: 22-Nov-18 Consider making the game run on two or more threads (beware of thread interference!)
            }
        }.start();

        primaryStage.show();
    }

    private void render(GraphicsContext graphics) { //synchronizing the block does not solve the error
        graphics.clearRect(0, 0, SCREENWIDTH, SCREENHEIGHT); //ess around with this to create some cool effects
        if(gameState == GameState.PLAYING) {
//            double startTime = System.nanoTime(); //ALERT
            graphics.drawImage(display.gameBackground, 0, 0, SCREENWIDTH, SCREENHEIGHT);
            for (int i = 0; i < hostiles.size(); i++) {
                hostiles.get(i).draw(graphics);
            }
            //Drawing all Objects in the friendlies ArrayList
            graphics.setFill(Color.GREEN);
            for (int i = 0; i < friendlies.size(); i++) {
                graphics.drawImage((friendlies.get(i).getImage()), friendlies.get(i).getX(), friendlies.get(i).getY(), friendlies.get(i).getWidth(), friendlies.get(i).getHeight());
                //error usually occurs here
            }
            //Drawing the HUD
            display.drawScore(graphics);
            display.drawSelectedWeapon(graphics);

            //Drawing the player
            player.draw(graphics);

            //Drawing the game over screen when the player has lost
            if (!gameRunning) display.drawGameOverScreen(graphics);
            if (gamePaused && gameRunning) display.drawPauseScreen(graphics);

            /*double endTime = System.nanoTime();
            double totalTime = (endTime - startTime) / 1000000;
            System.out.println(totalTime + ",");*/
            //remove this
//            display.drawText(graphics, 100, 100, String.valueOf(1/((deltaTime + 10)/1000)));
        }
        else if(gameState == GameState.MENU) {
            display.drawMenuScreen(graphics);
        }
    }


    public static void addEnemies(Enemy ... enemies) {
        Collections.addAll(hostiles, enemies);
    }
    private void addFriendlies(Bullet ... bullets) {
        Collections.addAll(friendlies, bullets);
    }

    public static Enemy addGenericEnemy(float x, float y) {
        int ratio = (int) rand(0, 100);
        if(ratio < 50)
            return new Enemy(x, y, 50, 50, EnemyType.NORMAL);
        if(ratio >= 50 && ratio < 80)
            return new Enemy(x, y, 50, 50,EnemyType.FAST);
        if(ratio >= 80 && ratio < 95)
            return new Enemy(x, y, 50, 50, EnemyType.TOUGH);
        if(ratio >= 95 && ratio < 100)
            return new Enemy(x, y, 50, 50,EnemyType.OBESE);
        if(ratio == 100)
            return new Enemy(x, y, 50, 50,EnemyType.BEHEMOTH);

        return new Enemy(x, y, 50, 50, EnemyType.NORMAL);
    }


    private void initializeObjects() {
        try {
//            Display display = new Display(); //not used, but the constructor is required for the initialization of certain fields in the class // TODO: 02-Nov-18 try making this constructor declaration i
            genericEnemyImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\enemies\\enemy.png"));
            player = new Player(Main.SCREENWIDTH/2 - 35/2, Main.SCREENHEIGHT/2 - 35/2, 70, 70, new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\player.png")), 5.0f);
            rifleBulletImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\rifleBullet.png"));
            //adding enemies to the hostiles ArrayList
            addEnemies(
                    addGenericEnemy(0, 0),
                    addGenericEnemy(200, 0),
                    addGenericEnemy(400, 0),
                    addGenericEnemy(600, 0),
                    addGenericEnemy(800, 0),
                    addGenericEnemy(1000, 0),
                    addGenericEnemy(1200, 0)
            );


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        initializeThreads();
    }

    private void initializeThreads() {
        collisionDetectionThread = new Thread() { // TODO: 09-Nov-18 Find a way to incorporate the deltaTime Formula

            @Override
            public void run() {
                while (gameRunning) {

                        update();
                        //limiting the amount of FPS
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                    System.out.println("FPS: " + 1/((deltaTime + 10)/1000));

                }
            }
        };
        collisionDetectionThread.start();
    }

    private void update() {
        if (!gamePaused && gameState == GameState.PLAYING) {

            double startTime = System.nanoTime(); //ALERT

            //Checking for collisions between the player and enemies
            if (Physics.collision(player, hostiles)) {
                Sound.playDeathSound();
                gameOver(false);
            }

            int[] result = Physics.collision(friendlies, hostiles);
            if (result[0] != -1) {
                //hostiles.remove(result[0]);
                //removing health from the enemies

                if (player.getSelectedWeapon() == SelectedWeapon.RIFLE)
                    ((Enemy) hostiles.get(result[0])).removeHealth(Guns.rifleBullet().getDamage());
                if (player.getSelectedWeapon() == SelectedWeapon.SHOTGUN)
                    ((Enemy) hostiles.get(result[0])).removeHealth(Guns.shotgunBullet(1)[0].getDamage());
                if (player.getSelectedWeapon() == SelectedWeapon.SNIPER)
                    ((Enemy) hostiles.get(result[0])).removeHealth(Guns.sniperBullet().getDamage());


                player.addScore(1);
                //Removing the missile after it hits the player - and adding shrapnel
                        /*if (result[1] != -1) { //checks if the bullet is a missile
                            addFriendlies(Guns.shrapnel(30,
                                    (int) friendlies.get(result[1]).getX(),
                                    (int) friendlies.get(result[1]).getY())
                            );
                            //Playing the missile explosion sound
                            Sound.playMissileSound();

                            friendlies.remove(result[1]);
                        }*/

                if (result[2] == 1) {
                    addFriendlies(Guns.shrapnel(30,
                            (int) friendlies.get(result[1]).getX(),
                            (int) friendlies.get(result[1]).getY())
                    );
                    //Playing the missile explosion sound
                    Sound.playMissileSound();
//                            ((Enemy) hostiles.get(result[0])).removeHealth(100);
                    friendlies.remove(result[1]);
                } else {
                    ((Enemy) hostiles.get(result[0])).removeHealth(((Bullet) (friendlies.get(result[1]))).getDamage());
                    friendlies.remove(result[1]);
                }

                if (((Enemy) hostiles.get(result[0])).getHealth() < 0) {
                    for (int i = 0; i < ((Math.random() < 0.1) ? 2 : 1); i++) {
                        addEnemies(addGenericEnemy(Enemy.generateNewPosition()[0], Enemy.generateNewPosition()[1]));
                    }
                }


            }

            //Time increment, tick methods
            player.tick();
            for (int i = 0; i < friendlies.size(); i++) {
                ((Bullet) friendlies.get(i)).tick();
            }
            for (int i = 0; i < hostiles.size(); i++) {
                ((Enemy) hostiles.get(i)).tick();
            }

            double endTime = System.nanoTime(); //ALERT
            deltaTime = (endTime - startTime) / 10000000 + 10;
        }
    }



    private void gameOver(boolean noMenu) {
        friendlies.clear();
        gameRunning = false;
        gamePaused = false;
//        collisionDetectionThread = null; //Allowing the thread to be garbage collected (maybe not?)
        hostiles.clear();
        initializeObjects();
        if(noMenu) gameRunning = true;
    }

    public static float rand(float min, float max) {
        return new Random().nextInt((int) (max - min + 1)) + min;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
