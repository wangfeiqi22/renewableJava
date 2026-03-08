package com.renewable.ai.service;

import com.renewable.ai.entity.Station;
import com.renewable.ai.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public List<Station> getStationsByType(Integer type) {
        return stationRepository.findByType(type);
    }

    // Basic Euclidean distance (Mock for MVP)
    // In production, use PostGIS or Redis Geo
    public Station findNearestStation(double lat, double lon, Integer type) {
        List<Station> candidates = type == null ? stationRepository.findAll() : stationRepository.findByType(type);
        Station nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Station s : candidates) {
            if (s.getLat() == null || s.getLon() == null) continue;
            
            double dist = Math.sqrt(Math.pow(s.getLat().doubleValue() - lat, 2) + Math.pow(s.getLon().doubleValue() - lon, 2));
            if (dist < minDistance) {
                minDistance = dist;
                nearest = s;
            }
        }
        return nearest;
    }
}
