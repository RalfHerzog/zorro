

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;


public class Zorro {
	private Connection db = null;
	private Properties config= null;
	private PreparedStatement currentStatement= null;
	private ResultSet currentResultSet=null;
	public Zorro() {
		config=Config.getConfig();
	}
	public Zorro(String sql) throws SQLException {
		config=Config.getConfig();
		connect();
		exe(sql);
		printResult();
		close();
	}
	public void connect() throws SQLException {
		connect(config.getProperty("dbhost"), config.getProperty("dbname"), config.getProperty("dbuser"), config.getProperty("dbpass"));
	}
	public void connect(String dbHost, String dbName, String dbUser, String dbPass) throws SQLException{
		if(db!=null){
			db.close();
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch ( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		db = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/"+ dbName, dbUser, dbPass);
	}
	public ResultSet exe(String sql) throws SQLException{
		return exe(sql, new Object[] {});	 
	}
	public ResultSet exe(String sql, Object[] params) throws SQLException{
		closeStmt();
		currentStatement = prepare(currentStatement, sql, params);
		currentStatement.executeQuery();
		currentResultSet=currentStatement.getResultSet();
		return currentResultSet;
	}
	public int update(String sql) throws Exception{
		return update(sql, new Object[] {});	 
	}
	public int update(String sql, Object[] params) throws SQLException{
		closeStmt();
		currentStatement = prepare(currentStatement, sql, params);
		return currentStatement.executeUpdate();
	}
	private PreparedStatement prepare(PreparedStatement stmt, String sql,Object[] params) throws SQLException{
		if ( db == null ) {
			throw new SQLException( "Not connected to database" );
		}
		
		stmt = db.prepareStatement(sql);
		for(int i=0;i<params.length;i++){
			int paramIndex=i+1;
			if(params[i] instanceof String){
				stmt.setString(paramIndex, ((String)params[i]));
			}else if(params[i] instanceof Integer){
				stmt.setInt(paramIndex, ((Integer)params[i]));
			}else if(params[i] instanceof Double){
				stmt.setDouble(paramIndex, ((Double)params[i]));
			}else{
				throw new SQLException("Unknown parameter");
			}
		}
		return stmt;
	}
	public void closeStmt(){ //When a Statement object is closed, its current ResultSet object, if one exists, is also closed.
		if(currentStatement!=null){
			try {currentStatement.close();} catch (SQLException e) {}
		}
	}
	public void close(){
		if ( db == null ) {
			// No connection so nothing to close
			return;
		}
		try {
			db.close();
		} catch (SQLException e) {}
	}
	private String createResultString(){
		StringBuilder sb= new StringBuilder();
		if(currentResultSet!=null){
			try {
				int rows=0;
				ResultSetMetaData rsmd = currentResultSet.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				while (currentResultSet.next()) {
					for(int i=0; i<columnsNumber;i++){
						try{
							sb.append(currentResultSet.getObject(i+1).toString()+" : ");
						}catch(NullPointerException e){
							sb.append("NULL :");
						}
					}
					sb.append("\r\n");
					rows++;
				}
				sb.append("===========================================================\r\n");
				for(int i=0;i<columnsNumber;i++){
					sb.append(((i==(columnsNumber-1) ? (rsmd.getColumnName(i+1)+"\r\n") : (rsmd.getColumnName(i+1)+" : ")))); // Append "colname : " or "colname\r\n" if it's the last column
				}
				sb.append("===========================================================\r\n");
				sb.append(rows+" rows in set");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			sb.append("no result");
		}
		return sb.toString();
	}
	public void printResult(){
		System.out.println(createResultString());
	}
	public String toString(){
		return createResultString();
	}
}
