import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private Set<Integer> pressedKeys;
    private Set<Integer> previousKeys;

    private long lastEscTime;

    private long lastShiftPressTime = 0;
    private static final long SHIFT_COOLDOWN = 200;
    
    public InputHandler() {
        pressedKeys = new HashSet<>();
        previousKeys = new HashSet<>();
        lastEscTime = 0;
    }
    
    public void update() {
        previousKeys = new HashSet<>(pressedKeys);
    }
    
    public void keyPressed(int keyCode) {
        pressedKeys.add(keyCode);
        
        if (keyCode == KeyEvent.VK_ESCAPE) {
            lastEscTime = System.currentTimeMillis();
        }
    }
    
    public void keyReleased(int keyCode) {
        pressedKeys.remove(keyCode);
    }
    
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    public boolean isKeyJustPressed(int keyCode) {
        return pressedKeys.contains(keyCode) && !previousKeys.contains(keyCode);
    }
    
    public boolean isUpPressed() {
        return isKeyPressed(KeyEvent.VK_W) || isKeyPressed(KeyEvent.VK_UP);
    }
    
    public boolean isDownPressed() {
        return isKeyPressed(KeyEvent.VK_S) || isKeyPressed(KeyEvent.VK_DOWN);
    }
    
    public boolean isLeftPressed() {
        return isKeyPressed(KeyEvent.VK_A) || isKeyPressed(KeyEvent.VK_LEFT);
    }
    
    public boolean isRightPressed() {
        return isKeyPressed(KeyEvent.VK_D) || isKeyPressed(KeyEvent.VK_RIGHT);
    }
    
    public boolean isStartPressed() {
        return isKeyJustPressed(KeyEvent.VK_ENTER);
    }
    
    public boolean isFireJustPressed() {
        return isKeyJustPressed(KeyEvent.VK_SPACE);
    }
    
    public boolean isRestartPressed() {
        return isKeyJustPressed(KeyEvent.VK_R);
    }
    
    public boolean isEscapePressed() {
        boolean escPressed = isKeyJustPressed(KeyEvent.VK_ESCAPE) || 
                            (System.currentTimeMillis() - lastEscTime < 300);
        
        if (escPressed) {
            lastEscTime = 0;
            return true;
        }
        return false;
    }

    public boolean isShiftActionTriggered() {
        long currentTime = System.currentTimeMillis();
        
        if (isKeyPressed(KeyEvent.VK_SHIFT) && currentTime - lastShiftPressTime > SHIFT_COOLDOWN) {
            lastShiftPressTime = currentTime;
            return true;
        }
        return false;
    }
}
