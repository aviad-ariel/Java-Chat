import javax.swing.JFrame;
public class ClientTest
{
    public static void main(String[] args)
    {
        Client client = new Client(4555);
        client.main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
