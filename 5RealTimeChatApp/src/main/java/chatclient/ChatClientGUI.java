package chatclient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientGUI {

    private JFrame frame;

    private JPanel centerPanel;
    private JScrollPane centerScroll;

    private JTextField inputField;
    private JTextField usernameField;
    private JTextField roomField;

    private DefaultListModel<String> userModel;
    private JList<String> userList;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final String serverHost;
    private final int serverPort;

    public ChatClientGUI(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        buildUI();
        connect(host, port);
    }

    private void buildUI() {
        frame = new JFrame("Chat Application (Modern UI)");
        frame.setSize(900, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        usernameField = new JTextField("user" + (int)(Math.random() * 1000), 10);
        roomField = new JTextField("general", 10);
        JButton loginBtn = new JButton("Login");
        JButton joinBtn = new JButton("Join");

        top.add(new JLabel("Username:"));
        top.add(usernameField);
        top.add(loginBtn);

        top.add(new JLabel("Room:"));
        top.add(roomField);
        top.add(joinBtn);

        frame.add(top, BorderLayout.NORTH);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(240, 240, 240));

        centerScroll = new JScrollPane(centerPanel);
        centerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(centerScroll, BorderLayout.CENTER);

        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        frame.add(userScroll, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        inputField = new JTextField();
        JButton sendBtn = new JButton("Send");

        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);
        bottom.setBorder(new EmptyBorder(8, 8, 8, 8));

        frame.add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> login());
        joinBtn.addActionListener(e -> joinRoom());
        sendBtn.addActionListener(e -> sendMsg());
        inputField.addActionListener(e -> sendMsg());

        frame.setVisible(true);
    }

    private void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::readLoop).start();
            appendSystem("Connected to server.");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Unable to connect: " + e.getMessage());
        }
    }

    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String msg = line;
                SwingUtilities.invokeLater(() -> handleServerLine(msg));
            }
        } catch (IOException e) {
            appendSystem("Disconnected from server.");
        }
    }

    private void appendSystem(String msg) {
        addBubble(msg, "system", null, null);
    }

    private void addBubble(String text, String type, String user, String ts) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel label = new JLabel("<html>" + escape(text) + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.add(label);

        if (ts != null) {
            JLabel time = new JLabel(ts);
            time.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            time.setBorder(new EmptyBorder(6, 0, 0, 0));
            bubble.add(time);
        }

        if ("me".equals(type)) {
            bubble.setBackground(new Color(200, 255, 200));
            bubble.setOpaque(true);
            wrapper.add(bubble, BorderLayout.EAST);

        } else if ("other".equals(type)) {
            if (user != null) {
                JLabel u = new JLabel(user);
                u.setFont(new Font("Segoe UI", Font.BOLD, 12));
                wrapper.add(u, BorderLayout.NORTH);
            }
            bubble.setBackground(Color.WHITE);
            bubble.setOpaque(true);
            wrapper.add(bubble, BorderLayout.WEST);

        } else {
            bubble.setBackground(new Color(225, 225, 225));
            bubble.setOpaque(true);
            wrapper.add(bubble, BorderLayout.CENTER);
        }

        centerPanel.add(wrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.revalidate();

        JScrollBar bar = centerScroll.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> bar.setValue(bar.getMaximum()));
    }

    private String escape(String t) {
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
    private void handleServerLine(String line) {

        if (line.startsWith("MSG:")) {
            String data = line.substring(4);

            int idx1 = data.indexOf(':');
            int idx2 = data.indexOf(':', idx1 + 1);
            int idx3 = data.indexOf(':', idx2 + 1);

            if (idx1 < 0 || idx2 < 0 || idx3 < 0) {
                appendSystem(line);
                return;
            }

            String timestamp = data.substring(0, idx1);
            String room = data.substring(idx1 + 1, idx2);
            String user = data.substring(idx2 + 1, idx3);
            String msg = data.substring(idx3 + 1);

            boolean isMe = user.equals(usernameField.getText().trim());

            addBubble(msg, isMe ? "me" : "other", isMe ? null : user, timestamp);
            return;
        }

        if (line.startsWith("PM:")) {

            String data = line.substring(3);
            int idxLast = data.lastIndexOf(':');
            if (idxLast < 0) return;

            String message = data.substring(idxLast + 1);
            String rem = data.substring(0, idxLast);

            int idxUser = rem.lastIndexOf(':');
            if (idxUser < 0) return;

            String fromUser = rem.substring(idxUser + 1);
            String timestamp = rem.substring(0, idxUser);

            boolean isMe = fromUser.equals(usernameField.getText().trim());

            addBubble("[PM] " + message,
                    isMe ? "me" : "other",
                    isMe ? null : fromUser,
                    timestamp);

            return;
        }

        if (line.startsWith("INFO:")) {
            appendSystem(line.substring(5));
            return;
        }

        if (line.startsWith("ERROR:")) {
            appendSystem("[ERROR] " + line.substring(6));
            return;
        }

        if (line.startsWith("USERLIST:")) {
            userModel.clear();
            String[] list = line.substring(9).split(",");
            for (String u : list)
                if (!u.isBlank()) userModel.addElement(u);
            return;
        }

        appendSystem(line);
    }

    private void login() {
        out.println("LOGIN:" + usernameField.getText().trim());
    }

    private void joinRoom() {
        out.println("JOIN:" + roomField.getText().trim());
    }

    private void sendMsg() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;

        if (t.startsWith("/pm ")) {
            String[] p = t.split(" ", 3);
            if (p.length < 3) {
                appendSystem("Usage: /pm user message");
                return;
            }
            out.println("PM:" + p[1] + ":" + p[2]);
            addBubble("[PM] " + p[2], "me", null, now());

        } else {
            out.println("MSG:" + t);
        }

        inputField.setText("");
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) {

        String h = "localhost";
        int p = 5555;

        if (args.length > 0) h = args[0];
        if (args.length > 1) p = Integer.parseInt(args[1]);

        final String host = h;
        final int port = p;

        SwingUtilities.invokeLater(() -> new ChatClientGUI(host, port));
    }

}
