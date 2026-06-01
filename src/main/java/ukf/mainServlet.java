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

public class mainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    getConnection connector = new getConnection();

    public mainServlet() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	String action = request.getParameter("action");
        if (action == null) { action = ""; }

        switch(action) {
        	case "login":
        		session.setAttribute("connector", connector);
        		login(request, response);
                break;
        	case "register_form":
        		response.sendRedirect("register.html");
        		break;
        	case "register":
        		register(request, response);
        		break;
        	case "logout":
        		logout(request, response, "success");
        		break;
        	default:
        		login(request, response);
        		break;
        }
    }
    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");

        Connection con = connector.dajSpojenie(request);
        
        HttpSession session = request.getSession();

		String email = request.getParameter("email");
        String heslo = request.getParameter("heslo");

        if (session.getAttribute("prihlasenyPouzivatel") != null) {
        	String message = request.getParameter("message");		
        	Integer admin = (Integer) session.getAttribute("jeAdmin");		//Ak je po prihlásení admin, tak ...
        	if (admin == 1) {
        		response.sendRedirect("adminServlet"); return;		//Prechod do admin rozhrania
        	}
        	if (message != null) { response.sendRedirect("products?message=" + message); return;}
        	response.sendRedirect("products");
            return;
        }
        else if (email == null && heslo == null) {
        	logout(request, response, "1");
        	return;
        }
        else if (email == null) {
        	logout(request, response, "2");
        	return;
        }
        else if (heslo == null) {
        	logout(request, response, "3");
        	return;
        }
        
        else if (overPouzivatela(con, email, heslo, session)) {
        	Integer ban = (Integer) session.getAttribute("ban");
        	Integer admin = (Integer) session.getAttribute("jeAdmin");		//Ak je po prihlásení admin, tak ...
        	if (ban == 1) {
        		logout(request, response, "5"); return;
        	}
        	else if (admin == 1) {
        		response.sendRedirect("adminServlet?message=admin"); return;		//Prechod do admin rozhrania
        	}
        	response.sendRedirect("products?message=success");
            return;
        } else {
        	logout(request, response, "4");
        }
    }
    private void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");

        Connection con = connector.dajSpojenie(request);

        String meno = request.getParameter("meno");
        String priezvisko = request.getParameter("priezvisko");
        String email = request.getParameter("email");
        String heslo = request.getParameter("heslo");
        String adresa = request.getParameter("adresa");

        String heslo_repeat = request.getParameter("heslo_repeat");
        if (!heslo.equals(heslo_repeat)) {
        	response.sendRedirect("register.html?registerMessage=1");
        	return;
        }
        
        try {
        	String sql = "SELECT * FROM users WHERE email=?";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                    	response.sendRedirect("register.html?registerMessage=2");
                    	return;
                    }
                }
            }
        	
            sql = "INSERT INTO users (meno, priezvisko, email, heslo, adresa, zlava, poznamky, admin, ban) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, meno);
                statement.setString(2, priezvisko);
                statement.setString(3, email);
                statement.setString(4, heslo);
                statement.setString(5, adresa);
                statement.setString(6, "0");
                statement.setString(7, "");
                statement.setString(8, "0");
                statement.setString(9, "0");

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                	logout(request, response, "registration_success");
                	return;
                } else {
                	response.sendRedirect("register.html?registerMessage=3");
                	return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Chyba pri registrácii. Skúste to prosím znova.");
        }

        out.println("</body></html>");
    }
    
    private boolean overPouzivatela(Connection con, String email, String heslo, HttpSession session) {
        try {
            String sql = "SELECT * FROM users WHERE email=? AND heslo=?";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setString(2, heslo);
                try (ResultSet result = statement.executeQuery()) {
                	if (result.next()) {
                		session.setAttribute("ban", result.getInt("ban"));
                		session.setAttribute("userID", result.getInt("id"));
                        session.setAttribute("prihlasenyPouzivatel", email);
                        session.setAttribute("jeAdmin", result.getInt("admin"));
                        return true;
                	}
                	return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    void logout(HttpServletRequest request, HttpServletResponse response, String msg) throws IOException {
    	HttpSession session = request.getSession();
    	
    	closeConnection(request);
		session.invalidate();
    	response.sendRedirect("index.html?message=" + msg);
    	return;
    }
    
    void closeConnection(HttpServletRequest request)
    {
    	try {
    		HttpSession session = request.getSession();
    		Connection c = (Connection) session.getAttribute("spojenie");
            c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
