package chatserver;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Final ChatServer — FILE BROADCAST FORMAT (Choice-1)
 *
 * Broadcast when file uploaded:
 *  MSG:<ts>:<room>:SERVER:FILE:<fileId>:<filename>:<filesize>:<sender>
 *
 * File Upload:
 *   UPLOAD:<room>:<filename>:<filesize>:<sender>
 *
 * File Download:
 *   DOWNLOAD:<fileId>
 */
public class ChatServer {
    private final int port;
    private final ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<ClientHandler>> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientHandler> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FileMeta> files = new ConcurrentHashMap<>();

    private final Path filesDir = Paths.get("server_files");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);

        if (!Files.exists(filesDir)) Files.createDirectories(filesDir);

        // Start file transfer server
        new Thread(new FileTransferServer(6000)).start();
        System.out.println("FileTransferServer started on port 6000");
        System.out.println("ChatServer started on port " + port);
    }

    public void start() {
        try {
            while (true) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, this);
                pool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String username, ClientHandler handler) {
        return users.putIfAbsent(username, handler) == null;
    }

    public void unregisterUser(String username) {
        users.remove(username);
        rooms.values().forEach(set -> set.removeIf(ch -> username.equals(ch.getUsername())));
    }

    public void joinRoom(String room, ClientHandler client) {
        rooms.computeIfAbsent(room, r -> new CopyOnWriteArraySet<>()).add(client);
        broadcastInfo(room, client.getUsername() + " joined the room.");
        sendRoomUserList(room);
    }

    public void leaveRoom(String room, ClientHandler client) {
        CopyOnWriteArraySet<ClientHandler> set = rooms.get(room);
        if (set != null) {
            set.remove(client);
            broadcastInfo(room, client.getUsername() + " left the room.");
            sendRoomUserList(room);
        }
    }

    public void broadcastToRoom(String room, String username, String text) {
        Set<ClientHandler> set = rooms.get(room);
        if (set == null) return;

        String ts = LocalDateTime.now().format(TS);
        String out = "MSG:" + ts + ":" + room + ":" + username + ":" + text;

        for (ClientHandler ch : set) ch.send(out);
    }

    public void broadcastInfo(String room, String text) {
        Set<ClientHandler> set = rooms.get(room);
        if (set != null) {
            String out = "INFO:" + text;
            for (ClientHandler ch : set) ch.send(out);
        }
    }

    public void sendPrivate(String from, String to, String text) {
        ClientHandler target = users.get(to);
        if (target != null) {
            String ts = LocalDateTime.now().format(TS);
            target.send("PM:" + ts + ":" + from + ":" + text);
        }
    }

    public void sendRoomUserList(String room) {
        Set<ClientHandler> set = rooms.get(room);
        if (set == null) return;

        String list = String.join(",",
                set.stream().map(ClientHandler::getUsername).toArray(String[]::new));

        for (ClientHandler ch : set) ch.send("USERLIST:" + list);
    }

    // ------------ File Meta Holder ----------
    private static class FileMeta {
        final String id, filename, sender, room;
        final Path path;
        final long size;

        FileMeta(String id, Path path, String filename, long size, String sender, String room) {
            this.id = id;
            this.path = path;
            this.filename = filename;
            this.size = size;
            this.sender = sender;
            this.room = room;
        }
    }

    private void registerUploadedFile(FileMeta meta) {
        files.put(meta.id, meta);

        // FINAL CHOICE-1 BROADCAST
        String fileMsg = "FILE:" + meta.id + ":" + meta.filename + ":" + meta.size + ":" + meta.sender;

        broadcastToRoom(meta.room, "SERVER", "FILE:" + meta.id + ":" + meta.filename + ":" + meta.size + ":" + meta.sender);
    }

    private FileMeta getFileMeta(String id) {
        return files.get(id);
    }

    // ---------------- FILE TRANSFER SERVER ----------------
    private class FileTransferServer implements Runnable {
        private final int filePort;
        private ServerSocket fileSocket;

        FileTransferServer(int filePort) {
            this.filePort = filePort;
            try {
                fileSocket = new ServerSocket(filePort);
            } catch (IOException e) { throw new RuntimeException(e); }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket s = fileSocket.accept();
                    handle(s);
                } catch (IOException ignored) {}
            }
        }

        private void handle(Socket s) {
            try (InputStream rawIn = s.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(rawIn));
                 OutputStream rawOut = s.getOutputStream();
                 PrintWriter writer = new PrintWriter(rawOut, true)) {

                String header = reader.readLine();
                if (header == null) return;

                if (header.startsWith("UPLOAD:")) {
                    String[] p = header.split(":", 5);
                    String room = p[1];
                    String filename = p[2];
                    long filesize = Long.parseLong(p[3]);
                    String sender = p[4];

                    String id = UUID.randomUUID().toString();
                    Path dest = filesDir.resolve(id + "_" + filename);

                    try (OutputStream fos = Files.newOutputStream(dest)) {
                        byte[] buf = new byte[8192];
                        long remaining = filesize;
                        while (remaining > 0) {
                            int r = rawIn.read(buf, 0, (int) Math.min(remaining, buf.length));
                            if (r == -1) break;
                            fos.write(buf, 0, r);
                            remaining -= r;
                        }
                    }

                    long actual = Files.size(dest);
                    FileMeta meta = new FileMeta(id, dest, filename, actual, sender, room);
                    registerUploadedFile(meta);

                    writer.println("OK:" + id);
                }

                else if (header.startsWith("DOWNLOAD:")) {
                    String id = header.substring(9);
                    FileMeta meta = getFileMeta(id);

                    if (meta == null) {
                        writer.println("ERROR:NotFound");
                        return;
                    }

                    writer.println("OK:" + meta.filename + ":" + meta.size);

                    try (InputStream fis = Files.newInputStream(meta.path)) {
                        fis.transferTo(rawOut);
                    }
                }

            } catch (Exception ignored) {}
        }
    }


    // ------------- MAIN -------------
    public static void main(String[] args) {
        try {
            new ChatServer(5555).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
