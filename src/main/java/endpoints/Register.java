package endpoints;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.UUID;

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
 * Servlet implementation class Register
 */

public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static PropertiesReader pr = PropertiesReader.getInstance();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
    }
    

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		try {
			JSONObject body = new JSONObject(IOUtils.toString(request.getReader()));
			
			String name = body.getString("name"), 
					lastName = body.getString("lastName"), 
					username = body.getString("username"), 
					email = body.getString("email"),
					password = body.getString("password");
			TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(body.getString("birthDate"));
		    Instant i = Instant.from(ta);
		    Date birthDate = Date.from(i);
		    
			if (name.equals("") || lastName.equals("") || username.equals("") || email.equals("") || password.equals("") || birthDate == null) {
				response.setStatus(400);
				out.write("Por favor envie los parametros adecuados.");
				return;
			}
			

			String userId = UUID.randomUUID().toString();
			try (
					Connection conn = ConnManager.getConnection(); 
					PreparedStatement stm = conn.prepareStatement(pr.getValue(Constants.FETCH_USER_QUERY));
					PreparedStatement stmCreate = conn.prepareStatement(pr.getValue(Constants.CREATE_USER_QUERY))
				) {
				
				stm.setString(1, email);
				ResultSet rs = stm.executeQuery();
				
				if (rs.next()) {
					response.setStatus(401);
					out.write("Ya existe un usuario con el mismo correo.");
					return;
				}
				
				stmCreate.setString(1, username);
				stmCreate.setString(2, name);
				stmCreate.setString(3, lastName);
				stmCreate.setString(4, email);
				stmCreate.setObject(5, birthDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
				stmCreate.setString(6, Hasher.getHash(password));
				stmCreate.setString(7, userId);
				stmCreate.execute();
			}
			
			HttpSession session = request.getSession();
			session.setAttribute("email", email);
			session.setAttribute("id", userId);
			
			response.setStatus(200);
			out.write("Se ha creado el usuario con exito.");
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			out.write("Ha ocurrido un error en el servidor: " + e);
		}
		
	}

}
