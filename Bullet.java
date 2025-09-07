import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Bullet extends GameObject {
   private static final float BULLET_SPEED = 5.0f; 
    private static final float FAST_BULLET_MULTIPLIER = 1.5f; 

    private boolean fromPlayer;
    private ResourceManager resourceManager;
    private BufferedImage bulletImage;
    private boolean fastBullet;
    private boolean steelPiercing;
    
    public Bullet(float x, float y, Direction direction, boolean fromPlayer, ResourceManager resourceManager, boolean fastBullet, boolean steelPiercing) {
        super(x, y, 8, 8);
        this.direction = direction;
        this.fromPlayer = fromPlayer;
        this.resourceManager = resourceManager;
        this.fastBullet = fastBullet;
        this.steelPiercing = steelPiercing;
        
        String bulletImageKey = getBulletKeyForDirection(direction);
        this.bulletImage = resourceManager.getImage(bulletImageKey);
    }

    public Bullet(float x, float y, Direction direction, boolean fromPlayer, ResourceManager resourceManager) {
        this(x, y, direction, fromPlayer, resourceManager, false, false);
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public boolean isSteelPiercing() {
        return steelPiercing;
    }

    private String getBulletKeyForDirection(Direction direction) {
        switch (direction) {
            case UP:
                return "bullet_up";
            case DOWN:
                return "bullet_down";
            case LEFT:
                return "bullet_left";
            case RIGHT:
                return "bullet_right";
            default:
                return "bullet";
        }
    }
    
    @Override
    public void update() {
        float speed = fastBullet ? BULLET_SPEED * FAST_BULLET_MULTIPLIER : BULLET_SPEED;
        
        switch (direction) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
                break;
        }

        int playableAreaStart = Level.getPlayableAreaStart() * Level.CELL_SIZE;
        int playableAreaEnd = (Level.getPlayableAreaStart() + Level.getPlayableAreaSize()) * Level.CELL_SIZE;
        
        if (x < playableAreaStart || x > playableAreaEnd - 1 || 
            y < playableAreaStart || y > playableAreaEnd - 1) {
            active = false;
        }
        updateBounds();
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!active) { 
        return;
        }
        
        if (bulletImage != null) {
            g.drawImage(bulletImage, (int)x, (int)y, null);
        }
    }
}