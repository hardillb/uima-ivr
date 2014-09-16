package uk.me.hardill.uima.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class Processor
 */
@WebServlet("/Processor")
public class ProcessorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private static Processor processor = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProcessorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	String path = config.getServletContext().getRealPath("/WEB-INF/resources/");
    	if (processor == null) {
    		processor = new Processor(path);
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String text = request.getParameter("text");
		if (processor != null && text != null) {
			JSONObject json = processor.process(text);
			
			response.setContentType("application/json");
			try {
				response.getWriter().print(json.toString(2));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.flushBuffer();
		}
	}

}
