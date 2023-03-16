/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(7800);
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

            while (true) {
                byte[] data = new byte[13];
                try {
                    inputStream.readFully(data);
                } catch (EOFException e) {
                    break;
                }
                ByteBuffer buffer = ByteBuffer.wrap(data);
                int dx = buffer.getInt();
                int dy = buffer.getInt();
                boolean leftButton = buffer.get() != 0;
                boolean rightButton = buffer.get() != 0;
                //System.out.println("Received x: " + dx + ", y: " + dy + ", left: " + leftButton + ", right: " + rightButton);

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

                /* if (leftButton) {
                    if (!isLeftButtonDown) {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        isLeftButtonDown = true;
                    }
                } else {
                    if (isLeftButtonDown) {
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        isLeftButtonDown = false;
                    }
                }

                if (rightButton) {
                    if (!isRightButtonDown) {
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        isRightButtonDown = true;
                    }
                } else {
                    if (isRightButtonDown) {
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        isRightButtonDown = false;
                    }
                }*/
                //System.out.println("Received x: " + dx + ", y: " + dy + ", left: " + leftButton + ", right: " + rightButton);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
