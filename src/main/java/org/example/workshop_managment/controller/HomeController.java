//package org.example.workshop_managment.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class HomeController {
//
//    @GetMapping("/")
//    public String home() {
//        return "Workshop Management API is running";
//    }
//
//    @GetMapping("/healthz")
//    public String health() {
//        return "ok";
//    }
//}

package org.example.workshop_managment.controller;

import org.example.workshop_managment.model.WorkshopItem; // Import your Model class
import org.springframework.stereotype.Controller; // Use @Controller
import org.springframework.ui.Model; // Import Model
import org.springframework.web.bind.annotation.GetMapping;
// NOTE: Remove the import for org.springframework.web.bind.annotation.RestController

/**
 * Controller responsible for handling web requests and directing to the view.
 * This class implements the 'C' (Controller) part of the MVC pattern.
 */
@Controller // CHANGE 1: Replace @RestController with @Controller
public class HomeController {

    /**
     * Handles GET requests to the root URL ("/").
     * * @param model The Spring UI Model to carry data to the view.
     * @return The name of the template ("home") to render.
     */
    @GetMapping("/")
    public String home(Model model) { // CHANGE 2: Accept Model and return String (view name)

        // M: Create an instance of the Model
        // Assuming WorkshopItem.java is created in the model package
        WorkshopItem item = new WorkshopItem("Hydraulic Lift Pump", 1);

        // Pass data to the View
        model.addAttribute("welcomeMessage", "Workshop Management System Dashboard");
        model.addAttribute("workshopItem", item);

        // V: Return the name of the view file (home.html)
        return "home";
    }

    // You can keep the /healthz endpoint, but it's typically moved to a dedicated
    // HealthController or requires the @ResponseBody annotation if you keep @Controller.
    // For a pure MVC example, we'll focus on the home method.
    /*
    @GetMapping("/healthz")
    @ResponseBody // Needs this if using @Controller instead of @RestController
    public String health() {
        return "ok";
    }
    */
}