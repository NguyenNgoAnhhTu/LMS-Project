package com.example.location;


import com.example.module.Module;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public String listLocations(Model model,
                              @RequestParam(value = "searchQuery", required = false) String searchQuery,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Page<Location> locations;
        Pageable pageable = PageRequest.of(page, pageSize);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            locations = locationService.searchLocations(searchQuery, page, pageSize);
        } else {
            locations = locationService.getLocations(page, pageSize);
        }

        model.addAttribute("locations", locations.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", locations.getTotalPages());
        model.addAttribute("totalItems", locations.getTotalElements());
        model.addAttribute("searchQuery", searchQuery);
        // add attribute for layout
        model.addAttribute("content","location/list");
        return "layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("locations", locationService.getAllLocations());
        model.addAttribute("content", "location/create");
        return "layout";
    }

    // Create new location
    @PostMapping("/create")
    public String createLocation(@ModelAttribute Location location) {
        locationService.saveLocation(location);
        return "redirect:/locations";
    }

    // Show edit form for a specific location
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("location", locationService.getLocationById(id));
        model.addAttribute("content", "location/edit");
        return "layout";
    }

    // Update existing location
    @PostMapping("/edit/{id}")
    public String updateLocation(@PathVariable("id") Integer id, @ModelAttribute Location location, Model model) {
        location.setId(Long.valueOf(id)); // Ensure the id is set correctly
        locationService.saveLocation(location); // Save the location
        return "redirect:/locations";
    }

    // Delete a location
    @GetMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return "redirect:/locations";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportLocations() {
        // Fetch all roles (page size set to max to get all records)
        List<Location> locations = locationService.getAllLocations();

        // Convert roles to Excel (assumes you have a service for that)
        ByteArrayInputStream excelFile = locationService.exportLocationsToExcel(locations);

        // Create headers for the response (Content-Disposition to trigger file download)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=locations.xlsx");

        // Return the file in the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(excelFile));
    }

    @PostMapping("/import")
    public String importLocations(@RequestParam("file") MultipartFile file) {
        locationService.importExcel(file);
        return "redirect:/locations";
    }


    // Print roles page
    @GetMapping("/print")
    public String printLocations(Model model) {
        List<Location> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);
        return "location/print";
    }
}
