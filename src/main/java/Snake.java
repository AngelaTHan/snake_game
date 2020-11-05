import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;


public class Snake {
    private static final double BLOCK_WIDTH = 40;
    private static final double MIN_X = 40;
    private static final double MIN_Y = 120;
    private static final double MAX_X = 1200;
    private static final double MAX_Y = 720;
    private static final double START_X = 160;
    private static final double START_Y = 680;
    private static final int START_LENGTH = 3;

    private int speed;
    private int length;
    private DIRECTION direction;
    private List<Rectangle> snake;

    enum DIRECTION {UP, DOWN, LEFT, RIGHT};

    private DIRECTION bodyDirection(double goalX, double goalY, DIRECTION goalDirection, double currentX, double currentY) {
        DIRECTION dir;

        if (goalX == currentX) {
            if (goalY < currentY) {
                dir = DIRECTION.UP;
            } else {
                dir = DIRECTION.DOWN;
            }
        } else if (goalY == currentY) {
            if (goalX < currentX) {
                dir = DIRECTION.LEFT;
            } else {
                dir = DIRECTION.RIGHT;
            }
        } else if (goalX > currentX && goalY > currentY) {
            if (goalDirection == DIRECTION.DOWN) {
                dir = DIRECTION.RIGHT;
            } else {
                dir = DIRECTION.DOWN;
            }
        } else if (goalX < currentX && goalY < currentY) {
            if (goalDirection == DIRECTION.UP) {
                dir = DIRECTION.LEFT;
            } else {
                dir = DIRECTION.UP;
            }
        } else if (goalX > currentX && goalY < currentY) {
            if (goalDirection == DIRECTION.UP) {
                dir = DIRECTION.RIGHT;
            } else {
                dir = DIRECTION.UP;
            }
        } else {
            if (goalDirection == DIRECTION.DOWN) {
                dir = DIRECTION.LEFT;
            } else {
                dir = DIRECTION.DOWN;
            }
        }

        return dir;
    }

