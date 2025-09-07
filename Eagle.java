import java.awt.*;

public class Eagle extends GameObject {
    private ResourceManager resourceManager;
    private boolean destroyed = false;
    
    public Eagle(float x, float y, ResourceManager resourceManager) {
        super(x, y, 32, 32);
        this.resourceManager = resourceManager;
    }
    
    @Override
    public void update() {}
    
    @Override
    public void render(Graphics2D g) {
        if (destroyed) {
            if (resourceManager.isImageLoaded("eagle_destroyed")) {
                g.drawImage(resourceManager.getImage("eagle_destroyed"), (int)x, (int)y, null);
            } 
        } 
        else {
            if (resourceManager.isImageLoaded("eagle")) {
                g.drawImage(resourceManager.getImage("eagle"), (int)x, (int)y, null);
            }
        }
    }
    
    public void destroy() {
        this.destroyed = true;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
    public boolean blocksMovement() {
        return true;
    }
}