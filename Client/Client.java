import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
/**
 * implements the chat client
 * @author aviad
 *
 */
public class Client extends JFrame implements Runnable
{
    private String userName, msg, serverIP;
    private boolean pressed = false,isOnline=false;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket socket;
    JFrame main;
    private JLabel name, address;
    private JTextField named, addressed, userText, nameDisplay;
    private JTextArea chatWindow;
    private JButton connect, showOnline, clear, send;
    private final String getOnlineUsers = ":getOnlineUsers", deletMe = ":deleteMe";
    private int port;
    /**
     * The constructor is making the gui.
     * @param port The port that the client is want to connect to.
     */
    public Client(int port)
    {
        Client client = this;
        this.port = port;
        main = new JFrame(userName);
        connect = new JButton("Sing In");
        connect.setBounds(5,10,95,30);
        connect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(!pressed)
                {
                    client.userName = named.getText()+"-";
                    client.serverIP = addressed.getText();
                    Thread R1 = new Thread(client);
                    R1.start();
                    connect.setText("Sing Out");
                    pressed = true;
                    nameDisplay.setText(userName);
                    client.isOnline = true;
                }
                else
                {
                    client.isOnline = false;
                    sendInvisible(deletMe,client.userName);
                    closeAll();
                    System.exit(0);
                }
            }
        });

        showOnline = new JButton("Show Online");
        showOnline.setBounds(405,10,125,30);
        showOnline.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sendInvisible(getOnlineUsers,client.userName);
            }
        });

        clear = new JButton("Clear");
        clear.setBounds(535,10,95,30);
        clear.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                chatWindow.setText("");
            }
        });

        nameDisplay = new JTextField("User Name");
        nameDisplay.setEditable(false);
        nameDisplay.setBackground(Color.white);
        nameDisplay.setBounds(5,359,95,20);
        Border borderName = BorderFactory.createLineBorder(Color.BLACK);
        nameDisplay.setBorder(BorderFactory.createCompoundBorder(borderName,
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        userText = new JTextField();
        userText.setEditable(true);
        userText.setBounds(105,359,425,20);
        userText.setBackground(Color.white);
        userText.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                send(userText.getText(),client.userName);
                userText.setText("");
            }

        });

        send = new JButton("Send");
        send.setBounds(535,354,95,30);
        send.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                send(userText.getText(),client.userName);
                userText.setText("");
            }
        });

        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        JScrollPane sp = new JScrollPane(chatWindow);
        sp.setBounds(5, 50, 625, 300);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        sp.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        name = new JLabel("name:");
        name.setBounds(105,10,95,30);
        named = new JTextField();
        named.setBounds(150,15,95,20);

        address = new JLabel("address:");
        address.setBounds(245,10,95,30);
        addressed = new JTextField();
        addressed.setBounds(308,15,95,20);

        main.add(connect);
        main.add(showOnline);
        main.add(clear);
        main.add(named);
        main.add(name);
        main.add(addressed);
        main.add(address);
        main.add(sp);
        main.add(userText, BorderLayout.SOUTH);
        main.add(nameDisplay);
        main.add(send);

        main.setSize(637,415);
        main.setLayout(null);
        main.setVisible(true);
    }
    /**
     * establish the connection by calling connect() setupStreams() chat()
     * @param port The port that the client is want to connect to.
     */
    public void setup(int port)
    {
        try
        {
            connect(port);
            setupStreams();
            sendInvisible("",this.userName);
            chat();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Open new socket
     * @param port The port that the client is want to connect to.
     * @throws IOException
     */
    private void connect(int port) throws IOException
    {
        showMessage("Attemping to connect...");
        socket = new Socket(InetAddress.getByName(serverIP),port);
    }
    /**
     * Making output and input streams
     * @throws IOException
     */
    private void setupStreams()throws IOException
    {
        output=new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
    }
    /**
     * Wait for new messages from the input stream as long as the client is online
     * show the message that received
     * @throws IOException
     */
    private void chat() throws IOException
    {
        typing(true);
        do
        {
            try
            {
                msg = (String) input.readObject();
                showMessage("\n"+msg);
            }
            catch (ClassNotFoundException e)
            {
                showMessage("\n cannot understand what are you sending");
            }
        } while (isOnline);
    }
    /**
     * close open strams and socket
     */
    private void closeAll()
    {
        showMessage("\n closing...");
        typing(false);
        try
        {
            output.close();
            input.close();
            socket.close();
        }
        catch (IOException  e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Sent the message throw the client output stream and show it on the chat window
     * @param msg message to send
     * @param myName client user name
     */
    private void send(String msg,String myName)
    {
        try
        {
            output.writeObject(myName + msg);
            output.flush();
            showMessage("\n"+myName+msg);
        }
        catch (IOException e)
        {
            chatWindow.append("\nERROR");
        }
    }
    /**
     * Sent the message throw the client output stream without printing it
     * @param msg message to send
     * @param myName client user name
     */
    private void sendInvisible(String msg,String myName)
    {
        try
        {
            output.writeObject(myName + msg);
            output.flush();
        }
        catch (IOException e)
        {
            chatWindow.append("\nERROR");
        }
    }
    /**
     * show message on chat window
     * @param msg message to show (string)
     */
    private void showMessage(final String msg)
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        chatWindow.append(msg);
                    }
                }
        );
    }
    public void typing(final boolean flag)
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        userText.setEditable(flag);
                    }
                }
        );
    }
    @Override
    public void run() {
        setup(port);
    }
}