    private double[] nextMove(DIRECTION dir, double x, double y) {
        double nextX, nextY;
        double delta = speed;

        if (dir == DIRECTION.UP) {
            nextX = x;
            nextY = y - delta;
        } else if (dir == DIRECTION.DOWN) {
            nextX = x;
            nextY = y + delta;
        } else if (dir == DIRECTION.LEFT) {
            nextX = x - delta;
            nextY = y;
        } else {
            nextX = x + delta;
            nextY = y;
        }

        double[] next = {nextX, nextY};
        return next;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public DIRECTION getDirection() {
        return direction;
    }

    public void setDirection(DIRECTION direction) {
        this.direction = direction;
    }

    public List<Rectangle> getSnake() {
        return snake;
    }

    public Snake() {
        Rectangle head = new Rectangle(BLOCK_WIDTH, BLOCK_WIDTH, Color.BLUE);
        Rectangle body = new Rectangle(BLOCK_WIDTH, BLOCK_WIDTH, Color.MEDIUMSLATEBLUE);
        Rectangle tail = new Rectangle(BLOCK_WIDTH, BLOCK_WIDTH, Color.MEDIUMSLATEBLUE);

        speed = 0;
        length = START_LENGTH;
        direction = DIRECTION.RIGHT;
        snake = new ArrayList<Rectangle>(Arrays.asList(head, body, tail));

        for (int i=0; i<START_LENGTH; i++) {
            Rectangle current = snake.get(i);
            current.setArcHeight(20);
            current.setArcWidth(20);
            current.setX(START_X-i*BLOCK_WIDTH);
            current.setY(START_Y);
        }
    }

    public Rectangle grow() {
        double x, y;
        Rectangle tail = snake.get(length-1);
        double tailX = tail.getX();
        double tailY = tail.getY();
        Rectangle previous = snake.get(length-2);
        double prevX = previous.getX();
        double prevY = previous.getY();
        DIRECTION dir = bodyDirection(prevX, prevY, direction, tailX, tailY); //direction should never gets used because snake only grow when blocks are centered at cell

        Rectangle body = new Rectangle(BLOCK_WIDTH, BLOCK_WIDTH, Color.MEDIUMSLATEBLUE);
        body.setArcHeight(20);
        body.setArcWidth(20);

        if (dir == DIRECTION.UP) {
            if (tailY < MAX_Y) {
                x = tailX;
                y = tailY + BLOCK_WIDTH;
            } else if (tailX > MIN_X) {
                x = tailX - BLOCK_WIDTH;
                y = tailY;
            } else {
                x = tailX + BLOCK_WIDTH;
                y = tailY;
            }
        } else if (dir == DIRECTION.DOWN) {
            if (tailY > MIN_Y) {
                x = tailX;
                y = tailY - BLOCK_WIDTH;
            } else if (tailX > MIN_X) {
                x = tailX - BLOCK_WIDTH;
                y = tailY;
            } else {
                x = tailX + BLOCK_WIDTH;
                y = tailY;
            }
        } else if (dir == DIRECTION.LEFT) {
            if (tailX < MAX_X) {
                x = tailX + BLOCK_WIDTH;
                y = tailY;
            } else if (tailY > MIN_Y) {
                x = tailX;
                y = tailY - BLOCK_WIDTH;
            } else {
                x = tailX;
                y = tailY + BLOCK_WIDTH;
            }
        } else {
            if (tailX > MIN_X) {
                x = tailX - BLOCK_WIDTH;
                y = tailY;
            } else if (tailY > MIN_Y) {
                x = tailX;
                y = tailY - BLOCK_WIDTH;
            } else {
                x = tailX;
                y = tailY + BLOCK_WIDTH;
            }
        }

        body.setX(x);
        body.setY(y);
        snake.add(body);
        length++;

        return body;
    }

    public void move() throws Exception {
        double prevX, prevY;
        DIRECTION prevDir;

        Rectangle head = snake.get(0);
        double[] nextHead = nextMove(direction, head.getX(), head.getY());
        double nextHeadX = nextHead[0];
        double nextHeadY = nextHead[1];

        if (nextHeadX < MIN_X || nextHeadX > MAX_X || nextHeadY < MIN_Y || nextHeadY > MAX_Y) {
            throw new Exception("Snake hit the wall. Game over");
        }

        prevX = nextHeadX;
        prevY = nextHeadY;
        prevDir = direction;

        // update X,Y's for the snake
        for (Rectangle block : snake) {
            double x = block.getX();
            double y = block.getY();
            DIRECTION dir = bodyDirection(prevX, prevY, prevDir, x, y);
            double[] blockMove = nextMove(dir, x, y);
            block.setX(blockMove[0]);
            block.setY(blockMove[1]);
            prevX = x;
            prevY = y;
            prevDir = dir;
        }

        // check for self bite
        for (int i=2; i<length; i++) {
            if (snake.get(i).contains(nextHeadX, nextHeadY)) {
                throw new Exception("Snake bites itself. Game over.");
            }
        }
    }

    public int[] eatApple(ImageView[][] appleMap) {
        Rectangle head = snake.get(0);
        double headX = head.getX();
        double headY = head.getY();
        if ((headX % BLOCK_WIDTH == 0) && (headY % BLOCK_WIDTH == 0)) {
            int xPos = (int) ((headX == MIN_X) ? 0 : (headX - MIN_X) / BLOCK_WIDTH);
            int yPos = (int) ((headY == MIN_Y) ? 0 : (headY - MIN_Y) / BLOCK_WIDTH);
            if (appleMap[xPos][yPos] != null) {
                int[] pos = {xPos, yPos};
                return pos;
            }
        }
        int[] notFound = {-1, -1};
        return notFound;
    }

    public void turnLeft() {
        if (direction == DIRECTION.UP) {
            direction = DIRECTION.LEFT;
        } else if (direction == DIRECTION.DOWN) {
            direction = DIRECTION.RIGHT;
        } else if (direction == DIRECTION.LEFT) {
            direction = DIRECTION.DOWN;
        } else {
            direction = DIRECTION.UP;
        }
    }

    public void turnRight() {
        if (direction == DIRECTION.UP) {
            direction = DIRECTION.RIGHT;
        } else if (direction == DIRECTION.DOWN) {
            direction = DIRECTION.LEFT;
        } else if (direction == DIRECTION.LEFT) {
            direction = DIRECTION.UP;
        } else {
            direction = DIRECTION.DOWN;
        }
    }
}
