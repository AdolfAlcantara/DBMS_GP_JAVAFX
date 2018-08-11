package proyecto1;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import proyecto1.ConnectionStablisher;
import proyecto1.FxDialogs;

public class FXMLDocumentController {
    
    Connection conn = null;
    ArrayList<ConnectionNode> connectionList = new ArrayList<>();
    PostgreQuerys pq = new PostgreQuerys();
    ConnectionStablisher cs = new ConnectionStablisher();
    
    public Button connectBtn;
    public Button reloadBtn;
    @FXML private TextField userField,passField,dbNameField;
    @FXML private TextArea instructionsInput;
    @FXML private GridPane dataGrid;
    @FXML private ComboBox<String> connectionBox;
    @FXML private TreeView tree;
    public String user, dbName;
    int gridColumns=0,gridRows=0;
    
    private class ConnectionNode{
        String usuario,password,basedatos;
        private ConnectionNode(String usr, String pass, String db){
            usuario = usr;
            password = pass;
            basedatos = db;
        }
    }
    
    private Connection getConn(String user, String pass, String dbname){
        try{
            conn  = cs.connect(user, pass, dbName);
            return conn;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    private Connection getOldConn(){
        String selectedItems = connectionBox.getValue();
        String[] values = selectedItems.split(":");
        for(Iterator<ConnectionNode> i = connectionList.iterator();i.hasNext();){
            ConnectionNode cn = i.next();
            if(values[0].equals(cn.usuario) && values[1].equals(cn.basedatos)){
                return cs.connect(cn.usuario, cn.password, cn.basedatos);
            }
        }
        return null;
    }
    
    private Connection getOldConn(String db, String user){
        for(Iterator<ConnectionNode> i = connectionList.iterator();i.hasNext();){
            ConnectionNode cn = i.next();
            if(user.equals(cn.usuario) && db.equals(cn.basedatos)){
                return cs.connect(cn.usuario, cn.password, cn.basedatos);
            }
        }
        return null;
    }
    
    public void connect(){
        user = userField.getText();
        dbName = dbNameField.getText();
        conn = getOldConn(dbName, user);
        if(conn==null){
            conn = getConn(user, passField.getText(), dbName);
            connectionBox.getItems().add(user+":"+dbName);
            connectionList.add(new ConnectionNode(user, passField.getText() ,dbName));
            FxDialogs.showInformation("Exito", "Conexion Exitosa");
        }else{
            FxDialogs.showInformation("Error", "La conexion ya existe");
        }
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        populateTree(user,dbName);
    }
    
    public void reload(){
        String selectedItems = connectionBox.getValue();
        String[] values = selectedItems.split(":");
        populateTree(values[0],values[1]);
    }
    
    public void ExecuteStatement(KeyEvent event){
        if(event.isControlDown() && event.getCode() == KeyCode.ENTER){
            String instruction = instructionsInput.getText();
            conn = getOldConn();
            ResultSet rs = pq.PostgreQuerys(conn, instruction);
            
            resultsToGrid(rs);
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void resultsToGrid(ResultSet rs){  
        try{
             int colCount = rs.getMetaData().getColumnCount();
                createGrid(colCount);
                int contadorCol=1,contadorRow=1;
                while(contadorCol<=colCount){
                    dataGrid.add(new Text(rs.getMetaData().getColumnName(contadorCol)),contadorCol-1,0);
                    contadorCol++;
                }
            if(rs.next()==false){
                FxDialogs.showInformation("Exito", "Accion exitosa");
            }else{
                do{
                    RowConstraints rowConst = new RowConstraints();
                    rowConst.setVgrow(Priority.ALWAYS);
                    rowConst.setValignment(VPos.CENTER);
                    rowConst.prefHeightProperty().setValue(8);
                    dataGrid.getRowConstraints().add(rowConst);
                    for(int i=1;i<=colCount;i++){
                        Text temp;
                        try{
                            temp  = new Text(rs.getObject(i).toString());
                        }catch(NullPointerException e){
                            temp = new Text("");
                        }
                        dataGrid.add(temp, i-1, contadorRow);
                    }
                    contadorRow++;
                }while(rs.next());
            }
//            gridRows = contadorRow-1;
        }catch(Exception e){
            e.printStackTrace();
        }
        RowConstraints rowConst = new RowConstraints();
        rowConst.setVgrow(Priority.ALWAYS);
        rowConst.setValignment(VPos.CENTER);
        rowConst.prefHeightProperty().setValue(8);
        dataGrid.getRowConstraints().add(rowConst);
    }
    
    public void createGrid(int columncount){
        clearGrid();

        for (int i = 0; i < columncount; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setHgrow(Priority.ALWAYS);
            colConst.setHalignment(HPos.CENTER);
            dataGrid.getColumnConstraints().add(colConst);
        }

    }
    
    public void clearGrid(){
        dataGrid.setGridLinesVisible(true);
        dataGrid.setAlignment(Pos.TOP_LEFT);
        Node node = dataGrid.getChildren().get(0);
        while(dataGrid.getColumnConstraints().size()>0){
            dataGrid.getChildren().remove(0);
            dataGrid.getColumnConstraints().remove(0);
        }
        while(dataGrid.getRowConstraints().size()>0){
            dataGrid.getRowConstraints().remove(0);
        }
        dataGrid.getChildren().clear();
        dataGrid.getChildren().add(node);
    }
    
    public void populateTree(String user, String dbname){
        try{
            tree.getRoot().getChildren().clear();
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
        TreeItem<String> root;
        root = new TreeItem<>();
        root.setExpanded(true);
        
        tree.setRoot(root);
        tree.setShowRoot(false);
        conn = getOldConn(dbname,user);
//        ResultSet rs = pq.PostgreQuerys(conn,"select datname from pg_stat_activity;");
//        try {
//            while(rs.next()){
//                try{
//                    String db = rs.getObject(1).toString();
                    TreeItem<String> temp = makeBranch(dbname,root);
                    temp.setExpanded(true);
                    
                    TreeItem<String> tablas = makeBranch("Tablas",temp);
                    TreeItem<String> funciones = makeBranch("Funciones",temp);
//                    TreeItem<String> procedimientos = makeBranch("Procedimientos",temp);
                    TreeItem<String> triggers = makeBranch("Triggers",temp);
                    TreeItem<String> secuencias = makeBranch("Secuencias",temp);
                    TreeItem<String> indices = makeBranch("Indices",temp);
                try{
                    ResultSet tb = pq.PostgreQuerys(conn, "select tablename from pg_tables where schemaname='public';");
                    ResultSet fn = pq.PostgreQuerys(conn, "select proname from pg_proc where pronamespace='2200';");
                    ResultSet tg = pq.PostgreQuerys(conn, "select t.tgname from pg_class c inner join pg_trigger t on t.tgrelid = c.oid where c.relnamespace = '2200';");
                    ResultSet in = pq.PostgreQuerys(conn, "select indexname from pg_indexes where schemaname='public';");
                    ResultSet sq = pq.PostgreQuerys(conn, "select c.relname FROM pg_class c WHERE c.relkind = 'S';");
                    while(tb.next()){
                            String tab = tb.getObject(1).toString();
                            TreeItem<String> tabla = makeBranch(tab,tablas);
                    }
                    while(fn.next()){
                        String fun = fn.getObject(1).toString();
                        TreeItem<String> funcion = makeBranch(fun, funciones);
                    }
                    while(tg.next()){
                        String trig = tg.getObject(1).toString();
                        TreeItem<String> trigger = makeBranch(trig, triggers);
                    }
                    while(in.next()){
                        String ind = in.getObject(1).toString();
                        TreeItem<String> index = makeBranch(ind, indices);
                    }
                    while(sq.next()){
                        String seq = sq.getObject(1).toString();
                        TreeItem<String> sequence = makeBranch(seq, secuencias);
                    }
                }catch(NullPointerException e){
                    e.printStackTrace();
                }catch(SQLException sqe){
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, sqe);
                }
//            }
//            conn.close();
//        } catch (SQLException ex) {
            
//        }
        
    }
    
    private TreeItem<String> makeBranch(String text, TreeItem<String> father){
        TreeItem<String> item = new TreeItem<String>(text);
        item.setExpanded(false);
        father.getChildren().add(item);
        return item;
    }
    
}