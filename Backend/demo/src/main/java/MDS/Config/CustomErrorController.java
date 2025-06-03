package MDS.Config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    // Aceasta metoda va fi apelata automat de Spring cand apare o eroare
    // si se face redirect catre ruta "/error"

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        // Preia codul statusului HTTP (ex: 404, 500)
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        // Preia mesajul de eroare
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        // Preia exceptia aruncata
        Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        // Adauga codul de eroare in model
        if (status != null) {
            model.addAttribute("status", status.toString());
        }

        // Adauga mesajul de eroare in model
        if (message != null) {
            model.addAttribute("message", message.toString());
        }

        // Adauga o descriere generica a erorii
        if (error != null) {
            model.addAttribute("error", "Eroare în aplicație");
        }

        // Returneaza pagina "error.html"
        return "error";
    }
}