package endpoints;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.google.gson.Gson;

import models.UserModel;
import utilities.ConnManager;
import utilities.Constants;
import utilities.PropertiesReader;

/**
 * Servlet implementation class User
 */
public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static PropertiesReader pr = PropertiesReader.getInstance();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public User() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(false);
		response.setContentType("application/json");
		if (session == null) {
			response.setStatus(401);
			out.write("No tiene sesion iniciada.");
			return;
		}
		String email = (String) session.getAttribute("email");
		try (Connection conn = ConnManager.getConnection(); PreparedStatement stm = conn.prepareStatement(pr.getValue(Constants.FETCH_USER_QUERY))) {
			 stm.setString(1, email);
			 
			 ResultSet rs = stm.executeQuery();
			 
			 if (rs.next()) {
				 
				 UserModel user = new UserModel();
				 user.email = rs.getString("email");
				 user.name = rs.getString("name");
				 user.lastName = rs.getString("lastName");
				 user.birthDate = rs.getDate("birthDate");
				 user.username = rs.getString("username");
				 user.country = rs.getString("country")!= null ? rs.getString("country") : "";
				 user.phoneNumber = rs.getString("phoneNumber") != null ? rs.getString("phoneNumber") : "";
				 user.favoriteColor = rs.getString("favoriteColor")!= null ? rs.getString("favoriteColor") : "";
				 user.id = rs.getString("id");
				 
				 Gson gson = new Gson();
				 String jsonString = gson.toJson(user);
				 
				 response.setStatus(200);
				 out.write(jsonString);
			 }
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(500);
			out.write("Ha ocurrido un error en el servidor: " + e);
		}
	}

	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		try {
			JSONObject body = new JSONObject(IOUtils.toString(request.getReader()));
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(401);
				out.write("No tiene sesion iniciada.");
				return;
			}
			
			
			String id = body.getString("id"), 
					name = body.getString("name"), 
					lastName = body.getString("lastName"), 
					username = body.getString("username"), 
					email = body.getString("email"),
					phoneNumber = body.getString("phoneNumber"),
					favoriteColor = body.getString("favoriteColor"),
					country = body.getString("country");
			TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(body.getString("birthDate"));
		    Instant i = Instant.from(ta);
		    Date birthDate = Date.from(i);
			
			if (name.equals("") || lastName.equals("") || username.equals("") || email.equals("") || birthDate == null) {
				response.setStatus(400);
				out.write("Por favor envie los parametros adecuados.");
				return;
			}
			
			String userId = (String) session.getAttribute("id");
			if (!userId.equals(id)) {
				response.setStatus(401);
				out.write("No puede modificar el usuario sin iniciar sesion.");
				return;
			}
			
			try (Connection conn = ConnManager.getConnection(); PreparedStatement pm = conn.prepareStatement(pr.getValue(Constants.UPDATE_USER_QUERY))) {
				pm.setString(1, username);
				pm.setString(2, name);
				pm.setString(3, lastName);
				pm.setString(4, email);
				pm.setObject(5, birthDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
				pm.setObject(6, phoneNumber);
				pm.setString(7, favoriteColor);
				pm.setString(8, country);
				pm.setString(9, id);
				
				pm.execute();
			} catch (Exception e) {
				throw e;
			}
			
			session.setAttribute("email", email);
			response.setStatus(200);
			out.write("El usuario ha sido actualizado con exito.");
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			out.write("Ha ocurrido un error en el servidor: " + e);
		}
		
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		try {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(401);
				out.write("No tiene sesion iniciada.");
				return;
			}
			
			String id = request.getParameter("id");
			
			if (id.equals("") || id == null) {
				response.setStatus(400);
				out.write("Por favor envie los parametros adecuados.");
				return;
			}
			
			String userId = (String) session.getAttribute("id");
			if (!userId.equals(id)) {
				response.setStatus(401);
				out.write("No puede modificar el usuario sin iniciar sesion.");
				return;
			}
			
			try (Connection conn = ConnManager.getConnection(); PreparedStatement pm = conn.prepareStatement(pr.getValue(Constants.DELETE_USER_QUERY))) {
		
				pm.setString(1, id);
				
				pm.execute();
			} catch (Exception e) {
				throw e;
			}
			
			session.invalidate();
			
			response.setStatus(200);
			out.write("El usuario ha sido eliminado con exito.");
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			out.write("Ha ocurrido un error en el servidor: " + e);
		}
	}
}
