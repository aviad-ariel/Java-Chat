import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

class ClientHandler implements Runnable
{
    Server main;
    String userName;
    String friend;
    final ObjectInputStream i;
    final ObjectOutputStream o;
    Socket s;

    public ClientHandler(Socket s, String name, ObjectInputStream i, ObjectOutputStream o, Server serv)
    {
        this.main = serv;
        this.i = i;
        this.o = o;
        this.userName = name;
        this.s = s;
    }

    public void run()
    {
        String msg = "You are now connected!";
        this.main.send(msg, this.o, this.userName);
        this.broadcast(" Entered");

        while(true)
        {
            while(true)
            {
                try
                {
                    msg = (String)this.i.readObject();
                } catch (IOException | ClassNotFoundException var4)
                {
                    var4.printStackTrace();
                }

                if (msg.split(":")[1].equals("getOnlineUsers"))
                {
                    this.main.send(this.main.getUsersOnline(), this.o, this.userName);
                }
                else
                {
                    if (msg.split(":")[1].equals("deleteMe"))
                    {
                        this.main.removeClientFromList(this.userName);
                        this.broadcast(" Leaved");
                        return;
                    }

                    this.friend = msg.split(":")[0].split("-")[1];
                    ClientHandler toSend;
                    if (this.friend.equals("all"))
                    {
                        Iterator var3 = Server.clients.iterator();

                        while(var3.hasNext())
                        {
                            toSend = (ClientHandler)var3.next();
                            this.main.send(msg.split(":")[1], toSend.o, this.userName);
                        }
                    }
                    else
                    {
                        this.friend = this.friend + "-";
                        toSend = this.findClient(this.friend);
                        if (toSend != null)
                        {
                            this.main.send(msg.split(":")[1], toSend.o, this.userName);
                        }
                        else
                        {
                            this.main.send("User not found \nclick Show Online to get online user list", this.o, this.userName);
                        }
                    }
                }
            }
        }
    }

    private ClientHandler findClient(String friend)
    {
        return this.main.find(friend);
    }

    private void broadcast(String state)
    {
        Iterator var3 = Server.clients.iterator();

        while(var3.hasNext())
        {
            ClientHandler i = (ClientHandler)var3.next();
            this.main.send(this.userName + state, i.o, this.userName);
        }

        this.main.showMessage("\n" + this.userName + state);
    }
}
