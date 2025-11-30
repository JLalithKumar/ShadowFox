package chatserver;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String currentRoom;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public String getUsername() { return username; }

    public void send(String msg) {
        out.println(msg);
        out.flush();
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            send("INFO:Welcome! Please login with LOGIN:<username>");

            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line);
            }
        } catch (IOException e) {
        } finally {
            cleanup();
        }
    }

    private void handleLine(String line) {
        try {
            if (line.startsWith("LOGIN:")) {
                String name = line.substring(6).trim();
                if (name.isEmpty()) { send("ERROR:Username cannot be empty"); return; }
                if (server.registerUser(name, this)) {
                    username = name;
                    send("INFO:Logged in as " + username);
                } else {
                    send("ERROR:Username already taken");
                }
            } else if (line.startsWith("JOIN:")) {
                String room = line.substring(5).trim();
                if (currentRoom != null) {
                    server.leaveRoom(currentRoom, this);
                }
                currentRoom = room;
                server.joinRoom(room, this);
                send("INFO:Joined " + room);
            } else if (line.equals("LEAVE")) {
                if (currentRoom != null) {
                    server.leaveRoom(currentRoom, this);
                    send("INFO:Left " + currentRoom);
                    currentRoom = null;
                } else {
                    send("ERROR:Not in any room");
                }
            } else if (line.startsWith("MSG:")) {
                if (currentRoom == null) { send("ERROR:Join a room first"); return; }
                String msg = line.substring(4);
                server.broadcastToRoom(currentRoom, username, msg);
            } else if (line.startsWith("PM:")) {
                // format PM:target:message
                String[] parts = line.split(":", 3);
                if (parts.length < 3) { send("ERROR:PM format PM:target:message"); return; }
                server.sendPrivate(username, parts[1], parts[2]);
            } else if (line.equals("LOGOUT")) {
                send("INFO:Bye");
                cleanup();
                try { socket.close(); } catch (IOException ignored) {}
            } else {
                send("ERROR:Unknown command");
            }
        } catch (Exception e) {
            send("ERROR:Server exception: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (currentRoom != null) server.leaveRoom(currentRoom, this);
            if (username != null) server.unregisterUser(username);
            socket.close();
        } catch (IOException ignored) {}
    }
}
