/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto1;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author FuryCoder
 */
public class PostgreQuerys {
    
    public ResultSet PostgreQuerys(Connection con, String query){
        try{
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            return rs;
        }catch(Exception e){
            FxDialogs.showInformation("Nota", e.getMessage());
        }
        return null;
    }
    
    
    
}
