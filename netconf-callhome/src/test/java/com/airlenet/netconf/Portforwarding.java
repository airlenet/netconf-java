package com.airlenet.netconf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Portforwarding {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket sdnc = new Socket("172.16.17.129",4334);


        Socket sshSocket= new Socket("172.19.106.72",2022);


        OutputStream sdncOutputStream = sdnc.getOutputStream();
        sdncOutputStream.write("FlexEdge".getBytes());
        sdncOutputStream.write(new byte[]{'\0'});

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try (InputStream sdncInputStream = sdnc.getInputStream(); OutputStream sshSocketOutputStream = sshSocket.getOutputStream()) {

                    byte[] bytes = new byte[4096];
                    int read=0;
                    while (( read = sdncInputStream.read(bytes))!=-1){
                        System.out.println("from sdnc:"+new String(bytes,0,read));
                        sshSocketOutputStream.write(bytes, 0, read);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });thread1.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try (InputStream sshInputStream = sshSocket.getInputStream(); OutputStream sdncSocketOutputStream = sdnc.getOutputStream()) {

                    byte[] bytes = new byte[4096];
                    int read=0;
                    while (( read = sshInputStream.read(bytes))!=-1){
                        System.out.println("from device:"+new String(bytes,0,read));
                        sdncSocketOutputStream.write(bytes, 0, read);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });thread.start();

        thread.join();
        thread1.join();

    }
}
