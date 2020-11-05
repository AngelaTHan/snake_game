import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board extends Application {
    private static final Integer TIMER = 30;
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 800;
    private static final int GRID_WIDTH = 40;
    private static final int GRID_X = 40;
    private static final int GRID_Y = 120;
    private static final int ROWS = 16;
    private static final int COLUMNS = 30;
    private static final int LEVEL_ONE_APPLE = 5;
    private static final int LEVEL_TWO_APPLE = 10;
    private static final int LEVEL_THREE_APPLE = 15;
    private static final int SLOW = 2; //pixel per 40ms
    private static final int FASTER = 4;
    private static final int FAST = 8;
    private static final int FPS = 40;

    private Integer eatonApples = 0;
    private Integer scores = 0;
    private Integer highestScores = 0;
    double timeRemain = TIMER;
    ImageView[][] appleMap;

    Scene splashScreen, levelScreen, gameOver;
    Pane levelOne, levelTwo, levelThree;
    Label levelLabel, scoreLabel, appleLabel, timerLabel, highestScoreLabel;
    Image apple;
    Snake snake;
    List<TURN> turnList;
    Timeline timeline;
    boolean pause;
    SCENES nextLevel;
    enum SCENES {SPLASH, LEVEL1, LEVEL2, LEVEL3, OVER};
    enum TURN {LEFT, RIGHT};

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Snake Game - Welcome");

        drawScenes(stage);

        // timeline
        timeline = new Timeline(FPS);
        int duration = 1000/FPS;
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(duration), event -> {
                    try {
                        // center actions: turn, switch level
                        if ((stage.getScene() == levelScreen)) {
                            boolean turnAvailable = (snake.getSnake().get(0).getX() % GRID_WIDTH == 0)
                                    && (snake.getSnake().get(0).getY() % GRID_WIDTH == 0);

                            // turn
                            if (turnAvailable && !turnList.isEmpty()) {
                                if (turnList.get(0) == TURN.LEFT) {
                                    snake.turnLeft();
                                } else {
                                    snake.turnRight();
                                }
                                turnList.remove(0);
                            }

                            // switch levels
                            if (turnAvailable && nextLevel != null) {
                                if (nextLevel == SCENES.LEVEL1) {
                                    setScene(stage, SCENES.LEVEL1);
                                } else if (nextLevel == SCENES.LEVEL2) {
                                    setScene(stage,SCENES.LEVEL2);
                                } else {
                                    setScene(stage, SCENES.LEVEL3);
                                }
                                nextLevel = null;
                            }
                        }

                        snake.move();

                        // apple and score
                        int[] eatonApplePos = snake.eatApple(appleMap);
                        if ((eatonApplePos[0] != -1) && (stage.getScene() == levelScreen)) {
                            String eatSound = getClass().getClassLoader().getResource("bite.mp3").toString();
                            AudioClip eatClip = new AudioClip(eatSound);
                            eatClip.play();
                            Rectangle newBody = snake.grow();
                            if (stage.getScene().getRoot() == levelOne) {
                                levelOne.getChildren().remove(appleMap[eatonApplePos[0]][eatonApplePos[1]]);
                                drawRandomApple(levelOne);
                                levelOne.getChildren().add(newBody);
                                scores++;
                            } else if (stage.getScene().getRoot() == levelTwo) {
                                levelTwo.getChildren().remove(appleMap[eatonApplePos[0]][eatonApplePos[1]]);
                                drawRandomApple(levelTwo);
                                levelTwo.getChildren().add(newBody);
                                scores += 2;
                            } else {
                                levelThree.getChildren().remove(appleMap[eatonApplePos[0]][eatonApplePos[1]]);
                                drawRandomApple(levelThree);
                                levelThree.getChildren().add(newBody);
                                scores += 3;
                            }
                            appleMap[eatonApplePos[0]][eatonApplePos[1]] = null;
                            eatonApples++;
                            appleLabel.setText(eatonApples.toString());
                            scoreLabel.setText(scores.toString());
                        }

                        // timer
                        if (stage.getScene().getRoot() == levelOne || stage.getScene().getRoot() == levelTwo) {
                            timeRemain -= 0.025;
                            timerLabel.setText(Integer.toString((int) timeRemain));

                            boolean endAvailable = (snake.getSnake().get(0).getX() % GRID_WIDTH == 0)
                                    && (snake.getSnake().get(0).getY() % GRID_WIDTH == 0);

                            if (endAvailable && stage.getScene().getRoot() == levelOne && (int) timeRemain == 0) {
                                setScene(stage, SCENES.LEVEL2);
                            } else if (endAvailable && stage.getScene().getRoot() == levelTwo && (int) timeRemain == 0) {
                                setScene(stage, SCENES.LEVEL3);
                            }
                        }

                    } catch (Exception e) {
                        setScene(stage, SCENES.OVER);
                    }
                })
        );

        timeline.playFromStart();
        setScene(stage, SCENES.SPLASH);
        stage.show();
    }

    private void setScene(Stage stage, SCENES scene) {
        switch(scene) {
            case SPLASH:
                stage.setTitle("Snake Game - Welcome");
                drawScenes(stage);
                timeline.play();
                stage.setScene(splashScreen);
                break;
            case LEVEL1:
                stage.setTitle("Snake Game - Level 1");
                levelOne = drawLevel(SCENES.LEVEL1);
                levelOne.getChildren().addAll(snake.getSnake());
                levelScreen.setRoot(levelOne);
                stage.setScene(levelScreen);
                snake.setSpeed(SLOW);
                break;
            case LEVEL2:
                stage.setTitle("Snake Game - Level 2");
                levelTwo = drawLevel(SCENES.LEVEL2);
                levelTwo.getChildren().addAll(snake.getSnake());
                levelScreen.setRoot(levelTwo);
                stage.setScene(levelScreen);
                snake.setSpeed(FASTER);
                break;
            case LEVEL3:
                stage.setTitle("Snake Game - Level 3");
                levelThree = drawLevel(SCENES.LEVEL3);
                levelThree.getChildren().addAll(snake.getSnake());
                levelScreen.setRoot(levelThree);
                stage.setScene(levelScreen);
                snake.setSpeed(FAST);
                break;
            case OVER:
                stage.setTitle("Snake Game - Game Over");
                highestScores = (scores > highestScores) ? scores : highestScores;
                highestScoreLabel.setText(highestScores.toString());
                String gameoverSound = getClass().getClassLoader().getResource("gameover.mp3").toString();
                AudioClip gameoverClip = new AudioClip(gameoverSound);
                gameoverClip.play();
                stage.setScene(gameOver);
                timeline.pause();
                break;
        }
    }

    private Pane drawLevel(SCENES level) {
        eatonApples = 0;
        appleMap = new ImageView[COLUMNS][ROWS];

        Pane root = new Pane();
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));

        // draw grid
        for (int i=0; i<COLUMNS; i++) {
            for (int j=0; j<ROWS; j++) {
                Rectangle rectangle = new Rectangle(GRID_WIDTH, GRID_WIDTH);
                rectangle.setX(GRID_X +i* GRID_WIDTH);
                rectangle.setY(GRID_Y +j* GRID_WIDTH);
                if ((i+j) == 0 || (i+j)%2 == 0) {
                    rectangle.setFill(Color.LIMEGREEN);
                } else {
                    rectangle.setFill(Color.LIGHTGREEN);
                }
                root.getChildren().add(rectangle);
            }
        }

        // draw game title
        levelLabel = new Label();
        levelLabel.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));
        Image trophy = new Image("trophy.png", GRID_WIDTH, GRID_WIDTH, false, true);
        ImageView trophyView = new ImageView(trophy);
        scoreLabel = new Label();
        scoreLabel.setText(scores.toString());
        scoreLabel.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));
        apple = new Image("apple.png", GRID_WIDTH, GRID_WIDTH, false, true);
        ImageView appleView = new ImageView(apple);
        appleLabel = new Label();
        appleLabel.setText(eatonApples.toString());
        appleLabel.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));
        Image timer = new Image("timer.png", GRID_WIDTH, GRID_WIDTH, false, true);
        ImageView timerView = new ImageView(timer);
        timerLabel = new Label();
        timerLabel.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));

        HBox gameTitle = new HBox();
        gameTitle.setPadding(new Insets(GRID_X));
        gameTitle.setSpacing(30.0);

        if (level == SCENES.LEVEL1) {
            // title
            timeRemain = TIMER;
            timerLabel.setText(Integer.toString((int) timeRemain));
            levelLabel.setText("Level 1");
            gameTitle.getChildren().addAll(levelLabel, trophyView, scoreLabel, appleView, appleLabel, timerView, timerLabel);
            root.getChildren().add(gameTitle);

            // apple
            int[] applesX = {80, 480, 600, 640, 800};
            int[] applesY = {280, 640, 120, 320, 200};
            for (int i = 0; i< LEVEL_ONE_APPLE; i++) {
                drawApple(root, applesX[i], applesY[i]);
            }
        } else if (level == SCENES.LEVEL2) {
            // title
            timeRemain = TIMER;
            timerLabel.setText(Integer.toString((int) timeRemain));
            levelLabel.setText("Level 2");
            gameTitle.getChildren().addAll(levelLabel, trophyView, scoreLabel, appleView, appleLabel, timerView, timerLabel);
            root.getChildren().add(gameTitle);

            // apple
            int[] applesX = {80, 160, 280, 440, 520, 640, 760, 800, 920, 960};
            int[] applesY = {280, 120, 240, 640, 120, 600, 680, 320, 560, 200};
            for (int i = 0; i< LEVEL_TWO_APPLE; i++) {
                drawApple(root, applesX[i], applesY[i]);
            }
        } else if (level == SCENES.LEVEL3) {
            // title
            levelLabel.setText("Level 3");
            gameTitle.getChildren().addAll(levelLabel, trophyView, scoreLabel, appleView, appleLabel);
            root.getChildren().add(gameTitle);

            // apple
            int[] applesX = {80, 120, 280, 280, 320, 440, 520, 640, 760, 880, 920, 960, 1080, 1120, 1200};
            int[] applesY = {680, 240, 720, 280, 120, 240, 640, 120, 600, 440, 320, 360, 560, 200, 240};
            for (int i = 0; i< LEVEL_THREE_APPLE; i++) {
                drawApple(root, applesX[i], applesY[i]);
            }
        }

        return root;
    }

    private void drawApple(Pane root, int x, int y) {
        ImageView appleOnGrid = new ImageView(apple);
        appleOnGrid.setX(x);
        appleOnGrid.setY(y);
        root.getChildren().add(appleOnGrid);
        appleMap[xPos(x)][yPos(y)] = appleOnGrid;
    }

    private int xPos(int x) {
        return (x== GRID_X) ? 0 : (x - GRID_X)/ GRID_WIDTH;
    }

    private int yPos(int y) {
        return (y== GRID_Y) ? 0 : (y - GRID_Y)/ GRID_WIDTH;
    }

    private int randomX() {
        Random x = new Random();
        return x.nextInt(COLUMNS) * GRID_WIDTH + GRID_X;
    }

    private int randomY() {
        Random y = new Random();
        return y.nextInt(ROWS) * GRID_WIDTH + GRID_Y;
    }

    private void drawRandomApple(Pane root) {
        while (true) {
            int x = randomX();
            int y = randomY();
            if (appleMap[xPos(x)][yPos(y)] == null) {
                drawApple(root, x, y);
                break;
            }
        }
    }

    private void drawScenes(Stage stage) {
        // splash
        pause = false;
        scores = 0;

        Image welcomeSnake = new Image("snake1.png", SCREEN_WIDTH, SCREEN_HEIGHT, false, true);
        ImageView welcomeSnakeView = new ImageView(welcomeSnake);
        welcomeSnakeView.setOpacity(0.20);
        Text title = new Text("SNAKE GAME\n\n"
                + "     Press `enter` to start the game\n\n\n");
        title.setFont(Font.font("Chalkduster", FontWeight.BOLD, 40));
        title.setFill(Color.GREEN);
        Text username = new Text("User Name: Tingqian Han\n"
                + "User ID: 20705652\n\n"
                + "How to play: \n"
                + "     Press `left` or `right` arrow key to make the snake turn. \n"
                + "     Eat as many apples as possible without letting the snake collide with any walls or bite itself.");
        username.setFont(Font.font("Chalkboard", 20));

        VBox welcomeMsg = new VBox();
        welcomeMsg.setPadding(new Insets(100));
        welcomeMsg.getChildren().addAll(title, username);

        Pane rootSplash = new Pane(welcomeSnakeView, welcomeMsg);
        rootSplash.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        splashScreen = new Scene(rootSplash, SCREEN_WIDTH, SCREEN_HEIGHT);

        // pressing botton to switch scenes
        splashScreen.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.DIGIT1) {
                setScene(stage, SCENES.LEVEL1);
            } else if (event.getCode() == KeyCode.DIGIT2) {
                setScene(stage, SCENES.LEVEL2);
            } else if (event.getCode() == KeyCode.DIGIT3) {
                setScene(stage, SCENES.LEVEL3);
            } else if (event.getCode() == KeyCode.Q) {
                setScene(stage, SCENES.OVER);
            }
        });

        // levels
        Pane rootLevel = drawLevel(SCENES.LEVEL1);
        snake = new Snake();
        rootLevel.getChildren().addAll(snake.getSnake());
        levelScreen = new Scene(rootLevel, SCREEN_WIDTH, SCREEN_HEIGHT);
        turnList = new ArrayList<TURN>();

        // pressing button to switch scenes
        levelScreen.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.R) {
                setScene(stage, SCENES.SPLASH);
            } else if (event.getCode() == KeyCode.DIGIT1) {
                nextLevel = SCENES.LEVEL1;
            } else if (event.getCode() == KeyCode.DIGIT2) {
                nextLevel = SCENES.LEVEL2;
            } else if (event.getCode() == KeyCode.DIGIT3) {
                nextLevel = SCENES.LEVEL3;
            } else if (event.getCode() == KeyCode.Q) {
                setScene(stage, SCENES.OVER);
            } else if (event.getCode() == KeyCode.LEFT) {
                turnList.add(TURN.LEFT);
            } else if (event.getCode() == KeyCode.RIGHT) {
                turnList.add(TURN.RIGHT);
            } else if (event.getCode() == KeyCode.P) {
                if (!pause) {
                    pause = true;
                    timeline.pause();
                } else {
                    pause = false;
                    timeline.play();
                }
            }
        });

        // gameover
        Image endSnake = new Image("snake2.png", SCREEN_WIDTH, SCREEN_HEIGHT, false, true);
        ImageView endSnakeView = new ImageView(endSnake);
        endSnakeView.setOpacity(0.20);
        Text ending = new Text("GAME OVER\n\n"
                + "     Press `r` to restart\n\n\n");
        ending.setFont(Font.font("Chalkduster", FontWeight.BOLD, 50));
        ending.setFill(Color.RED);
        Text highScore = new Text("Highest Score:");
        highScore.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));
        highestScoreLabel = new Label(highestScores.toString());
        highestScoreLabel.setFont(Font.font("Chalkboard", FontWeight.BOLD, 30));

        VBox endMsg = new VBox();
        endMsg.setPadding(new Insets(200));
        endMsg.getChildren().addAll(ending, highScore, highestScoreLabel);

        Pane rootOver = new Pane(endSnakeView, endMsg);
        rootOver.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        gameOver = new Scene(rootOver, SCREEN_WIDTH, SCREEN_HEIGHT);

        // pressing button to switch scenes
        gameOver.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.R) {
                setScene(stage, SCENES.SPLASH);
            }
        });
    }
}
