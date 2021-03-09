package endpoints;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import utilities.ConnManager;
import utilities.Constants;
import utilities.Hasher;
import utilities.PropertiesReader;

/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static PropertiesReader pr = PropertiesReader.getInstance();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		try {
			JSONObject body = new JSONObject(IOUtils.toString(request.getReader()));
			
			String email = body.getString("email"),
					password = body.getString("password");
			
			if (email.equals("") || password.equals("")) {
				response.setStatus(400);
				out.write("Por favor envie los parametros adecuados.");
				return;
			}
			
			try (Connection conn = ConnManager.getConnection(); PreparedStatement stm = conn.prepareStatement(pr.getValue(Constants.FETCH_USER_QUERY))) {
				stm.setString(1, email);
				ResultSet rs = stm.executeQuery();
				
				if (!rs.next()) {
					response.setStatus(404);
					out.write("El usuario no existe.");
				} else if (rs.getString("password").equals(Hasher.getHash(password))) {
					
					HttpSession session = request.getSession();
					session.setAttribute("email", rs.getString("email"));
					session.setAttribute("id", rs.getString("id"));
					
					response.setStatus(200);
					out.write("Login exitoso.");
				} else {
					response.setStatus(401);
					out.write("Datos incorrectos.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			out.write("Ha ocurrido un error en el servidor: " + e);
		}
	}

}
