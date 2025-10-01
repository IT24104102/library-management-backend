package com.hettiarachchi.roomservice.repository;

import com.hettiarachchi.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByIsActiveTrue();
    
    Optional<Room> findByIdAndIsActiveTrue(Long id);
    
    Optional<Room> findByName(String name);
    
    @Query("SELECT r FROM Room r WHERE r.isActive = true AND r.capacity >= :minCapacity")
    List<Room> findAvailableRoomsWithMinCapacity(Integer minCapacity);
    
    @Query("SELECT r FROM Room r JOIN r.facilities f WHERE r.isActive = true AND f IN :facilities")
    List<Room> findRoomsByFacilities(List<String> facilities);
}