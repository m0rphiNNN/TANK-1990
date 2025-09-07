import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private Map<String, BufferedImage> imageCache;
    private String imagePath = "./images/";
    
    public ResourceManager() {
        imageCache = new HashMap<>();
        loadAllImages();
    }
    
    private void loadAllImages() {
        loadImage("tank_player_up", "player_tank_up.png");
        loadImage("tank_player_down", "player_tank_down.png");
        loadImage("tank_player_left", "player_tank_left.png");
        loadImage("tank_player_right", "player_tank_right.png");
        
        String[] enemyTypes = {"basic", "fast", "power", "armor"};
        String[] directions = {"up", "down", "left", "right"};
        
        for (String tür : enemyTypes) {
            for (String yön : directions) {
                String anahtar = "enemy_" + tür + "_" + yön;
                loadImage(anahtar, "enemy_" + tür + "_" + yön + ".png");
            }
        }
        
        loadImage("bullet_up", "bullet_up.png");
        loadImage("bullet_down", "bullet_down.png");
        loadImage("bullet_left", "bullet_left.png");
        loadImage("bullet_right", "bullet_right.png");
        loadImage("bullet", "bullet.png");

        loadImage("brick", "brick.png");
        loadImage("steel", "steel.png");
        loadImage("water", "water.png");
        loadImage("ice", "ice.png");
        loadImage("trees", "trees.png");
        
        loadImage("eagle", "eagle.png");
        loadImage("eagle_destroyed", "eagle_destroyed.png");
        
        loadImage("powerup_helmet", "powerup_helmet.png");
        loadImage("powerup_shovel", "powerup_shovel.png");
        loadImage("powerup_star", "powerup_star.png");
        loadImage("powerup_grenade", "powerup_grenade.png");
        loadImage("powerup_tank", "powerup_tank.png");
        loadImage("powerup_timer", "powerup_timer.png");

        loadImage("explosion_small", "explosion_small.png");
        loadImage("explosion_medium", "explosion_medium.png");
        loadImage("explosion_large", "explosion_large.png");
        loadImage("explosion_xl", "explosion_xl.png");
        loadImage("explosion_xxl", "explosion_xxl.png");
        
        loadImage("logo", "logo.png");
        loadImage("arrow", "arrow.png");
        loadImage("game_over", "game_over.png");
    }
    
    private void loadImage(String key, String fileName) {
        try {
            File file = new File(imagePath + fileName);
            if (file.exists()) {
                BufferedImage image = ImageIO.read(file);
                imageCache.put(key, image);
            }
        } catch (IOException e) {
            System.out.println("Hata");
        }
    }
    
    public BufferedImage getImage(String key) {
        BufferedImage image = imageCache.get(key);
        return image;
    }

    public boolean isImageLoaded(String key) {
        return imageCache.containsKey(key);
    }
    
    public void loadArrowImage() {
        loadImage("arrow", "arrow.png");
    }

    public void loadGameOverImage() {
        loadImage("game_over", "game_over.png");
    }

    public void loadLogoImage() {
        loadImage("logo", "logo.png");
        if (!isImageLoaded("logo")) {
            System.out.println("Logo görseli yüklenemedi. Varsayılan görsel kullanılacak.");
        }
    }
}