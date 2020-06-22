package conexoes;
import java.sql.SQLException;
import java.sql.Statement;

import conexoes.ConexaoSQLite;

public class CriarBanco {
	
	private final ConexaoSQLite conexaoSQLite;
	
	public CriarBanco(ConexaoSQLite pConexaoSQLite){
		this.conexaoSQLite = pConexaoSQLite;
	}
	
	public void criarTabelaProduto(){
		
		String sql = "CREATE TABLE IF NOT EXISTS Produtos"
				+ "("
				+ "id integer PRIMARY KEY AUTOINCREMENT,"
				+ "nome text NOT NULL,"
				+ "classificacao real,"
				+ "preco_inicial real,"
				+ "preco_final real,"
				+ "preco_desconto real,"
				+ "url text"
				+ ");";
		
		//exetucando sql de criar tabela.
		boolean conectou = false;
		
		try {
			conectou = this.conexaoSQLite.conectar();
			
			Statement stmt = this.conexaoSQLite.criarStatement();
			
			stmt.execute(sql);
			
		} catch (SQLException e) {
			// TODO: handle exception
		}finally {
			if(conectou){
				this.conexaoSQLite.desconectar();
			}
		}
		
	}
	
	//Metodo responsável por deletar todos os registros da tabela quando executado o Crawler.
	public void limparTabela(){
		String sql = " DELETE FROM PRODUTOS; ";
		
		boolean conectou = false;
		
		try {
			conectou = this.conexaoSQLite.conectar();
			
			Statement stmt = this.conexaoSQLite.criarStatement();
			
			stmt.execute(sql);
			System.out.println("Registros limpados");
			
		} catch (SQLException e) {
			// TODO: handle exception
		}finally {
			if(conectou){
				this.conexaoSQLite.desconectar();
			}
		}
	}

}
