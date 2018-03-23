
package enei.data.extract.db;




import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;




/**
 *
 */
public class ConnectionManager {



	public static Connection getConnection(String connString, String user, String password) throws SQLException {

		return DriverManager.getConnection(connString, user, password);
	}

}
