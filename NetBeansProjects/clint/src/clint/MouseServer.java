
package clint;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitch
 */
public class MouseServer {

    private static  int PORT = 8000;
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            PORT = port;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(socket));
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MouseServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ClientHandler implements Runnable {

    private Socket socket;
    private boolean isLeftButtonDown = false;
    private boolean isRightButtonDown = false; 
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            Robot robot = new Robot();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            boolean running = true;
            while (running) {
                byte[] data = new byte[14];
                try {
                    inputStream.readFully(data);
                } catch (EOFException e) {
                    break;
                }
                int identifier = data[0];                
                ByteBuffer buffer = ByteBuffer.wrap(data,1,13);
                if(identifier == 1){
                    int dx = buffer.getInt();
                    int dy = buffer.getInt();
                    boolean leftButton = buffer.get() != 0;
                    boolean rightButton = buffer.get() != 0;
                    //System.out.println("Received x: " + dx + ", y: " + dy + ", left: " + leftButton + ", right: " + rightButton);
                    if(dx == 0 && dy == 0 && leftButton == true && rightButton == true){
                        running = false;
                        break;
                    }
                    //get the current mouse position
                    Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();

                    // add the difference to the current mouse position to get the new mouse position
                    Point newMousePosition = new Point(currentMousePosition.x + dx, currentMousePosition.y + dy);
                    robot.mouseMove(newMousePosition.x, newMousePosition.y);
                    // handle mouse button events
                    if (leftButton) {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

                    }
                    else {
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    }

                    if (rightButton) {
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);

                    } else {
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                    }
                }
                else if(identifier == 2){
                    
                    int keycode = buffer.getInt();
                    System.out.println("KeyCode Received"+keycode);
                    robot.keyPress(keycode);
                    robot.keyRelease(keycode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socket.close();
                System.out.println("Connection closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
