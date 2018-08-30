import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

public class Server extends JFrame implements Runnable
{
    static Vector<ClientHandler> clients = new Vector();
    static int numOfClients = 0;
    private int port;
    private boolean pressed = false;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket socket;
    JTextField userText;
    JTextArea chatWindow;
    JFrame main;
    JPanel panel;
    JLabel label;
    String userName;
    JButton start;

    public Server(int port)
    {
        this.port = port;
        this.main = new JFrame("Chat Server");
        this.start = new JButton("Start");
        this.start.setBounds(153, 10, 95, 30);
        this.start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!Server.this.pressed)
                {
                    Thread R1 = new Thread(Server.this);
                    R1.start();
                    Server.this.start.setText("stop");
                    Server.this.pressed = true;
                }
                else
                {
                    System.exit(0);
                }

            }
        });
        this.chatWindow = new JTextArea();
        this.chatWindow.setEditable(false);
        JScrollPane sp = new JScrollPane(this.chatWindow);
        sp.setBounds(5, 50, 400, 300);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        sp.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        this.main.add(this.start);
        this.main.add(sp);
        this.main.setSize(412, 381);
        this.main.setLayout((LayoutManager)null);
        this.main.setVisible(true);
    }

    public void setup(int port) throws ClassNotFoundException
    {
        try
        {
            this.server = new ServerSocket(port, 10);
            this.showMessage(Inet4Address.getLocalHost().getHostAddress());

            while(true)
            {
                this.waitForClients();
                this.setupStreams();
                this.userName = (String)this.input.readObject();
                ClientHandler curr = new ClientHandler(this.socket, this.userName, this.input, this.output, this);
                Thread t = new Thread(curr);
                clients.add(curr);
                t.start();
                ++numOfClients;
            }
        }
        catch (IOException var4)
        {
            System.out.println(var4);
        }
    }

    private void waitForClients() throws IOException
    {
        this.showMessage("\nServer is up and waiting for clients");
        this.socket = this.server.accept();
        this.showMessage("\n" + this.socket.getInetAddress().getHostName() + " Is now connected");
    }

    private void setupStreams() throws IOException
    {
        this.output = new ObjectOutputStream(this.socket.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(this.socket.getInputStream());
        this.showMessage("\nStream is setup");
    }

    public void send(String msg, ObjectOutputStream o, String un)
    {
        try
        {
            o.writeObject(un + ":" + msg);
            o.flush();
            this.showMessage("\n" + un + ":" + msg);
        }
        catch (IOException var5)
        {
            this.chatWindow.append("\n ERROR");
        }

    }

    protected void showMessage(final String msg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Server.this.chatWindow.append(msg);
            }
        });
    }

    protected void typing(final boolean flag)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Server.this.userText.setEditable(flag);
            }
        });
    }

    protected String getUsersOnline()
    {
        String ans = "|";

        ClientHandler i;
        for(Iterator var3 = clients.iterator(); var3.hasNext(); ans = ans + i.userName + "|")
        {
            i = (ClientHandler)var3.next();
        }

        return ans;
    }

    protected ClientHandler find(String friend)
    {
        Iterator var3 = clients.iterator();

        while(var3.hasNext())
        {
            ClientHandler i = (ClientHandler)var3.next();
            if (friend.equals(i.userName))
            {
                return i;
            }
        }

        return null;
    }

    public void removeClientFromList(String toRemove)
    {
        clients.remove(this.find(toRemove));
        --numOfClients;
    }

    public void run()
    {
        try
        {
            this.setup(this.port);
        }
        catch (ClassNotFoundException var2)
        {
            var2.printStackTrace();
        }

    }
}
