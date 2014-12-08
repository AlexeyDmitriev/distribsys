import name.admitriev.keyvaluestorage.Coordinator;
import name.admitriev.keyvaluestorage.IncorrectOperationException;
import name.admitriev.keyvaluestorage.Server;

import java.rmi.Naming;

public class TestServer
{
    static void test(boolean condition) throws TestFailedException
    {
        if (!condition) {
            throw new TestFailedException("FAILED");
        }
    }

    public static void main (String[] argv) throws Exception
    {
        String coordName = "coordinator1";
        String srv1Name = "server1";
        String srv2Name = "server2";

        Coordinator coordinator = new Coordinator();
        Naming.rebind(coordName, coordinator);

        Server server1 = new Server(srv1Name, coordName);
        Server server2 = new Server(srv2Name, coordName);
        Naming.rebind(srv1Name, server1);
        Naming.rebind(srv2Name, server2);

        int longDelay = Coordinator.DEAD_PINGS * 2;

        // first primary
        for (int i = 0; i < longDelay; ++i) {
            coordinator.tick();
            server1.tick();
        }
        test(coordinator.primary().equals(srv1Name));
        server1.put("a", "aaa");
        test(server1.get("a").equals("aaa"));

        // first backup
        for (int i = 0; i < longDelay; ++i) {
            coordinator.tick();
            server1.tick();
            server2.tick();
        }
        test(coordinator.primary().equals(srv1Name));
        server1.put("b", "bbb");
        test(server1.get("b").equals("bbb"));

        // primary fails
        java.rmi.server.UnicastRemoteObject.unexportObject(server1, true);
        for (int i = 0; i < longDelay; ++i) {
            coordinator.tick();
            server2.tick();
        }
        test(coordinator.primary().equals(srv2Name));
        test(server2.get("a").equals("aaa"));
        test(server2.get("b").equals("bbb"));

        // ex-primary restarts
        server1 = new Server(srv1Name, coordName);
        Naming.rebind(srv1Name, server1);
        for (int i = 0; i < longDelay; ++i) {
            coordinator.tick();
            server1.tick();
            server2.tick();
        }
        test(coordinator.primary().equals(srv2Name));

        // oh no! network partition
        // client sees server2, but coordinator does not
        for (int i = 0; i < longDelay; ++i) {
            coordinator.tick();
            server1.tick();
        }
        test(coordinator.primary().equals(srv1Name));
        test(server1.get("a").equals("aaa"));
        test(server1.get("b").equals("bbb"));
        try {
            server2.put("c", "ccc");
            throw new TestFailedException("Must throw!");
        } catch(IncorrectOperationException ex) {
            // ok
        }

        System.out.println("Everything ok!");

        java.rmi.server.UnicastRemoteObject.unexportObject(coordinator, true);
        java.rmi.server.UnicastRemoteObject.unexportObject(server1, true);
        java.rmi.server.UnicastRemoteObject.unexportObject(server2, true);
    }
}
