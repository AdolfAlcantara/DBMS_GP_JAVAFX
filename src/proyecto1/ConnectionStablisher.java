/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto1;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import org.postgresql.util.PSQLException;
import proyecto1.FxDialogs;
/**
 *
 * @author FuryCoder
 */
public class ConnectionStablisher {
    
    private final String url = "jdbc:postgresql://192.168.60.101:5432/";
    
    public Connection connect(String user, String pass, String dbname ){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url+dbname, user, pass);
            System.out.println("Connected to Postgresql server succesfully with user: "+user+" to database "+ dbname);
            return conn;
        }catch(Exception e){
            e.printStackTrace();
            FxDialogs.showInformation("Error",e.getMessage().toString() );
        }
        return null;
    }
    
    
}
