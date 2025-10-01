-- Sample data for testing the room booking service

-- Insert sample rooms
INSERT INTO rooms (name, capacity, description, location, is_active, created_at, updated_at) VALUES
('Conference Room A', 10, 'Large conference room with projector and whiteboard', 'First Floor', true, NOW(), NOW()),
('Study Room 1', 4, 'Small quiet study room for group work', 'Second Floor', true, NOW(), NOW()),
('Meeting Room B', 6, 'Medium meeting room with video conferencing', 'First Floor', true, NOW(), NOW()),
('Presentation Hall', 50, 'Large hall for presentations and seminars', 'Ground Floor', true, NOW(), NOW()),
('Study Pod 2', 2, 'Private study space for two people', 'Third Floor', true, NOW(), NOW());

-- Insert sample facilities
INSERT INTO room_facilities (room_id, facility) VALUES
(1, 'Projector'),
(1, 'Whiteboard'),
(1, 'Video Conferencing'),
(1, 'WiFi'),
(2, 'Whiteboard'),
(2, 'WiFi'),
(3, 'Video Conferencing'),
(3, 'Projector'),
(3, 'WiFi'),
(4, 'Projector'),
(4, 'Audio System'),
(4, 'Video Conferencing'),
(4, 'WiFi'),
(5, 'WiFi'),
(5, 'Power Outlets');