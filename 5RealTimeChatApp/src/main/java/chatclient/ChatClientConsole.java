package chatclient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientConsole {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread readerThread;

    public ChatClientConsole(String host, int port) { this.host = host; this.port = port; }

    public void start() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        readerThread = new Thread(() -> {
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println("[SERVER] " + s);
                }
            } catch (IOException e) {
            }
        });
        readerThread.start();

        Scanner sc = new Scanner(System.in);
        System.out.println("Connected. Use commands: LOGIN:<name>, JOIN:<room>, MSG:<text>, PM:target:msg, LIST, LEAVE, LOGOUT");
        while (true) {
            String line = sc.nextLine();
            out.println(line);
            out.flush();
            if (line.equals("LOGOUT")) break;
        }
        shutdown();
    }

    public void shutdown() throws IOException {
        if (socket != null && !socket.isClosed()) socket.close();
        if (readerThread != null) readerThread.interrupt();
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5555;
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);
        new ChatClientConsole(host, port).start();
    }
}
