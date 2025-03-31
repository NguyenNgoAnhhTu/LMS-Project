package com.example.location;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

//    public void saveLocation(Location location) {
//        // Check if the location exists
//        Optional<Location> existingLocation = locationRepository.findById(Math.toIntExact(location.getId()));
//        if (existingLocation.isPresent()) {
//            // Update the existing location
//            Location updatedLocation = existingLocation.get();
//            updatedLocation.setName(location.getName());
//            updatedLocation.setAddress(location.getAddress());
//            locationRepository.save(updatedLocation);
//        } else {
//            // Save as a new location
//            locationRepository.save(location);
//        }
//    }
    public Location getLocationById(Integer id) {
        return locationRepository.findById(id).orElse(null);
    }

    public Location createLocation(String locationName) {
        Location location = new Location();
        location.setName(locationName);
        return locationRepository.save(location);
    }

    public Location updateLocation(Integer id, String locationName) {
        Optional<Location> existingLocation = locationRepository.findById(id);
        if (existingLocation.isPresent()) {
            Location location = existingLocation.get();
            location.setName(locationName);
            return locationRepository.save(location);
        }
        return null;
    }

    public void deleteLocation(Integer id) {
        locationRepository.deleteById(id);
    }

    public void saveAll(List<Location> locations) {
        locationRepository.saveAll(locations);  // Save all locations at once
    }
    // Method to fetch paginated locations
    public Page<Location> getLocations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationRepository.findAll(pageable);
    }
    public Page<Location> searchLocations(String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
    }

    // Import locations from an Excel file
    public List<Location> importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            List<Location> locations = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) { // Start from 1 to skip the header row
                Row row = sheet.getRow(i);
                if (row != null) {
                    String locationAddress = row.getCell(2).getStringCellValue();
                    String locationName = row.getCell(1).getStringCellValue(); // Assume location name is in column 01

                    Cell cell = row.getCell(1); // Assume location name is in column 01

                    if (cell != null) {
                        // Check if the location already exists
                        if (!locationExists(locationName)) {
                            Location location = new Location();
                            location.setName(locationName);
                            location.setAddress(locationAddress);
                            locations.add(location);
                        }
                    }
                }
            }

            // Save locations to the database
            return locationRepository.saveAll(locations); // Assuming you have a locationRepository bean
        } catch (IOException e) {
            throw new RuntimeException("Error importing locations from Excel", e);
        }
    }

    private boolean locationExists(String locationName) {
        return locationRepository.findByName(locationName).isPresent(); // Assuming you have this method in LocationRepository
    }

    // Method to export locations to Excel
    public ByteArrayInputStream exportLocationsToExcel(List<Location> locations) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Locations");

        // Create the header row
        String[] headers = { "Location ID", "Location Name", "Location Address" };
        sheet.createRow(0).createCell(0).setCellValue(headers[0]);
        sheet.getRow(0).createCell(1).setCellValue(headers[1]);
        sheet.getRow(0).createCell(2).setCellValue(headers[2]);

        // Populate data rows
        int rowNum = 1;
        for (Location location : locations) {
            sheet.createRow(rowNum).createCell(0).setCellValue(location.getId().toString());
            sheet.getRow(rowNum).createCell(1).setCellValue(location.getName());
            sheet.getRow(rowNum).createCell(2).setCellValue(location.getAddress());
            rowNum++;
        }

        // Write to ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public boolean isLocationNameExists(String locationName) {
        return this.locationExists(locationName);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }
}
