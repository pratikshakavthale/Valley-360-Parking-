package com.app.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dto.ParkingAreaDTO;
import com.app.entities.ParkingArea;
import com.app.entities.User;
import com.app.enums.Status;
import com.app.exception.UserNotFoundException;
import com.app.repository.ParkingAreaRepository;
import com.app.repository.UserRepository;

@Service
@Transactional
public class ParkingAreaServiceImpl implements ParkingAreaService {

	@Autowired
	private ParkingAreaRepository parkingAreaRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ModelMapper mapper;
	
	private static final double EARTH_RADIUS = 6371; // Radius of the earth in km
	
	@Override
	public ParkingArea addParkingArea(ParkingAreaDTO parking) {
		User user = userRepository.findById(parking.getOwnerId())
				.orElseThrow(() -> new UserNotFoundException("Invalid id !!"));
		
		ParkingArea parkingArea = mapper.map(parking, ParkingArea.class);
		
		parkingArea.setUser(user);
			
		return parkingAreaRepository.save(parkingArea);
	}


//	@Override
//	public List<ParkingArea> getNearByParking(double lon, double lat) {
//		List<ParkingArea> list = parkingAreaRepository.findAll();
//		
//		List<ParkingArea> filteredList = list.stream().filter(l -> l.getLongitude() == lon && l.getLatitude() == lat)
//										.collect(Collectors.toList());
//		
//		return filteredList;
//	}


	@Override
	public List<ParkingAreaDTO> findNearbyParking(double currentLatitude, double currentLongitude, double radiusInKm) {
	    List<ParkingArea> allParkingAreas = parkingAreaRepository.findAll();

	    return allParkingAreas.stream()
	            .filter(parkingArea -> {
	                double distance = calculateDistance(currentLatitude, currentLongitude,
	                        parkingArea.getLatitude(), parkingArea.getLongitude());
	                return distance <= radiusInKm;
	            })
	            .map(this::convertToDTO)
	            .collect(Collectors.toList());
	}

	private ParkingAreaDTO convertToDTO(ParkingArea parkingArea) {
//	    ParkingAreaDTO dto = new ParkingAreaDTO();
//	    dto.setId(parkingArea.getId());
//	    dto.setAddress(parkingArea.getAddress());
//	    dto.setLatitude(parkingArea.getLatitude());
//	    dto.setLongitude(parkingArea.getLongitude());
//	    dto.setStatus(parkingArea.getStatus());
//	    dto.setOwnerId(parkingArea.getUser().getId());
//	    return dto;
		
		return mapper.map(parkingArea, ParkingAreaDTO.class);

	}


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c; // Distance in km
    }


	@Override
	public List<ParkingAreaDTO> findParkingAreaByStatus(Status status) {
		
		 List<ParkingArea> parkingAreas = parkingAreaRepository.findByStatus(status);
		 
		 return parkingAreas.stream()
	                .map(parkingArea -> mapper.map(parkingArea, ParkingAreaDTO.class))
	                .collect(Collectors.toList());
	}

}
