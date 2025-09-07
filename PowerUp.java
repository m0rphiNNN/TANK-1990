import java.awt.*;
import java.util.Random;

public class PowerUp extends GameObject {
    public enum PowerUpType {
        HELMET,   
        SHOVEL,   
        STAR,    
        GRENADE,  
        TANK,    
        TIMER,    
    }
    
    private PowerUpType type;
    private ResourceManager resourceManager;
    private long spawnTime;
    private static final long LIFETIME = 15000; 
     public static final int POINTS_VALUE = 500;
    
    public PowerUp(float x, float y, PowerUpType type, ResourceManager resourceManager) {
        super(x, y, 32, 32);
        this.type = type;
        this.resourceManager = resourceManager;
        this.spawnTime = System.currentTimeMillis();
    }
    
    private static final int[][] SPAWN_LOCATIONS = {
        {2, 2}, {6, 2}, {10, 2},
        {2, 4}, {6, 4}, {10, 4},
        {2, 6}, {6, 6}, {10, 6}, 
        {2, 8}, {6, 8}, {10, 8},
        {2, 10}, {6, 10}, {10, 10}
    };

    public static int[] getRandomSpawnLocation(Random random) {
        return SPAWN_LOCATIONS[random.nextInt(SPAWN_LOCATIONS.length)];
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - spawnTime > LIFETIME) {
            active = false;
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        String imageKey = "powerup_" + type.toString().toLowerCase();
        
        if (resourceManager.isImageLoaded(imageKey)) {
            g.drawImage(resourceManager.getImage(imageKey), (int)x, (int)y, null);
        } 

        if ((System.currentTimeMillis() / 200) % 2 == 0 && 
            System.currentTimeMillis() - spawnTime > LIFETIME - 3000) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y, width, height);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    public PowerUpType getType() {
        return type;
    }
}