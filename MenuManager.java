import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

public class MenuManager {
    private static final int MENU_ITEMS = 14;
    private static final String[] LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
    
    private int selectedIndex = 0;
    private BufferedImage arrowImage;
    private int screenWidth;
    private int screenHeight;
    
    public MenuManager(ResourceManager resourceManager,int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        resourceManager.loadArrowImage();
        resourceManager.loadLogoImage();

        if (resourceManager.isImageLoaded("arrow")) {
            arrowImage = resourceManager.getImage("arrow");
        } else {
            arrowImage = null;
        }
    }
    
    public void update(InputHandler inputHandler) {
        if (inputHandler.isShiftActionTriggered()) {
            selectedIndex = (selectedIndex + 1) % MENU_ITEMS;
        }
        
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            selectedIndex = (selectedIndex + 1) % MENU_ITEMS;
        }
        
        if (inputHandler.isKeyJustPressed(KeyEvent.VK_UP)) {
            selectedIndex = (selectedIndex - 1 + MENU_ITEMS) % MENU_ITEMS;
        }
    }

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Courier", Font.BOLD, 16));
        String copyright = "© V.S 1990.2   15→28";
        int copyrightWidth = g.getFontMetrics().stringWidth(copyright);
        g.drawString(copyright, screenWidth/2 - copyrightWidth/2, 30);
        
        g.drawLine(screenWidth/2 - 150, 40, screenWidth/2 + 150, 40);
        
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Courier", Font.BOLD, 18));
        
        int tankX = screenWidth/2 - 50; 
        int letterX = screenWidth/2 + 25; 
        int arrowX = tankX - 35; 
        
        for (int i = 0; i < MENU_ITEMS; i++) {
            int y = 150 + i * 24;
            
            g.drawString("TANK", tankX, y);
            
            g.drawString(LETTERS[i], letterX, y);
            
            if (i == selectedIndex) {
                if (arrowImage != null) {
                    g.drawImage(arrowImage, arrowX, y - 12, 20, 20, null);
                } else {
                    g.drawString("→", arrowX + 10, y);
                }
            }
        }
        }

    public int getSelectedIndex() {
        return selectedIndex;
    }

}