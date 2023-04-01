/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clint;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitch
 */
public class Presentation {
    private static final int port = 7500;
    
    public static void main(String[] args){
        try {
            ServerSocket serversocket = new ServerSocket(port);
            System.out.println("Server started on port "+port );
            while(true){
                Socket socket = serversocket.accept();
                try {       
                    Robot robot = new Robot();
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    while(true){
                        byte[] dta = new byte[4];
                        inputStream.readFully(dta);
                        int result = 0;
                        for(int  i = 0;i < dta.length; i++ ){
                            int byteValue = dta[i] & 0xFF;
                            result += byteValue << (8 * (3 - i));
                        } 
                       System.out.println("keycode:"+result);
                    }
                } catch (AWTException ex) {
                    Logger.getLogger(Presentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Presentation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
}
