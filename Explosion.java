import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Explosion extends GameObject {
    private static final int FRAME_DURATION = 80; 
    
    private BufferedImage[] frames;
    private int currentFrame;
    private long lastFrameTime;
    private ResourceManager resourceManager;
    
    
    public Explosion(float x, float y, ResourceManager resourceManager) {
        super(x, y, 32, 32); 
        this.resourceManager = resourceManager;
        

        frames = new BufferedImage[5]; 
        frames[0] = resourceManager.getImage("explosion_small");
        frames[1] = resourceManager.getImage("explosion_medium");
        frames[2] = resourceManager.getImage("explosion_large");
        frames[3] = resourceManager.getImage("explosion_xl");
        frames[4] = resourceManager.getImage("explosion_xxl");
        
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }
    
    @Override
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastFrameTime > FRAME_DURATION) {
            currentFrame++;
            lastFrameTime = currentTime;
            
            if (currentFrame >= frames.length) {
                setActive(false);
            }
        }
    }
    
    @Override
public void render(Graphics2D g) {
    if (!isActive() || currentFrame >= frames.length) {
        return;
    }
    
    BufferedImage frame = frames[currentFrame];
    if (frame != null) {
        g.drawImage(frame, (int)x - frame.getWidth()/2, (int)y - frame.getHeight()/2, null);
    } else {
        System.out.println("HATA");
        }
    }

}