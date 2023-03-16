package clint;


import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ScreenSender1 {
    private static final int PORT = 8000;
    private static final int BUFFER_SIZE = 65536;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) throws IOException {
        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRectangle = new Rectangle(screenSize);
            Robot robot = new Robot();
            
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Listening on port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                    threadPool.execute(() -> {
                        try {
                            while (true) {
                                BufferedImage image = robot.createScreenCapture(screenRectangle);
                                BufferedImage rotatedImage = rotateImage(image);
                                
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
                                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {
                                    objectOutputStream.writeObject(getBytes(rotatedImage));
                                }
                                
                                clientSocket.getOutputStream().write(byteArrayOutputStream.toByteArray());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ScreenSender1.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
            }        } catch (AWTException ex) {
            Logger.getLogger(ScreenSender1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static byte[] getBytes(BufferedImage image) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        ImageIO.write(image, "jpeg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        bytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return bytes;
    }

    private static BufferedImage rotateImage(BufferedImage image) {
        AffineTransform transform = new AffineTransform();
        transform.translate(image.getHeight() / 2, image.getWidth() / 2);
        transform.rotate(Math.PI / 2);
        transform.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        BufferedImage rotatedImage = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
        Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(image, transform, null);
        g.dispose();
        return rotatedImage;
    }
}

