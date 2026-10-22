# TraffiQ: Intelligent Intersection Prioritization
**Smart Traffic Signal and Emergency Vehicle Queue Management System**

## Project Overview

TraffiQ is a traffic management simulation designed to organize vehicles at a four-way intersection. It uses data structures to manage lane-wise traffic, simulate signal rotation, and prioritize emergency vehicles.

## Objectives

- Manage vehicles arriving at different intersection lanes.
- Apply Circular Queue concepts for regular traffic management.
- Use a Priority Queue to handle emergency vehicles.
- Simulate traffic signal rotation between four lanes.
- Search for vehicles using their license plate numbers.

## Features

- Vehicle registration with license plate, type, and lane.
- Four-lane traffic management: North, East, South, and West.
- Emergency vehicle prioritization.
- Traffic signal rotation simulation.
- Vehicle search functionality.
- Dashboard displaying queues, signal status, and event logs.

## Technologies Used

- **HTML:** Website structure.
- **CSS:** Interface design and styling.
- **JavaScript:** Website interaction and traffic simulation.
- **Java:** Separate implementation of traffic management data structures.

## Data Structures Used

### Circular Queue

The Java implementation uses a Circular Queue to manage vehicles in each lane in FIFO (First In, First Out) order.

### Priority Queue

The Java implementation uses a min-heap Priority Queue to process emergency vehicles according to their priority levels.

## Working Principle

1. Register a vehicle by entering its license plate, vehicle type, and lane.
2. The system classifies the vehicle as regular or emergency traffic.
3. Regular vehicles are added to their respective lane queues.
4. Emergency vehicles are placed in a priority queue.
5. When the signal advances, the system checks for emergency vehicles first.
6. If an emergency vehicle is waiting, it is processed before regular traffic.
7. Otherwise, a vehicle from the active lane is processed, and the signal rotates to the next lane.
8. The dashboard updates the queues and traffic event log.

## How to Run

1. Open the website using GitHub Pages or a web browser.
2. Register vehicles using the registration form.
3. Select the vehicle type and lane.
4. Advance the traffic signal to simulate vehicle movement.
5. Use the search option to find a vehicle by its license plate.

## Project Structure

- `index.html` – Website structure
- `style.css` – Website styling
- `script.js` – Browser-based traffic simulation
- `Main.java` – Separate Java data structures implementation
- `README.md` – Project documentation

## Implementation Note

The website uses JavaScript for browser-based simulation. The Java implementation is a separate program and is not directly connected to the website.

## Future Enhancements

- Integrate the website with a Java backend.
- Introduce traffic-density-based signal timing.
- Store traffic records for analysis.
- Generate traffic flow reports.

## Conclusion

TraffiQ demonstrates how Circular Queue and Priority Queue concepts can be applied to traffic management. It provides a simple simulation of lane management, signal rotation, and emergency vehicle prioritization.
## Output Screenshots

### Whole Website
![Whole](Whole%20website.png)

### Output 1
![Output 1](Output%201.png)

### Output 2
![Output 2](Output%202.png)
