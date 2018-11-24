package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
 * Created by alxye on 29-Oct-18.
 */
public class Display {
    private Image gunSelector;
    private Image weaponImages;

    public Image rifleBullet;
    public Image shotgunBullet;
    public Image sniperBullet;
    public Image missileBullet;

    public Image gameOverImage;
    public Image gameBackground;

    public Image pauseScreen;
    public Image menuBackground;

    /*public Image pauseOptionResume;
    public Image pauseOptionTheme;
    public Image pauseOptionRestart;
    public Image pauseOptionMenu;*/
    private Image[] startingMenuFrames = new Image[2];
    private long menuTicks = 0;

    private Image[] weaponsMenu = new Image[4];
    public Image pauseMenuSelector;
    private Image[] pauseMenu = new Image[4];
    private Image[] playingField = new Image[6];
    private int selectedPauseMenuItem = 0;

    // STOPSHIP: 19-Nov-18 Remember to ask Mr. Huxley about unblocking github and realted services. DO this as quickly as possible

    public Display() {
        try {
            rifleBullet = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\rifleBullet.png"));

            weaponsMenu[0] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\rifle.png"));
            weaponsMenu[1] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\shotgun.png"));
            weaponsMenu[2] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\sniper.png"));
            weaponsMenu[3] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\guns\\rocket.png"));

            pauseMenu[0] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseMenu\\option1.png"));
            pauseMenu[1] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseMenu\\option2.png"));
            pauseMenu[2] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseMenu\\option3.png"));
            pauseMenu[3] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseMenu\\option4.png"));

            /*playingField[0] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background1.png"));
            playingField[1] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background2.png"));
            playingField[2] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background3.png"));
            playingField[3] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background-1.png"));
            playingField[4] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background-2.png"));
            playingField[5] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\sprite\\background-3.png"));*/



            startingMenuFrames[0] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\themes\\menuScreen1.png"));
            startingMenuFrames[1] = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\themes\\menuScreen2.png"));


            pauseScreen = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseScreen.png"));
            pauseMenuSelector = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\pauseMenu\\pauseMenuSelector.png"));

            gameOverImage = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\themes\\gameOverScreen.png"));
//            gameBackground = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\themes\\background.png"));
            gameBackground = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\themes\\menuScreen.png"));



            gunSelector = new Image(new FileInputStream("C:\\Users\\alxye\\Desktop\\2DShooter\\selector.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void drawScore(GraphicsContext graphics) {
        graphics.setFill(Color.PALEVIOLETRED);
        graphics.setFont(new Font("Verdana", 24));
        graphics.fillText("Score " + (int)Main.player.getScore(), Main.SCREENWIDTH-170, 40);
    }

    public void changeSelectedPauseMenuItem(int n) {
        if(selectedPauseMenuItem+n <= 3 && selectedPauseMenuItem+n >= 0 && Main.gamePaused)
            selectedPauseMenuItem += n;
    }
    public void setSelectedPauseMenuItem(int n) {
        selectedPauseMenuItem = n;
    }
    public int getSelectedPauseMenuItem() {
        return selectedPauseMenuItem;
    }
    public void drawText(GraphicsContext graphics, float x, float y, String text) {
        graphics.setFill(Color.PALEVIOLETRED);
        graphics.setFont(new Font("Verdana", 24));
        graphics.fillText("Score " + text, x, y);
    }

    public void drawPauseMenuOptions(GraphicsContext graphics) {
        for (int i = 0; i < pauseMenu.length; i++) {
            graphics.drawImage(pauseMenu[i], Main.SCREENWIDTH/2-200, Main.SCREENHEIGHT/2 + i * 100 - 200, 400, 100);
        }
        graphics.drawImage(pauseMenuSelector, Main.SCREENWIDTH/2 + 150, Main.SCREENHEIGHT/2 + selectedPauseMenuItem * 100 - 200, 50, 100);
    }

    public void drawGameOverScreen(GraphicsContext graphics) {
        graphics.drawImage(gameOverImage, 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);
    }

    /*public void drawGameField(GraphicsContext graphics) {
        menuTicks++;
        if(menuTicks % 20 > 9)
            graphics.drawImage(playingField[0], 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);
        else
            graphics.drawImage(playingField[1], 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);
    }*/
    public void drawPauseScreen(GraphicsContext graphics) {
        graphics.drawImage(pauseScreen, 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);
        drawPauseMenuOptions(graphics);
    }

    public void drawMenuScreen(GraphicsContext graphics) {
        menuTicks++;
        if(menuTicks % 20 > 9)
            graphics.drawImage(startingMenuFrames[0], 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);
        else
            graphics.drawImage(startingMenuFrames[1], 0, 0, Main.SCREENWIDTH, Main.SCREENHEIGHT);

    }



    public void drawSelectedWeapon(GraphicsContext graphics) {
        /*for (int i = 0; i < SelectedWeapon.values().length; i++) {
            graphics.fillRect(i * 150 + Main.SCREENWIDTH/2 - SelectedWeapon.values().length/2 * 150, Main.SCREENHEIGHT-150, 150, 150);
        }*/


        /*graphics.drawImage(image1, Main.SCREENWIDTH / 2 - SelectedWeapon.values().length/2 * 150, Main.SCREENHEIGHT-150, 150, 150);
        graphics.drawImage(image2, 150 + Main.SCREENWIDTH/2 - SelectedWeapon.values().length/2 * 150, Main.SCREENHEIGHT-150, 150, 150);
        graphics.drawImage(image3, 300 + Main.SCREENWIDTH/2 - SelectedWeapon.values().length/2 * 150, Main.SCREENHEIGHT-150, 150, 150);
        graphics.drawImage(image4, 450 + Main.SCREENWIDTH/2 - SelectedWeapon.values().length/2 * 150, Main.SCREENHEIGHT-150, 150, 150);
        graphics.drawImage(gunSelector, Arrays.asList(SelectedWeapon.values()).indexOf(Main.player.getSelectedWeapon()) * 150 + Main.SCREENWIDTH/2 -
                        SelectedWeapon.values().length/2 * 150,
                Main.SCREENHEIGHT-150, 150, 150);*/

        for (int i = 0; i < weaponsMenu.length; i++) {
            graphics.drawImage(weaponsMenu[i], 0, Main.SCREENHEIGHT - 400 + i * 100, 300, 100);
        }
        graphics.drawImage(gunSelector, 0, Main.SCREENHEIGHT - 400 + Arrays.asList(SelectedWeapon.values()).indexOf(Main.player.getSelectedWeapon()) * 100, 300, 100);

        /*graphics.drawImage(gunSelector, 0, Main.SCREENHEIGHT - 400 + Arrays.asList(SelectedWeapon.values()).indexOf(Main.player.getSelectedWeapon()) * 100, 300, 100);
        graphics.drawImage(weaponImages, 0, Main.SCREENHEIGHT - 400, 300, 400);*/

    }
}
