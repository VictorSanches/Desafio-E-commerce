package conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoSQLite {
	
	private Connection conexao;
	
	//criacao do metodo para conectar a base de dados.
	public boolean conectar(){
		
		try {
			//definicao da string de conexao.
			String url = "jdbc:sqlite:banco_de_dados/registros.db";
			
			this.conexao = DriverManager.getConnection(url);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
				
		return true;
	}
	
	//criacao do metodo para desconectar a base de dados.
	public boolean desconectar(){
		
		try {
			if(this.conexao.isClosed() == false){
				this.conexao.close();
			}			
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	//cria statement para ser executado	
	public Statement criarStatement(){
		try {
			return this.conexao.createStatement();
		} catch (SQLException e) {
			return null;
		}
	}
	
	
	//cria o preparedStatement.
	public PreparedStatement criarPreparedStatement(String sql){
		try {
			return this.conexao.prepareStatement(sql);
		} catch (SQLException e) {
			System.out.println(e);
			return null;
		}
	}
	
	public Connection getConexao(){
		return this.conexao;
	}

}
