
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;
    boolean gameStarted = false;
    int highScore = 0;
    javax.sound.sampled.Clip bgClip;
    boolean musicStopped = false;
    boolean soundPlayed = false;

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //bird class
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //scaled by 1/6
    int pipeHeight = 512;
    
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //game logic
    Bird bird;
    int velocityX = -4; //move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; //move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();
    Image foodImg;
    ArrayList<Food> foods = new ArrayList<>();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double yourScore = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();

      //load images
        backgroundImg = new ImageIcon(getClass().getResource("/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/bottompipe.png")).getImage();
        foodImg = new ImageIcon(getClass().getResource("/food.png")).getImage();
        
        //High score
        loadHighScore();
        
        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //place pipes timer
        placePipeTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Code to be executed
              placePipes();
            }
        });
        
		//game timer
		gameLoop = new Timer(1000/60, this); //how long it takes to start timer, milliseconds gone between frames 
		//new Timer(3000, e -> placeFood()).start(); // every 3 sec
	}
  
    void loadHighScore() {
        try {
            java.io.File file = new java.io.File("highscore.txt");
            if (file.exists()) {
                java.util.Scanner sc = new java.util.Scanner(file);
                highScore = sc.nextInt();
                sc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveHighScore() {
        try {
            java.io.FileWriter fw = new java.io.FileWriter("highscore.txt");
            fw.write(String.valueOf(highScore));
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void placePipes() {
        //(0-1) * pipeHeight/2.
        // 0 -> -128 (pipeHeight/4)
        // 1 -> -128 - 256 (pipeHeight/4 - pipeHeight/2) = -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;
    
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
    
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
        placeFood();
    }
    
    // place food
    void placeFood() {
        Food f = new Food(foodImg);
        f.x = boardWidth; // right edge

        boolean validY = false;
        int attempts = 0;

        while(!validY && attempts < 10){
            attempts++;
            int y = 50 + random.nextInt(boardHeight - 100); // margin

            validY = true;
            for(Pipe pipe : pipes){
                // pipe ke rectangle ke andar na ho
                if(y + f.height > pipe.y && y < pipe.y + pipe.height){
                    validY = false;
                    break;
                }
            }

            if(validY){
                f.y = y;
            }
        }

        foods.add(f);
    }
    
    // bird food
    class Food {
        int x;
        int y;
        int width = 30;
        int height = 30;
        Image img;
        boolean eaten = false;

        Food(Image img) {
            this.img = img;
            this.x = boardWidth;
            this.y = random.nextInt(boardHeight - 100) + 50; // random position
        }
    }
    

    
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        //bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        // food
        for (Food f : foods) {
            if (!f.eaten) {
                g.drawImage(f.img, f.x, f.y, f.width, f.height, null);
            }
        }

        //score
        g.setColor(Color.white);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        
        //Game over
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("GAME OVER", 80, 250);

            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Your Score: " + (int)yourScore, 110, 300);

            g.drawString("Press SPACE to Restart", 50, 350);
        }
        else {
            g.drawString(String.valueOf((int)yourScore), 10, 35);
        }
        if (!gameStarted) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Flappy Bird", 80, 250);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press SPACE to Start", 70, 300);
        }
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("High Score: " + highScore, 10, 60);
        
	}
// background music 
	void playBackgroundMusic() {
	    try {
	        // 🔥 pehle old music band karo
	        if (bgClip != null && bgClip.isRunning()) {
	            bgClip.stop();
	            bgClip.close();
	        }

	        java.net.URL url = getClass().getResource("/bg.wav");

	        if (url == null) {
	            System.out.println("bg.wav not found");
	            return;
	        }

	        javax.sound.sampled.AudioInputStream audio =
	            javax.sound.sampled.AudioSystem.getAudioInputStream(url);

	        bgClip = javax.sound.sampled.AudioSystem.getClip();
	        bgClip.open(audio);
	        bgClip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	// stop music 
	void stopBackgroundMusic() {
	    if (bgClip != null) {
	        bgClip.stop();
	        bgClip.flush();   
	        bgClip.close();  
	        bgClip=null;
	    }
	}
	
    public void move() {
        //bird
    	    if (!gameStarted) return;

    	    velocityY += gravity;
    	    bird.y += velocityY;
            bird.y = Math.max(bird.y, 0); //apply gravity to current bird.y, limit the bird.y to top of the canvas

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                yourScore += 0.5; //0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe) && !soundPlayed) {
                gameOver = true;
                playSound("hit.wav");
                soundPlayed = true;
            }
        }
        // food
        for (Food f : foods) {
            f.x += velocityX;

            // collision with bird
            if (!f.eaten &&
                bird.x < f.x + f.width &&
                bird.x + bird.width > f.x &&
                bird.y < f.y + f.height &&
                bird.y + bird.height > f.y) {

                yourScore += 10;   // 🔥 BONUS
                f.eaten = true;
                playSound("jump.wav"); // ya alag sound use kar sakta hai
            }
        }

        // top boundary
        if (bird.y <= 0 && !soundPlayed) {
            gameOver = true;
            playSound("hit.wav");
            soundPlayed = true;
        }

        // bottom boundary
        if (bird.y + bird.height >= boardHeight && !soundPlayed) {
            gameOver = true;
            playSound("hit.wav");
            soundPlayed = true;
        }
        
        //  save high score
        if (yourScore > highScore) {
            highScore = (int) yourScore;
            saveHighScore();
        }
    }
    
    // play sound 
    
    void playSound(String soundFile) {
        try {
            java.net.URL soundURL = getClass().getResource("/" + soundFile);
            javax.sound.sampled.AudioInputStream audio = javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            stopBackgroundMusic();  
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            // 🔁 RESTART
            if (gameOver) {
                bird.y = boardHeight / 2;
                velocityY = 0;
                pipes.clear();
                yourScore = 0;

                gameOver = false;
                gameStarted = false;   
                soundPlayed = false;
                musicStopped = false;
                gameLoop.stop();
                placePipeTimer.stop();

                repaint();
                return;
            }

            // 🎬 START GAME
            if (!gameStarted) {
                gameStarted = true;
                velocityY = -8;
                playSound("jump.wav");
                playBackgroundMusic();
                gameLoop.start();
                placePipeTimer.start();
            } 
            
            // 🕊️ NORMAL JUMP
            else {
                velocityY = -8;
                playSound("jump.wav");
            }
        }
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
