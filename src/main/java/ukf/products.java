package ukf;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class products extends HttpServlet {
	private static final long serialVersionUID = 1L;
	getConnection connector;
	PrintWriter out;
       
    public products() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		connector = (getConnection) session.getAttribute("connector");
		int userID = (int) session.getAttribute("userID");
		
		String id = request.getParameter("id");
		String action = request.getParameter("action");
		response.setContentType("text/html");
		out = response.getWriter();

		if (action == null) { showProducts(request, response); return; }	
		switch(action) {
			case "add":
		        if (userID == 0) { response.sendRedirect("mainServlet"); } 	//vráti sa na login s chybovou hláškou
		        try {
						pridatDoKosika(request, response);
		        } catch (Exception e) {
		            response.sendRedirect("mainServlet"); 					//vráti sa na login s chybovou hláškou
		        }
				break;
			case "objednat":
		        if (userID == 0) { response.sendRedirect("mainServlet"); } 	//vráti sa na login s chybovou hláškou
		        try { 														//kontrola dostatku tovaru prebieha v inej časti kódu, v tomto prípade nie je potreba
		        	synchronized (this) {
		        		if (polozkyDostupne(request, response)) {
		        			objednat(request, response);
		        		}
		        	}
		        } catch (Exception e) {
		            response.sendRedirect("mainServlet"); 					//vráti sa na login s chybovou hláškou
		        }
			case "showProduct":
				showProduct(id, request, response);
				break;
			case "showObjednavky":
				zobrazitObjednavky(request, response);
				break;
			case "showKosik":
				zobrazitKosik(request, response);
				break;
			case "odstranit":
				vymazKosik(request, response);
				zobrazitKosik(request, response);
				break;
			default:
				showProducts(request, response);
				break;
		}
		
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
	            out.println("<div class='row'>");
	            out.println("<div class='col'>");
	            String seria = rs.getString("seria");
	            if (seria != null && !seria.isEmpty()) {
	                out.println("<h1 class='pb-3'>" + seria + "</h1>");
	            }
	            out.println("<h2 class='pb-3'>" + rs.getString("nazov") + "</h2>");
	            out.println("<h3 class='text-white-50 pt-5'>Autor: " + rs.getString("author") + "</h3>");

	            out.println("<h5 class='pt-5'>Žáner: " + rs.getString("nazov_kategorie") + "</h5>");
	            out.println("<h5 class='pb-2'>Cena: " + rs.getDouble("cena") + " €</h5>");

	            int mnozstvo = rs.getInt("mnozstvo");
	            if (mnozstvo > 1) {
	                out.println("<form method='get' action='products'>");
	                out.println("<input type='hidden' name='idPolozky' value='" + id + "'>");
	                out.println("<input type='hidden' name='cena' value='" + rs.getDouble("cena") + "'>");
	                out.println("<label for='customRange1' class='form-label'>Počet: </label>");
	                out.println("<output name='currentValue' id='currentValue'>1</output>"); //kontrola tovaru pomocou range a sync
	                out.println("<input type='range' class='form-range' min='1' max='" + mnozstvo + "' id='customRange1' name='mnozstvo' value='1' oninput='currentValue.value = this.value'>");
	                out.println("<input type='hidden' name='celkoveMnozstvo' value='" + mnozstvo + "'>"); //celkove mnozstvo, kvôli updatnutiu kosika
	                out.println("<button type='submit' name='action' value='add' class='btn btn-primary'>Pridať do košíka</button>");
	                out.println("</form>");
	            }
	            out.println("</div>");
	            out.println("<div class='col-md-3 mt-3 text-end'>");
	            out.println("<img src='" + rs.getString("img") + "' height='250' alt='" + rs.getString("nazov") + "'>");
	            out.println("</div></div><br><hr><br>");
	            out.println("<div><div>");
	            out.println("<p class='fs-1'>" + rs.getString("popis") + "</p>");
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
	            out.println("<a href='products?action=showProduct&id=" + rs.getString("id") + "'>");
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
	void pridatDoKosika(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    HttpSession session = request.getSession();
	    String idPolozky = request.getParameter("idPolozky");
	    int userID = (int) session.getAttribute("userID");
	    int mnozstvo = Integer.parseInt(request.getParameter("mnozstvo"));
	    double cena = Double.parseDouble(request.getParameter("cena"));
	    
	    try {
	        Connection con = connector.dajSpojenie(request);

	        // Over, či je položka už v košíku pre daného používateľa
	        String checkQuery = "SELECT * FROM kosik WHERE id_usera=? AND id_polozky=?";
	        PreparedStatement checkStatement = con.prepareStatement(checkQuery);
	        checkStatement.setInt(1, userID);
	        checkStatement.setString(2, idPolozky);
	        ResultSet checkResult = checkStatement.executeQuery();
	        
	        if (checkResult.next()) {
	            String updateQuery = "UPDATE kosik SET mnozstvo=?, cena=? WHERE id_usera=? AND id_polozky=?";
	            PreparedStatement updateStatement = con.prepareStatement(updateQuery);

	            int mnozstvoInDB = checkResult.getInt("mnozstvo");
	            int aktualneMnozstvo = mnozstvoInDB + mnozstvo;
	            
	            updateStatement.setInt(1, aktualneMnozstvo);
	            updateStatement.setDouble(2, aktualneMnozstvo * cena);
	            updateStatement.setInt(3, userID);
	            updateStatement.setString(4, idPolozky);
	            updateStatement.executeUpdate();
	        } else {
	            // Položka nie je v košíku, pridáme ju
	            String insertQuery = "INSERT INTO kosik (id_polozky, id_usera, cena, mnozstvo) VALUES (?, ?, ?, ?)";
	            PreparedStatement insertStatement = con.prepareStatement(insertQuery);
	            insertStatement.setString(1, idPolozky);
	            insertStatement.setInt(2, userID);
	            insertStatement.setDouble(3, cena * mnozstvo);
	            insertStatement.setInt(4, mnozstvo);
	            insertStatement.executeUpdate();
	        }

	        checkResult.close();
	        checkStatement.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    zobrazitKosik(request, response);
	}

	void zobrazitKosik(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();

	    try {
	        Connection con = connector.dajSpojenie(request);
	        String selectQuery = "SELECT knihy.id, knihy.nazov, kosik.mnozstvo, kosik.cena, knihy.mnozstvo AS celkovy_pocet_knih FROM kosik INNER JOIN knihy ON kosik.id_polozky = knihy.id WHERE id_usera=?";
	        PreparedStatement selectStatement = con.prepareStatement(selectQuery);
	        HttpSession session = request.getSession();
	        int userID = (int) session.getAttribute("userID");
	        selectStatement.setInt(1, userID);
	        ResultSet cartItems = selectStatement.executeQuery();

	        out.println("<main>");
	        out.println("<div class='container bg-secondary rounded p-4'>");
	        out.println("<h1 class='mt-5 mb-4 text-light'>Košík</h1>");

	        out.println("<div class='row'>");
	        out.println("<div class='col-md-8'>");
	        out.println("<table class='table'>");
	        out.println("<thead class='table-dark'>");
	        out.println("<tr>");
	        out.println("<th scope='col'>Názov</th>");
	        out.println("<th scope='col'>Množstvo</th>");
	        out.println("<th scope='col'>Cena</th>");
	        out.println("<th scope='col'>Akcie</th>");
	        out.println("</tr>");
	        out.println("</thead>");
	        out.println("<tbody>");

	        double celkovaCena = 0.0;

	        while (cartItems.next()) {
	            int polozkaID = cartItems.getInt("id");
	            String nazov = cartItems.getString("nazov");
	            int mnozstvo = cartItems.getInt("mnozstvo");
	            double cena = cartItems.getDouble("cena");
	            int mnozstvoToUpdate = cartItems.getInt("celkovy_pocet_knih") + mnozstvo;

	            out.println("<tr>");
	            out.println("<td>" + nazov + "</td>");
	            out.println("<td>" + mnozstvo + "</td>");
	            out.println("<td>" + cena + "</td>");
	            celkovaCena += cena;
	            out.println("<td><form action='products' method='get'>" +
	                    "<input type='hidden' name='itemID' value='" + polozkaID + "'>" +
	                    "<input type='hidden' name='userID' value='" + userID + "'>" +
	                    "<input type='hidden' name='mnozstvoToUpdate' value='" + mnozstvoToUpdate + "'>" +
	                    "<input type='hidden' name='action' value='odstranit'>" +
	                    "<input type='submit' value='Odstrániť'>" +
	                    "</form></td>");
	            out.println("</tr>");
	        }
	        double zlava = 0;
	        double zlavnenaCena;
	        String sql = "SELECT zlava FROM users WHERE id = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, userID);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	        	zlava = rs.getDouble("zlava");
	        }
	        zlava = (celkovaCena * zlava) / 100;
	        zlavnenaCena = celkovaCena - zlava; 	//odpočet zlavy
	        zlavnenaCena = Math.round(zlavnenaCena * 100.0) / 100.0;

	        out.println("<tr>");
	        out.println("<td><b>Celková cena:</b></td>");
	        out.println("<td></td>");
	        out.println("<td><b>" + celkovaCena + "</b></td>");
	        out.println("<td></td>");
	        out.println("</tr>");
	        
	        out.println("<tr>");
	        out.println("<td><b>Zľava:</b></td>");
	        out.println("<td></td>");
	        out.println("<td><b>" + zlava + "</b></td>");
	        out.println("<td></td>");
	        out.println("</tr>");
	        
	        out.println("<tr>");
	        out.println("<td><b>Cena po zľave:</b></td>");
	        out.println("<td></td>");
	        out.println("<td><b>" + zlavnenaCena + "</b></td>");
	        out.println("<td></td>");
	        out.println("</tr>");

	        out.println("</tbody>");
	        out.println("</table>");

	        out.println("<form action='products' method='get'>");
	        out.println("<input type='hidden' name='cena' value='" + zlavnenaCena + "'>");
	        out.println("<input type='submit' name='action' value='objednat' class='btn btn-primary' value='Objednať'>");
	        out.println("</form>");

	        out.println("</div>");
	        out.println("</div>");

	        out.println("</div>");
	        out.println("</main>");

	        cartItems.close();
	        selectStatement.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    footer();
	}
	boolean polozkyDostupne(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Integer> mnozstvoToUpdate = new ArrayList<>();
		ArrayList<Integer> idsToUpdate = new ArrayList<>();
		
		HttpSession session = request.getSession();
		boolean polozkyDostupne = false;
		
		int userID = (int) session.getAttribute("userID");
		
		try {
			Connection con = connector.dajSpojenie(request);
			
			String sql = "SELECT * FROM kosik WHERE id_usera = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, userID);
	        ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id_polozky = rs.getInt("id_polozky");
				
				String product_sql = "SELECT * FROM knihy WHERE id = ?";
				PreparedStatement product_pstmt = con.prepareStatement(product_sql);
				product_pstmt.setInt(1, id_polozky);
				ResultSet product_rs = product_pstmt.executeQuery();
				
				if (product_rs.next()) {
					int pocet_polozky_v_kosiku = rs.getInt("mnozstvo");
					int pocet_polozky_v_sklade = product_rs.getInt("mnozstvo");
					
					mnozstvoToUpdate.add(pocet_polozky_v_sklade - pocet_polozky_v_kosiku); 
					idsToUpdate.add(id_polozky);
					
					if (pocet_polozky_v_kosiku <= pocet_polozky_v_sklade) {
						polozkyDostupne = true;
					} else {
						return false;
					}
				}
				product_pstmt.close();
				product_rs.close();
			}
			pstmt.close();
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		if (polozkyDostupne) {
			for (int i = 0; i < idsToUpdate.size(); i++) {
				updateMnozstvoTovaru(request, response, idsToUpdate.get(i), mnozstvoToUpdate.get(i));
			}
		}
		return polozkyDostupne;
	}
	
	void updateMnozstvoTovaru(HttpServletRequest request, HttpServletResponse response, int id_knihy, int mnozstvoToUpdate) throws ServletException, IOException {
	    try {
	        Connection con = connector.dajSpojenie(request);

	        String sql = "UPDATE knihy SET mnozstvo = ? WHERE id = ?";
	        PreparedStatement pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, mnozstvoToUpdate);
	        pstmt.setInt(2, id_knihy);
	        pstmt.executeUpdate();

	        pstmt.close();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	void objednat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    HttpSession session = request.getSession();
	    LocalDate localDate = java.time.LocalDate.now();
        
	    int userID = (int) session.getAttribute("userID");
	    double cena = Double.parseDouble(request.getParameter("cena"));
        String datum = localDate.toString();
        String stav = "spracuje sa";
        
	    try {
	        Connection con = connector.dajSpojenie(request);

            String insertQuery = "INSERT INTO objednavky (id_user, cena, datum, stav) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = con.prepareStatement(insertQuery);
            insertStatement.setInt(1, userID);
            insertStatement.setDouble(2, cena);
            insertStatement.setString(3, datum);
            insertStatement.setString(4, stav);
            insertStatement.executeUpdate();
            insertStatement.close();
            
            vymazKosik(request, response);
            zobrazitObjednavky(request, response);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}	
	
	public void vymazKosik(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    try {
	        Connection con = connector.dajSpojenie(request);

	        String itemID = request.getParameter("itemID");
	        String userID = request.getParameter("userID");
	        String mnozstvoToUpdate = request.getParameter("mnozstvoToUpdate");

	        if (itemID == null || userID == null || mnozstvoToUpdate == null) {
	            String deleteAllQuery = "DELETE FROM kosik";
	            Statement deleteAllStatement = con.createStatement();
	            deleteAllStatement.executeUpdate(deleteAllQuery);
	            deleteAllStatement.close();
	        } else { 		//Vymaze z kosika konkretny tovar 
	            String deleteQuery = "DELETE FROM kosik WHERE id_usera = ? AND id_polozky = ?";
	            PreparedStatement deleteStatement = con.prepareStatement(deleteQuery);
	            
	            int uID = Integer.parseInt(userID);
	            deleteStatement.setInt(1, uID);
	            int iID = Integer.parseInt(itemID);
	            deleteStatement.setInt(2, iID);
	    
	            deleteStatement.executeUpdate();
	            deleteStatement.close();
	            
	            updateMnozstvoTovaru(request, response, iID, Integer.parseInt(mnozstvoToUpdate));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	void zobrazitObjednavky(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    header();

	    try {
	        Connection con = connector.dajSpojenie(request);
	        HttpSession session = request.getSession();
	        int userID = (int) session.getAttribute("userID");

	        String selectQuery = "SELECT * FROM objednavky WHERE id_user=?";
	        PreparedStatement selectStatement = con.prepareStatement(selectQuery);
	        selectStatement.setInt(1, userID);
	        
	        ResultSet orders = selectStatement.executeQuery();

	        out.println("<main>");
	        out.println("<div class='container bg-secondary rounded rounded-4 p-4'>");
	        out.println("<h1 class='mt-5 mb-4'>Moje objednávky</h1>");

	        out.println("<table class='table'>");
	        out.println("<thead class='table-dark'>");
	        out.println("<tr>");
	        out.println("<th scope='col'>Číslo objednávky</th>");
	        out.println("<th scope='col'>Cena</th>");
	        out.println("<th scope='col'>Dátum</th>");
	        out.println("<th scope='col'>Stav</th>");
	        out.println("</tr>");
	        out.println("</thead>");
	        out.println("<tbody>");

	        while (orders.next()) {
	            out.println("<tr>");
	            out.println("<td>" + orders.getInt("id") + "</td>");
	            out.println("<td>" + orders.getDouble("cena") + "</td>");
	            out.println("<td>" + orders.getString("datum") + "</td>");
                out.println("<td>" + orders.getString("stav") + "</td>");
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
	    out.println("<a class='nav-link active' aria-current='page' href='products?action=showObjednavky'>Objednávky</a>");
	    out.println("</li>");
	    out.println("<li class='nav-item'>");
	    out.println("<a class='nav-link active' aria-current='page' href='products?action=showKosik'>Košík</a>");
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
	    out.println("</script>");
	    out.println("<script src='https://code.jquery.com/jquery-3.5.1.slim.min.js'></script>");
	    out.println("<script src='https://cdn.jsdelivr.net/npm/@popperjs/core@2.5.4/dist/umd/popper.min.js'></script>");
	    out.println("<script src='https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js'></script>");
	    out.println("</body></html>");
	}
}
