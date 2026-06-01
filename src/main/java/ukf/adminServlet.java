package ukf;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class adminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	getConnection connector;
	PrintWriter out;
       
    public adminServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		connector = (getConnection) session.getAttribute("connector");
		
		String id = request.getParameter("id");
		String action = request.getParameter("action");
		response.setContentType("text/html");
		out = response.getWriter();
		
		if (action == null) { showProducts(request, response); return; }
		
		switch (action) {
			case "zobrazitAddForm":
				zobrazitAddForm(request, response);
				break;
			case "pridatProduct":
				pridatProduct(request, response);
				break;
			case "showProduct":
				showProduct(id, request, response);
				break;
			case "showObjednavky":
				zobrazitObjednavky(request, response);	
				break;
			case "showPouzivatelia":
				zobrazitPouzivatelov(request, response);
				break;
			case "updateUser":
				updateUser(request, response);
				break;
			case "updateStav":
				updateStav(request, response);
				break;
			case "upravitProductForm":
				upravitProductForm(request, response);
				break;
			case "upravitProduct":
				upravitProduct(request, response);
				break;
			case "odstranitProductForm":
				odstranitProduct(request, response);
				break;
			case "odstranitObjednavku":
				odstranitObjednavku(request, response);
				break;
			case "odstranitUsera":
				odstranitUsera(request, response);
				break;
			default:
				showProducts(request, response);
				break;
		}
	}
	void showProducts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();
	    out.println("<main><div class=\"container text-light\"><h1>Novo pridané knihy</h1><hr><div class=\"row\">");

	    try {
	        Connection con = connector.dajSpojenie(request);

	        String query = "SELECT knihy.*, kategorie.nazov AS nazov_kategorie FROM knihy INNER JOIN "
	        		+ "kategorie ON knihy.id_kategorie = kategorie.id ORDER BY knihy.id DESC";
	        
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);

	        while (rs.next()) {
	            int mnozstvo = Integer.parseInt(rs.getString("mnozstvo"));
	            out.println("<div class='col-md-3'>");
	            out.println("<a href='adminServlet?action=showProduct&id=" + rs.getString("id") + "'>");
	            out.println("<div class='card mb-3'>");
	            out.println("<img src='" + rs.getString("img") + "' class='card-img-mid' alt='Obálka knihy'>");
	            out.println("<div class='card-body'>");
	            out.println("<h5 class='card-title'>" + rs.getString("nazov") + "</h5>");
	            out.println("<p class='card-text'>Autor: " + rs.getString("author") + "</p>");
	            out.println("<p class='card-text'>Žáner: " + rs.getString("nazov_kategorie") + "</p>");
	            out.println("<p class='card-text'>Cena: " + rs.getDouble("cena") + " €</p>");
	            if (mnozstvo > 0) {
	                out.println("<p class='card-text'>Množstvo: " + mnozstvo + "</p>");
	                out.println("<p class='card-text'><strong style='color: green;'>Dostupné</strong></p>");
	            } else {
	                out.println("<p class='card-text'>Množstvo: 0</p>");
	                out.println("<p class='card-text'><strong style='color: red;'>Nedostupné</strong></p>");
	            }
	            out.println("</div></div></a></div>");
	        }

	        rs.close();
	        stmt.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    out.println("</div></div></main>");
	    footer();
	}
	void showProduct(String id, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();

	    try {
	        Connection con = connector.dajSpojenie(request);

	        String query = "SELECT knihy.*, kategorie.nazov AS nazov_kategorie FROM knihy " +
	                "INNER JOIN kategorie ON knihy.id_kategorie = kategorie.id " +
	                "WHERE knihy.id = " + id;
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);

	        out.println("<main>");
	        out.println("<div class='container text-light w-75 bg-dark m-5 p-5 mx-auto rounded rounded-4'>");
	        
	        

	        while (rs.next()) {
	        	String nazov = rs.getString("nazov");
	        	String autor = rs.getString("author");
	        	String seria = rs.getString("seria");
	        	double cena = rs.getDouble("cena");
	        	int mnozstvo = rs.getInt("mnozstvo");
	        	String image = rs.getString("img");
	        	String nazov_kategorie = rs.getString("nazov_kategorie");
	        	String popis = rs.getString("popis");
	        	
	        	out.println("<form method='get' action='adminServlet'>");
	        	out.println("<input type='hidden' name='id' value='" + id + "'>");
	            out.println("<input type='hidden' name='nazov' value='" + nazov + "'>");
	            out.println("<input type='hidden' name='autor' value='" + autor + "'>");
	            out.println("<input type='hidden' name='seria' value='" + seria + "'>");
	            out.println("<input type='hidden' name='cena' value='" + cena + "'>");
	            out.println("<input type='hidden' name='mnozstvo' value='" + mnozstvo + "'>");
	            out.println("<input type='hidden' name='image' value='" + image + "'>");
	            out.println("<input type='hidden' name='nazov_kategorie' value='" + nazov_kategorie + "'>");
	            out.println("<input type='hidden' name='popis' value='" + popis + "'>");
	            out.println("<button class='primary' type='submit' name='action' value='upravitProductForm' class='btn btn-primary'>Upraviť</button>");
	        	out.println("<button class='danger' type='submit' name='action' value='odstranitProductForm' class='btn btn-primary'>Odstranit</button>");
	        	out.println("</form>");
	        	
	            out.println("<div class='row'>");
	            out.println("<div class='col'>");
	            if (seria != null && !seria.isEmpty()) {
	                out.println("<h1 class='pb-3'>" + seria + "</h1>");
	            }
	            out.println("<h2 class='pb-3'>" + nazov + "</h2>");
	            out.println("<h3 class='text-white-50 pt-5'>Autor: " + autor + "</h3>");

	            out.println("<h5 class='pt-5'>Žáner: " + nazov_kategorie + "</h5>");
	            out.println("<h5 class='pb-2'>Cena: " + cena + " €</h5>");

	            
	            if (mnozstvo > 1) {
	                out.println("<form method='get' action='adminServlet'>");
	                out.println("<input type='hidden' name='idPolozky' value='" + id + "'>");
	                out.println("<input type='hidden' name='cena' value='" + cena + "'>");
	                out.println("<label for='customRange1' class='form-label'>Počet: </label>");
	                out.println("<output name='currentValue' id='currentValue'>1</output>"); //kontrola tovaru pomocou range a sync
	                out.println("<input type='range' class='form-range' min='1' max='" + mnozstvo + "' id='customRange1' name='mnozstvo' value='1' oninput='currentValue.value = this.value'>");
	                out.println("<input type='hidden' name='celkoveMnozstvo' value='" + mnozstvo + "'>"); //celkove mnozstvo, kvôli updatnutiu kosika
	                //out.println("<button type='submit' name='action' value='add' class='btn btn-primary'>Pridať do košíka</button>");
	                out.println("</form>");
	            }
	            out.println("</div>");
	            out.println("<div class='col-md-3 mt-3 text-end'>");
	            out.println("<img src='" + image + "' height='250' alt='" + nazov + "'>");
	            out.println("</div></div><br><hr><br>");
	            out.println("<div><div>");
	            out.println("<p class='fs-1'>" + popis + "</p>");
	            out.println("</div></div>");
	        }

	        out.println("</div></main>");
	        rs.close();
	        stmt.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    footer();
	}
	void zobrazitPouzivatelov(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();
	    HttpSession session = request.getSession();
		int userID = (int) session.getAttribute("userID");

	    try {
	        Connection con = connector.dajSpojenie(request);

	        String selectQuery = "SELECT * FROM users WHERE id != " + userID;
	        PreparedStatement selectStatement = con.prepareStatement(selectQuery);
	        ResultSet rs = selectStatement.executeQuery();

	        out.println("<main>");
	        out.println("<div class='container bg-secondary rounded rounded-4 p-4'>");
	        out.println("<h1 class='mt-5 mb-4'>Všetci používatelia</h1>");

	        out.println("<table class='table'>");
	        out.println("<thead class='table-dark'>");
	        out.println("<tr>");
	        out.println("<th scope='col'>ID</th>");
	        out.println("<th scope='col'>Meno</th>");
	        out.println("<th scope='col'>Priezvisko</th>");
	        out.println("<th scope='col'>Email</th>");
	        out.println("<th scope='col'>Heslo</th>");
	        out.println("<th scope='col'>Adresa</th>");
	        out.println("<th scope='col'>Zľava</th>");
	        out.println("<th scope='col'>Poznámky</th>");
	        out.println("<th scope='col'>Admin</th>");
	        out.println("<th scope='col'>Ban</th>");
	        
            out.println("<th scope='col'>Akcie</th>");
	        
	        out.println("</tr>");
	        out.println("</thead>");
	        out.println("<tbody>");

	        while (rs.next()) {
	            out.println("<tr>");
	            out.println("<td>" + rs.getInt("id") + "</td>");
	            out.println("<td>" + rs.getString("meno") + "</td>");
	            out.println("<td>" + rs.getString("priezvisko") + "</td>");
	            out.println("<td>" + rs.getString("email") + "</td>");
	            out.println("<td>" + rs.getString("heslo") + "</td>");
	            out.println("<td>" + rs.getString("adresa") + "</td>");

                out.println("<form action='adminServlet' method='get'>");
                out.println("<input type='hidden' name='userID' value='" + rs.getInt("id") + "'>");
                
                out.println("<td><input type='number' name='zlava' min='0' max='100' value='" + rs.getInt("zlava") + "'></td>");
                
                out.println("<td><textarea name='poznamky' rows='2' cols='10'>" + rs.getString("poznamky") + "</textarea></td>");
                
                out.println("<td><select name='adminStatus'>");
                // Pridaj bez duplicitných options
                String[] optionsList = {"0", "1"};
                String adminStav = rs.getString("admin");
                out.println("<option value='" + adminStav + "' selected>" + adminStav + "</option>");
                for (String stav : optionsList) {
                    if (!adminStav.equals(stav)) {
                        out.println("<option value='" + stav + "'>" + stav + "</option>");
                    }
                }
                out.println("</select></td>");
                
                out.println("<td><select name='ban'>");
                String banStav = rs.getString("ban");
                out.println("<option value='" + banStav + "' selected>" + banStav + "</option>");
                for (String stav : optionsList) {
                    if (!banStav.equals(stav)) {
                        out.println("<option value='" + stav + "'>" + stav + "</option>");
                    }
                }
                out.println("</select></td>");
                
                out.println("<td><button type='submit' name='action' value='updateUser'>Potvrdiť</button>");
                out.println("<button type='submit' name='action' value='odstranitUsera'><i class=\"bi bi-x-circle-fill\"></i></button></td>");
                out.println("</form>");

	            out.println("</tr>");
	        }


	        out.println("</tbody>");
	        out.println("</table>");

	        out.println("</div>");
	        out.println("</main>");

	        rs.close();
	        selectStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    footer();
	}
	void zobrazitAddForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		header();
		
		out.println("<main>");
        out.println("<div class='container bg-secondary rounded rounded-4 p-4'>");
        out.println("<h1 class='mt-5 mb-4'>Pridanie produktu</h1>");

        out.println("<hr class='bg-light'>");
        out.println("<form method='get' action='adminServlet'>");
        out.println("<div class='form-group'>");
        out.println("<label for='nazov'>nazov: </label>");
        out.println("<input type='text' class='form-control' name='nazov' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='autor'>autor: </label>");
        out.println("<input type='text' class='form-control' name='autor' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='seria'>seria: </label>");
        out.println("<input type='text' class='form-control' name='seria'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='cena'>cena: </label>");
        out.println("<input type='number' class='form-control' name='cena' min ='0' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='mnozstvo'>mnozstvo: </label>");
        out.println("<input type='number' class='form-control' name='mnozstvo' min ='1' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='image'>image URL: </label>");
        out.println("<input type='text' class='form-control' name='image' required>");
        out.println("</div>");
        
        out.println("<label for='kategoria'>Kategória: </label>");
        out.println("<td><select id='kategoria' name='kategoria'>");
        ArrayList<String> optionsList = kategorie(request, response);
        for (String stav : optionsList) {
            out.println("<option value='" + stav + "'>" + stav + "</option>");
        }
        out.println("</select></td><br>");
        
        out.println("<div class='form-group'>");
        out.println("<label for='popis'>Popis: </label>");
        out.println("<textarea id='popis' name='popis' rows='8' cols='100' required></textarea>");
        out.println("</div><br>");
        
        out.println("<button type=submit' class='btn btn-danger' name='action' value='pridatProduct'>Pridat</button>");
        out.println("</form>");

        out.println("</div>");
        out.println("</main>");
		
        footer();
	}
	void pridatProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String nazov = request.getParameter("nazov");
	    String autor = request.getParameter("autor");
	    String seria = request.getParameter("seria");
	    if (seria == null) { seria = ""; }
	    int cena = Integer.parseInt(request.getParameter("cena"));
	    int mnozstvo = Integer.parseInt(request.getParameter("mnozstvo"));
	    String image = request.getParameter("image");
	    String kategoria = request.getParameter("kategoria");
	    int id_kategorie = getIdKategorie(request, response, kategoria);
	    String popis = request.getParameter("popis");
	    
	    try {
	        Connection con = connector.dajSpojenie(request);
	        
            String insertQuery = "INSERT INTO knihy (nazov, author, seria, cena, mnozstvo, img, id_kategorie, popis) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = con.prepareStatement(insertQuery);
            insertStatement.setString(1, nazov);
            insertStatement.setString(2, autor);
            insertStatement.setString(3, seria);
            insertStatement.setInt(4, cena);
            insertStatement.setInt(5, mnozstvo);
            insertStatement.setString(6, image);
            insertStatement.setInt(7, id_kategorie);
            insertStatement.setString(8, popis);
            insertStatement.executeUpdate();
            insertStatement.close();
            
            showProducts(request, response);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	ArrayList<String> kategorie(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ArrayList<String> k = new ArrayList<String>();
		try {
	        Connection con = connector.dajSpojenie(request);
	        
	        String selectQuery = "SELECT * FROM kategorie";
	        PreparedStatement selectStatement = con.prepareStatement(selectQuery);
	        ResultSet rs = selectStatement.executeQuery();
	        
	        while (rs.next()) {
	        	k.add(rs.getString("nazov"));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
        return k;
	}
	void zobrazitObjednavky(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();

	    try {
	        Connection con = connector.dajSpojenie(request);
	        HttpSession session = request.getSession();
	        int userID = (int) session.getAttribute("userID");
	        
	        String selectQuery = "SELECT * FROM objednavky";
	        PreparedStatement selectStatement = con.prepareStatement(selectQuery);
	        ResultSet orders = selectStatement.executeQuery();

	        out.println("<main>");
	        out.println("<div class='container bg-secondary rounded rounded-4 p-4'>");
	        out.println("<h1 class='mt-5 mb-4'>Všetky Objednávky</h1>");

	        out.println("<table class='table'>");
	        out.println("<thead class='table-dark'>");
	        out.println("<tr>");
	        out.println("<th scope='col'>Číslo objednávky</th>");
	        out.println("<th scope='col'>Cena</th>");
	        out.println("<th scope='col'>Dátum</th>");
	        out.println("<th scope='col'>Stav</th>");
	        
            out.println("<th scope='col'>Akcie</th>");
	        
	        out.println("</tr>");
	        out.println("</thead>");
	        out.println("<tbody>");

	        while (orders.next()) {
	            out.println("<tr>");
	            out.println("<td>" + orders.getInt("id") + "</td>");
	            out.println("<td>" + orders.getDouble("cena") + "</td>");
	            out.println("<td>" + orders.getString("datum") + "</td>");

                out.println("<td>");
                out.println("<form action='adminServlet' method='get'>");
                out.println("<input type='hidden' name='orderID' value='" + orders.getInt("id") + "'>");
                out.println("<select name='newStatus'>");
                // Pridaj bez duplicitných options
                String[] optionsList = {"spracuje sa", "zaplatená", "odoslaná"};
                String actualStav = orders.getString("stav");

                out.println("<option value='" + orders.getString("stav") + "' selected>" + actualStav + "</option>");

                for (String stav : optionsList) {
                    if (!actualStav.equals(stav)) {
                        out.println("<option value='" + stav + "'>" + stav + "</option>");
                    }
                }
                out.println("</select>");
                out.println("<input type='hidden' name='cisloObjednavky' value='" + orders.getInt("id") + "'>");
                out.println("<td><button type='submit' name='action' value='updateStav'>Potvrdiť</button>");
                out.println("<button type='submit' name='action' value='odstranitObjednavku'>Odstrániť</button></td>");
                out.println("</form>");
                out.println("</td>");

	            out.println("</tr>");
	        }


	        out.println("</tbody>");
	        out.println("</table>");

	        out.println("</div>");
	        out.println("</main>");

	        orders.close();
	        selectStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    footer();
	}
	void updateUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    try {
	        Connection con = connector.dajSpojenie(request);
	        
	        int userID = Integer.parseInt(request.getParameter("userID"));
	        String zlava = request.getParameter("zlava");
	        String poznamky = request.getParameter("poznamky");
	        String adminStav = request.getParameter("adminStatus");
	        String banStav = request.getParameter("ban");
	        
	        String sql = "UPDATE users SET zlava = ?, poznamky = ?, admin = ?, ban = ? WHERE id = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, zlava);
	        pstmt.setString(2, poznamky);
	        pstmt.setString(3, adminStav);
	        pstmt.setString(4, banStav);
	        pstmt.setInt(5, userID);
	        pstmt.executeUpdate();

	        pstmt.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    zobrazitPouzivatelov(request, response);
	}
	void updateStav(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    try {
	        Connection con = connector.dajSpojenie(request);
	        String stav = request.getParameter("newStatus");
	        int cisloObjednavky = Integer.parseInt(request.getParameter("cisloObjednavky")); 
	        
	        String sql = "UPDATE objednavky SET stav = ? WHERE id = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, stav);
	        pstmt.setInt(2, cisloObjednavky);
	        pstmt.executeUpdate();

	        pstmt.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    zobrazitObjednavky(request, response);
	}
	void upravitProductForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		header();
		
		out.println("<main>");
        out.println("<div class='container bg-secondary rounded rounded-4 p-4'>");
        out.println("<h1 class='mt-5 mb-4'>Upraviť produkt</h1>");

        out.println("<hr class='bg-light'>");
        out.println("<form method='get' action='adminServlet'>");
        out.println("<div class='form-group'>");
    	out.println("<input type='hidden' name='id' value='" + request.getParameter("id") + "'>");
        out.println("<label for='nazov'>nazov: </label>");
        out.println("<input type='text' class='form-control' name='nazov' placeholder='" + request.getParameter("nazov") + "' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='autor'>autor: </label>");
        out.println("<input type='text' class='form-control' name='autor' placeholder='" + request.getParameter("autor") + "' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='seria'>seria: </label>");
        out.println("<input type='text' class='form-control' name='seria' placeholder='" + request.getParameter("seria") + "'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='cena'>cena: </label>");
        out.println("<input type='number' class='form-control' name='cena' min ='0' placeholder='" + request.getParameter("cena") + "' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='mnozstvo'>mnozstvo: </label>");
        out.println("<input type='number' class='form-control' name='mnozstvo' min ='1' placeholder='" + request.getParameter("mnozstvo") + "' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='image'>image URL: </label>");
        out.println("<input type='text' class='form-control' name='image' placeholder='" + request.getParameter("image") + "' required>");
        out.println("</div>"); 
        
        out.println("<label for='kategoria'>Kategória: </label>");
        out.println("<td><select id='kategoria' name='kategoria'>");
        String actKategoria = request.getParameter("nazov_kategorie");
        ArrayList<String> optionsList = kategorie(request, response);
        out.println("<option value='" + actKategoria + "' selected>" + actKategoria + "</option>");
        for (String stav : optionsList) {
        	if (!actKategoria.equals(stav)) {
        		out.println("<option value='" + stav + "'>" + stav + "</option>");
        	}
        }
        out.println("</select></td><br>");
        
        out.println("<div class='form-group'>");
        out.println("<label for='popis'>Popis: </label>");
        out.println("<textarea id='popis' name='popis' rows='8' cols='100' placeholder='" + request.getParameter("popis") + "' required></textarea>");
        out.println("</div><br>");
        
        out.println("<button type=submit' class='btn btn-danger' name='action' value='upravitProduct'>Upravit</button>");
        out.println("</form>");

        out.println("</div>");
        out.println("</main>");
		
        footer();
	}
	void upravitProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			int id = Integer.parseInt(request.getParameter("id"));
			String nazov = request.getParameter("nazov");
	        String autor = request.getParameter("autor");
	        String seria = request.getParameter("seria");
	        double cena = Double.parseDouble(request.getParameter("cena"));
	        int mnozstvo = Integer.parseInt(request.getParameter("mnozstvo"));
	        String image = request.getParameter("image");
	        String kategoria = request.getParameter("kategoria");
	        int id_kategorie = getIdKategorie(request, response, kategoria);
	        String popis = request.getParameter("popis");
	        
	        Connection con = connector.dajSpojenie(request);
	        
	        String sql = "UPDATE knihy SET nazov = ?, author = ?, seria = ?, cena = ?, mnozstvo = ?, img = ?, id_kategorie = ?, popis = ? WHERE id = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, nazov);
	        pstmt.setString(2, autor);
	        pstmt.setString(3, seria);
	        pstmt.setDouble(4, cena);
	        pstmt.setInt(5, mnozstvo);
	        pstmt.setString(6, image);
	        pstmt.setInt(7, id_kategorie);
	        pstmt.setString(8, popis);
	        pstmt.setInt(9, id);
	        pstmt.executeUpdate();
	        pstmt.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    showProducts(request, response);
	}
	int getIdKategorie(HttpServletRequest request, HttpServletResponse response, String nazov_kategorie) throws ServletException, IOException {
		int id_kategorie = -1;
		try {
			Connection con = connector.dajSpojenie(request);
	        
	        String sql = "SELECT id FROM kategorie WHERE nazov = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setString(1, nazov_kategorie);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	        	id_kategorie = rs.getInt("id");
	        }
	        pstmt.close();
	        rs.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return id_kategorie;
	}
	void odstranitUsera(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
	        Connection con = connector.dajSpojenie(request);
	        
	        String userID = request.getParameter("userID");
	        
            String deleteQuery = "DELETE FROM users WHERE id = ?";
            PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
            
            deleteStatement.setInt(1, Integer.parseInt(userID));
    
            deleteStatement.executeUpdate();
            deleteStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		showProducts(request, response);
	}
	void odstranitObjednavku(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
	        Connection con = connector.dajSpojenie(request);
	        
	        int cisloObjednavky = Integer.parseInt(request.getParameter("cisloObjednavky"));
	        
            String deleteQuery = "DELETE FROM objednavky WHERE id = ?";
            PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
            
            deleteStatement.setInt(1, cisloObjednavky);
    
            deleteStatement.executeUpdate();
            deleteStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		zobrazitObjednavky(request, response);
		//kusy vymazáva už pri vložení do košíka (ak odstráni kusy z košíka, kusy sa pridajú naspäť do skladu)
	}
	void odstranitProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
	        Connection con = connector.dajSpojenie(request);
	        
	        int id = Integer.parseInt(request.getParameter("id"));
	        
            String deleteQuery = "DELETE FROM knihy WHERE id = ?";
            PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
            
            deleteStatement.setInt(1, id);
    
            deleteStatement.executeUpdate();
            deleteStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		showProducts(request, response);
		//kusy vymazáva už pri vložení do košíka (ak odstráni kusy z košíka, kusy sa pridajú naspäť do skladu)
	}
	
	void header() {
	    out.println("<html><head><title>Zoznam kníh</title>");
	    out.println("<link rel='stylesheet' href='https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css'>");
	    out.println("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css'>");
	    out.println("<style>");
	    out.println("body { background-color: black; margin: 50px; min-height: 60vh; }");
	    out.println(".card-body { background-color: gray; }");
	    out.println(".card { border: 5px solid rgb(50, 50, 50); }");
	    out.println("main { padding-top: 6%; padding-bottom: 6%; }");
	    out.println("footer { position: sticky; top: 100%; }");
	    out.println("a { color: white; }");
	    out.println("</style>");
	    out.println("</head><body><header>");

	    out.println("<nav class='navbar navbar-expand-lg navbar-dark bg-dark fixed-top'>");
	    out.println("<div class='container p-1 fs-5 gap-5'>");
	    out.println("<a class='navbar-brand fs-5' href='mainServlet'><i class='bi bi-house' style='font-size: 28px;'></i></a>");
	    out.println("<button class='navbar-toggler' type='button' data-bs-toggle='collapse' data-bs-target='#navbarSupportedContent' aria-controls='navbarSupportedContent' aria-expanded='false' aria-label='Toggle navigation'>");
	    out.println("<span class='navbar-toggler-icon'></span>");
	    out.println("</button>");
	    out.println("<div class='collapse navbar-collapse' id='navbarSupportedContent'>");
	    out.println("<ul class='navbar-nav me-auto mb-2 mb-lg-0 gap-3'>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='adminServlet?action=showObjednavky'>Objednávky</a>");
	    out.println("</li>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='adminServlet?action=showPouzivatelia'>Používatelia</a>");
	    out.println("</li>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='adminServlet?action=zobrazitAddForm'>Pridať Položku</a>");
	    out.println("</li>");
	    out.println("</ul>");
	    out.println("<ul class='navbar-nav ml-auto gap-3'>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='kontakt.html'>Kontakt</a>");
	    out.println("</li>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='o_nas.html'>O nás</a>");
	    out.println("</li>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='mainServlet?action=logout'>Odhlásiť sa</a>");
	    out.println("</li>");
	    out.println("</ul>");
	    out.println("</div>");
	    out.println("</div>");
	    out.println("</nav></header>");
	}
	void footer() {
		out.println("<footer class='footer mt-auto py-3 bg-dark text-center'>");
		out.println("<div class='container'>");
		out.println("<span class='text-light'>© 2024 UKF AI. Všetky práva vyhradené.</span>");
		out.println("<ul class='list-inline mt-4' style='font-size: 1.2em;'>");
		out.println("<li class='list-inline-item mx-3'><a href='mainServlet'>Domov</a></li>");
		out.println("<li class='list-inline-item mx-3'><a href='kontakt.html'>Kontakt</a></li>");
		out.println("<li class='list-inline-item mx-3'><a href='o_nas.html'>O nás</a></li>");
		out.println("</ul>");
		out.println("</div>");
		out.println("</footer>");

	    out.println("<script>");
	    out.println("const urlParams = new URLSearchParams(window.location.search);");
	    out.println("const message = urlParams.get('message');");
	    out.println("if (message == 'success') { alert('Úspešne prihlásený'); }");
	    out.println("else if (message == 'msg') { alert('Úspešne odoslaná správa'); }");
	    out.println("else if (message == 'admin') { alert('Vitaj admin!'); }");
	    out.println("</script>");
	    out.println("<script src='https://code.jquery.com/jquery-3.5.1.slim.min.js'></script>");
	    out.println("<script src='https://cdn.jsdelivr.net/npm/@popperjs/core@2.5.4/dist/umd/popper.min.js'></script>");
	    out.println("<script src='https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js'></script>");
	    out.println("</body></html>");
	}
}
