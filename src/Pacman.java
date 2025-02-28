import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Pacman extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction ='U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction){
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls){
                if (collision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity(){
            if (direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image redGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanRightImage;
    private Image pacmanLeftImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    private HashSet<Block> walls;
    private HashSet<Block> foods;
    private HashSet<Block> ghosts;
    private Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'R', 'L'}; // UP DOWN LEFT RIGHT
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean win = false;

    public Pacman() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();

        initializeGame();
    }

    public void initializeGame() {
        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        
        score = 0;
        lives = 3;
        gameOver = false;
        win = false;
        
        // Start or restart the timer
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') {
                    this.pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw walls
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Draw food
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw ghosts
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Draw Pacman
        if (pacman != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }
        
        // Draw score and lives
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        
        if (gameOver) {
            String message = win ? "You Win! Score: " : "Game Over! Score: ";
            g.drawString(message + String.valueOf(score), tileSize/2, tileSize/2);
            g.drawString("Press ENTER to restart", tileSize/2, tileSize/2 + 25);
        } else {
            g.drawString("Lives: x" + String.valueOf(lives) + "  Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    public void move(){
        // Move pacman
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Teleportation code for pacman
        if (pacman.y == tileSize * 9 && pacman.x == 0) { 
            pacman.x = tileSize * (columnCount - 1); 
        } else if (pacman.y == tileSize * 9 && pacman.x == tileSize * (columnCount - 1)) { 
            pacman.x = 0; 
        }

        // Check wall collision
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Move and check ghost collisions
        for (Block ghost : ghosts) {
            // Move ghosts
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            // Check for wall collisions for ghosts
            boolean ghostCollided = false;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    
                    // Try to find a direction that's not blocked
                    int attempts = 0;
                    boolean foundDirection = false;
                    while (!foundDirection && attempts < 4) {
                        char newDirection = directions[random.nextInt(4)];
                        if (newDirection != ghost.direction) {
                            ghost.updateDirection(newDirection);
                            foundDirection = true;
                        }
                        attempts++;
                    }
                    
                    ghostCollided = true;
                    break;
                }
            }
            
            // Random direction change (10% chance)
            if (!ghostCollided && random.nextInt(100) < 10) {
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
            
            // Teleportation code for ghosts
            if (ghost.y == tileSize * 9 && ghost.x == 0) {
                ghost.x = tileSize * (columnCount - 1); 
            } else if (ghost.y == tileSize * 9 && ghost.x == tileSize * (columnCount - 1)) { 
                ghost.x = 0; 
            }
            
            // Check collision with pacman
            if (collision(ghost, pacman)) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    win = false;
                    gameLoop.stop();
                } else {
                    // Reset positions
                    resetPositions();
                }
            }
        }
        
        // Food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }
        
        // Check if all food has been eaten
        if (foods.isEmpty()) {
            gameOver = true;
            win = true;
            gameLoop.stop();
        }
    }
    
    // Reset positions method
    private void resetPositions() {
        pacman.x = pacman.startX;
        pacman.y = pacman.startY;
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        pacman.direction = 'R';
        pacman.image = pacmanRightImage;
        
        for (Block ghost : ghosts) {
            ghost.x = ghost.startX;
            ghost.y = ghost.startY;
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }
    
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
            a.x + a.width > b.x &&
            a.y < b.y + b.height &&
            a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
        }
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            initializeGame();
            return;
        }
        
        if (gameOver) return;
        
        //System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction =='U'){
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction =='D'){
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction =='L'){
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction =='R'){
            pacman.image = pacmanRightImage;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pacman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new Pacman());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}