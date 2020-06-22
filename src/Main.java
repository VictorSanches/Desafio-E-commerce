import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import conexoes.ConexaoSQLite;
import conexoes.CriarBanco;

public class Main {
	public static void main(String[] args) {

		String strOption;
		int option = 0;
		do {
			strOption = JOptionPane.showInputDialog(":::MENU:::\n1: Crawler\n2: Consultar Registros\n3: Sair");
			try {
				option = Integer.parseInt(strOption);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Só é permitido números");
				option = 0;
			}
			switch (option) {
			case 1:
				buscaProduto();
				JOptionPane.showMessageDialog(null, "Todos os registros foram inseridos");
				break;
			case 2:
				filtraRegistros();
			case 3:
				break;
			default:
				JOptionPane.showMessageDialog(null, "Opção Inválida");
				break;
			}

		} while (option != 3);

	}

	public static boolean buscaProduto() {
		try {
			// Toda execução, é limpado os registros na base de dados e inserido
			// novamente.
			// Valida se houver registros, limpa todos.
			if (limpaRegistros() != 0) {
				ConexaoSQLite conexaoSQLite = new ConexaoSQLite();
				CriarBanco criarBanco = new CriarBanco(conexaoSQLite);
				criarBanco.limparTabela();
			}

			int i = 1;
			while (true) {
				String urlFinal = "https://www.netshoes.com.br/lst/ntt-camisetas/masculino?mi=sub__masc_cat_camisetas_190402&psn=Banner_BarradeCategorias&nsCat=Artificial&page="
						+ i + "";

				Document doc = Jsoup.connect(urlFinal).get();

				Elements produtos = doc.getElementsByClass("item-card");

				// Caso houver menos de 42 itens no catálogo, interrompe a busca.
				int qtdProdutos = produtos.size();
				if (qtdProdutos < 42) {
					break;
				}

				for (Element produto : produtos) {
					Element nomeElemento = produto.getElementsByClass("item-card__description").first();
					String nomeProduto = nomeElemento.getElementsByTag("span").first().text();

					// Elemento da nota do produto.
					Element ratingElemento = produto.getElementsByClass("item-card__description__stars").first();

					// Variável que retorna falso caso o campo tenha valor.
					boolean validaRating = ratingElemento.getElementsByTag("i").isEmpty();

					// Validação para caso o produto não houver classificação,
					// não quebrar o código e imprimir o rating = 0.0.
					Double voto = 0.0;
					if (validaRating == false) {
						voto = Double.parseDouble(ratingElemento.getElementsByTag("i").first().text());
					}

					// Trazendo Url de cada produto.
					Element urlElement = produto.getElementsByClass("item-card__description__product-name").first();
					String urlProduto = urlElement.attr("href");
					urlFinal = "Http:" + urlProduto;

					// Criacao de variável que retorna uma Hash específica para
					// cada produto.
					String parentSku = produto.attr("parent-sku");

					// Concatenando a url com a Hash de cada produto.
					String urlPreco = "https://www.netshoes.com.br/refactoring/tpl/frdmprcs/" + parentSku + "/lazy/b";

					// Acessando url onde contém os valores do produto.
					Document docPreco = Jsoup.connect(urlPreco).get();

					// Buscando a Classe onde estão definidos os valores pelos
					// atributos.
					Elements valores = docPreco.getElementsByClass("pr");

					String PrecoOriginal = valores.attr("data-list-price");
					String PrecoFinal = valores.attr("data-final-price");
					String precoDesconto = valores.attr("data-discount-percentage");

					// Os valores devem ser dividos por 100 pois não é tratado
					// no retorno.
					Double precoOriginalConvertido = Double.parseDouble(PrecoOriginal) / 100;
					Double precoFinalConvertido = Double.parseDouble(PrecoFinal) / 100;
					Double precoDescontoConvertido = Double.parseDouble(precoDesconto) / 100;

					insereProduto(nomeProduto, voto, precoOriginalConvertido, precoFinalConvertido,
							precoDescontoConvertido, urlFinal);
					System.out.println("Inserindo produto: " + nomeProduto);
				}
				i++;
				System.out.println("Pagina: " + i);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static void insereProduto(String nomeProduto, Double voto, double precoOriginalConvertido,
			double precoFinalConvertido, double precoDescontoConvertido, String urlFinal) {

		// Iniciando conexão com base de dados.
		ConexaoSQLite conexaoSQLite = new ConexaoSQLite();
		CriarBanco criarBanco = new CriarBanco(conexaoSQLite);

		criarBanco.criarTabelaProduto();

		conexaoSQLite.conectar();

		String sqlInsert = " INSERT INTO PRODUTOS (" + "nome," + "classificacao," + "preco_inicial," + "preco_final,"
				+ "preco_desconto," + "url" + ") " + "VALUES(?,?,?,?,?,?); ";

		PreparedStatement pst = conexaoSQLite.criarPreparedStatement(sqlInsert);
		try {

			pst.setString(1, nomeProduto);
			pst.setDouble(2, voto);
			pst.setDouble(3, precoOriginalConvertido);
			pst.setDouble(4, precoFinalConvertido);
			pst.setDouble(5, precoDescontoConvertido);
			pst.setString(6, urlFinal);

			int resultado = pst.executeUpdate();

			// Validando retorno do preparedStatement para inserir produto.
			if (resultado == 1) {
				System.out.println("Produto inserido!");
			} else {
				System.out.println("Produto não inserido");
			}

		} catch (SQLException e) {
			System.out.println("Produto não inserido");
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			conexaoSQLite.desconectar();
		}
	}

	// Metodo retorna quantidade de registros.
	// Caso não houver registros, não apaga os dados.
	public static int limpaRegistros() {
		int retorno = 0;
		ConexaoSQLite conexaoSQLite = new ConexaoSQLite();
		conexaoSQLite.conectar();

		ResultSet rs = null;
		Statement st = null;
		String sql = " SELECT count(*) as total FROM PRODUTOS ";

		st = conexaoSQLite.criarStatement();
		try {
			rs = st.executeQuery(sql);
			System.out.println(sql);

			while (rs.next()) {
				retorno = rs.getInt("total");
			}
		} catch (SQLException e) {
			System.out.println("Erro ao consultar");
			System.out.println(e);
		} finally {
			try {
				rs.close();
				st.close();
			} catch (SQLException e2) {
				System.out.println("Erro ao fechar conexão");
			}
		}
		System.out.println("Retorno: " + retorno);
		return retorno;
	}

	// Metodo responsável por filtrar e exibir os registros pré definidos no
	// enunciado.
	public static void filtraRegistros() {

		ConexaoSQLite conexaoSQLite = new ConexaoSQLite();
		conexaoSQLite.conectar();

		ResultSet rs = null;
		Statement st = null;

		String sql = " select * from produtos WHERE classificacao in(select max(classificacao) from Produtos) limit 1; ";

		st = conexaoSQLite.criarStatement();
		try {
			rs = st.executeQuery(sql);

			while (rs.next()) {
				System.out.println("Produto com maior Rating:\n");
				System.out.println("Nome: " + rs.getString("nome"));
				System.out.println("Classificação: " + rs.getDouble("classificacao"));
				System.out.println("Preco Inicial: " + rs.getDouble("preco_inicial"));
				System.out.println("Preco Final: " + rs.getDouble("preco_final"));
				System.out.println("Valor Desconto: " + rs.getDouble("preco_desconto"));
				System.out.println("Url: " + rs.getDouble("url"));
				System.out.println(" ");
			}
		} catch (SQLException e) {
			System.out.println("Erro ao consultar");
		}

		sql = " select * from Produtos order by preco_final asc limit 1; ";

		st = conexaoSQLite.criarStatement();
		try {
			rs = st.executeQuery(sql);

			while (rs.next()) {
				System.out.println("Produto mais Barato:\n");
				System.out.println("Nome: " + rs.getString("nome"));
				System.out.println("Classificação: " + rs.getDouble("classificacao"));
				System.out.println("Preco Inicial: " + rs.getDouble("preco_inicial"));
				System.out.println("Preco Final: " + rs.getDouble("preco_final"));
				System.out.println("Valor Desconto: " + rs.getDouble("preco_desconto"));
				System.out.println("Url: " + rs.getDouble("url"));
				System.out.println(" ");
			}
		} catch (SQLException e) {
			System.out.println("Erro ao consultar");
		}

		sql = "select * from Produtos order by preco_desconto desc limit 1;";

		st = conexaoSQLite.criarStatement();
		try {
			rs = st.executeQuery(sql);

			while (rs.next()) {
				System.out.println("Produto com maior Desconto:\n");
				System.out.println("Nome: " + rs.getString("nome"));
				System.out.println("Classificação: " + rs.getDouble("classificacao"));
				System.out.println("Preco Inicial: " + rs.getDouble("preco_inicial"));
				System.out.println("Preco Final: " + rs.getDouble("preco_final"));
				System.out.println("Valor Desconto: " + rs.getDouble("preco_desconto"));
				System.out.println("Url: " + rs.getDouble("url"));
				System.out.println(" ");
			}
		} catch (SQLException e) {
			System.out.println("Erro ao consultar");
		}

	}

}
