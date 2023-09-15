package com.kwasheniak;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientCore {

    public static void sendDataToServer(String text) throws IOException {
        Socket socket = new Socket("localhost", 1234);

        byte[] bytes = text.getBytes();

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(bytes.length);
        dataOutputStream.write(bytes);

        dataOutputStream.close();
        socket.close();
    }
}
