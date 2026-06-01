package ukf;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class getConnection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String URL = "jdbc:mysql://localhost/kniznica_e-shop";
    private String LOGIN = "root";
    private String PWD = ""; 
    Guard g;
    
    public getConnection() {
        super();
    }

    protected Connection dajSpojenie(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            Connection c = (Connection) session.getAttribute("spojenie");

            if (c == null) {
            	Class.forName("com.mysql.cj.jdbc.Driver");
                c = DriverManager.getConnection(URL, LOGIN, PWD);
                session.setAttribute("spojenie", c);
                g = new Guard(c);
            }
            return c;
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

	public class Guard implements HttpSessionBindingListener {
	    Connection connection;
	    public Guard(Connection c) {
	    	connection = c;
	    }
	    @Override
	    public void valueBound(HttpSessionBindingEvent event) {
	    	
	    }
	    
	    @Override
	    public void valueUnbound(HttpSessionBindingEvent event) {
	    	try {
	    		if (connection != null) connection.close();
	    	}
	    	catch (Exception e) {
	    		
	    	}
	    }
	}

}
