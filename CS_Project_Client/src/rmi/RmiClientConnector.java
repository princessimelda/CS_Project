
package rmi;

import rmi.StudentDbInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

/**
 * Singleton connector to the RMI Student Database service
 */
public class RmiClientConnector {

    private static StudentDbInterface remoteService;

    public static StudentDbInterface getRemoteService() {
        if (remoteService == null) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                remoteService = (StudentDbInterface) registry.lookup("StudentDatabase");
                System.out.println("✅ Connected to RMI service: StudentDatabase");
            } catch (RemoteException | NotBoundException e) {
                System.err.println("❌ RMI connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return remoteService;
    }
}
