/**
 * TraffiQ System Frontend Driver
 * Simulates custom Circular Queue & Min-Heap Priority Queue logic in the web client
 */
class TraffiQFrontendSystem {
    constructor() {
        this.lanes = {
            NORTH: [],
            EAST: [],
            SOUTH: [],
            WEST: []
        };
        this.laneKeys = ["NORTH", "EAST", "SOUTH", "WEST"];
        this.activeSignalIndex = 0;
        this.emergencyQueue = []; // Min-Heap representation
        this.totalEmergencyCleared = 0;
        this.totalRegularCleared = 0;
        
        this.initDOM();
        this.render();
    }

    initDOM() {
        document.getElementById('vehicleForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleRegister();
        });

        document.getElementById('btnAdvanceSignal').addEventListener('click', () => {
            this.advanceSignal();
        });

        document.getElementById('btnSearch').addEventListener('click', () => {
            this.handleSearch();
        });
    }

    handleRegister() {
        const plate = document.getElementById('licensePlate').value.trim().toUpperCase();
        const typeSelect = document.getElementById('vehicleType').value;
        const lane = document.getElementById('laneSelect').value;

        const isEmergency = typeSelect.startsWith('EMERGENCY');
        const priorityRank = this.getPriorityRank(typeSelect);

        const vehicle = {
            plate,
            typeSelect,
            priorityRank,
            lane,
            isEmergency,
            timestamp: Date.now()
        };

        // Linear Search check for duplicates
        if (this.searchVehicle(plate)) {
            this.log(`❌ ERROR: License plate [${plate}] is already queued!`, 'emergency-log');
            return;
        }

        if (isEmergency) {
            this.emergencyQueue.push(vehicle);
            // Re-heapify / Sort by Priority Rank then Timestamp (FIFO for ties)
            this.emergencyQueue.sort((a, b) => a.priorityRank - b.priorityRank || a.timestamp - b.timestamp);
            this.log(`🚨 EMERGENCY ENQUEUED: [${plate}] placed in Priority Queue for ${lane} Lane.`);
        } else {
            if (this.lanes[lane].length >= 8) {
                this.log(`⚠️ OVERFLOW ERROR: ${lane} Circular Queue is full (Max 8)! Vehicle rejected.`, 'emergency-log');
                return;
            }
            this.lanes[lane].push(vehicle);
            this.log(`✅ SUCCESS: Vehicle [${plate}] added to ${lane} Lane Circular Queue.`);
        }

        document.getElementById('licensePlate').value = '';
        this.render();
    }

    advanceSignal() {
        let logMsg = "";
        
        // 1. Emergency Preemption
        if (this.emergencyQueue.length > 0) {
            const emergencyVehicle = this.emergencyQueue.shift();
            this.totalEmergencyCleared++;
            logMsg = `⚡ EMERGENCY PREEMPTION: Granted immediate Green signal to [${emergencyVehicle.plate}] on ${emergencyVehicle.lane} Lane!`;
            document.getElementById('activeVehicleSpot').innerHTML = `<span style="color:#ef4444; font-weight:bold;">🚨 ${emergencyVehicle.plate} (EMERGENCY)</span>`;
            this.log(logMsg, 'emergency-log');
        } else {
            // 2. Regular Rotation
            const currentLane = this.laneKeys[this.activeSignalIndex];
            if (this.lanes[currentLane].length > 0) {
                const vehicle = this.lanes[currentLane].shift(); // Dequeue
                this.totalRegularCleared++;
                logMsg = `🚦 SIGNAL DISPATCH: Green Light on ${currentLane} Lane. Dequeued vehicle [${vehicle.plate}].`;
                document.getElementById('activeVehicleSpot').innerHTML = `<span style="color:#10b981; font-weight:bold;">🚗 ${vehicle.plate}</span>`;
            } else {
                logMsg = `🚦 SIGNAL PASSED: Green Light on ${currentLane} Lane, but queue was empty.`;
                document.getElementById('activeVehicleSpot').innerText = `Lane ${currentLane} Empty`;
            }
            
            this.activeSignalIndex = (this.activeSignalIndex + 1) % 4;
            this.log(logMsg);
        }

        this.render();
    }

    handleSearch() {
        const plate = document.getElementById('searchPlateInput').value.trim().toUpperCase();
        const resBox = document.getElementById('searchResult');
        if (!plate) return;

        const found = this.searchVehicle(plate);
        resBox.style.display = 'block';
        if (found) {
            resBox.style.background = '#065f46';
            resBox.style.color = '#a7f3d0';
            resBox.innerHTML = `<strong>FOUND:</strong> [${found.plate}] in ${found.isEmergency ? 'Emergency Priority Queue' : found.lane + ' Lane Queue'}`;
        } else {
            resBox.style.background = '#991b1b';
            resBox.style.color = '#fecaca';
            resBox.innerHTML = `<strong>NOT FOUND:</strong> Vehicle [${plate}] not in any active queue.`;
        }
    }

    searchVehicle(plate) {
        for (let v of this.emergencyQueue) {
            if (v.plate === plate) return v;
        }
        for (let lane of this.laneKeys) {
            for (let v of this.lanes[lane]) {
                if (v.plate === plate) return v;
            }
        }
        return null;
    }

    getPriorityRank(typeStr) {
        switch (typeStr) {
            case 'EMERGENCY_AMBULANCE': return 1;
            case 'EMERGENCY_FIRE': return 2;
            case 'EMERGENCY_POLICE': return 3;
            default: return 5;
        }
    }

    log(msg, className = '') {
        const consoleEl = document.getElementById('eventConsole');
        const entry = document.createElement('div');
        entry.className = `log-entry ${className}`;
        entry.innerText = `[${new Date().toLocaleTimeString()}] ${msg}`;
        consoleEl.prepend(entry);
    }

    render() {
        document.getElementById('activeSignalDisplay').innerText = this.laneKeys[this.activeSignalIndex];
        document.getElementById('centerSignalIndicator').innerText = `${this.laneKeys[this.activeSignalIndex]} GREEN`;
        document.getElementById('emergencyClearedCount').innerText = this.totalEmergencyCleared;
        document.getElementById('regularClearedCount').innerText = this.totalRegularCleared;

        // Render Circular Lane Queues
        this.laneKeys.forEach(lane => {
            const laneBox = document.getElementById(`lane${this.capitalize(lane)}Box`);
            const buffer = document.getElementById(`${lane.toLowerCase()}QueueBuffer`);
            const badge = document.getElementById(`${lane.toLowerCase()}Badge`);

            if (this.laneKeys[this.activeSignalIndex] === lane) {
                laneBox.classList.add('active-green');
            } else {
                laneBox.classList.remove('active-green');
            }

            badge.innerText = `${this.lanes[lane].length}/8`;
            buffer.innerHTML = '';
            this.lanes[lane].forEach(v => {
                const chip = document.createElement('div');
                chip.className = 'vehicle-chip';
                chip.innerText = v.plate;
                buffer.appendChild(chip);
            });
        });

        // Render Emergency Queue
        const emgList = document.getElementById('emergencyQueueList');
        if (this.emergencyQueue.length === 0) {
            emgList.innerHTML = `<p class="empty-msg">No active emergency vehicles in priority queue.</p>`;
        } else {
            emgList.innerHTML = '';
            this.emergencyQueue.forEach(v => {
                const chip = document.createElement('div');
                chip.className = 'vehicle-chip emergency';
                chip.innerText = `🚨 ${v.plate} (${v.lane})`;
                emgList.appendChild(chip);
            });
        }
    }

    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.traffiQApp = new TraffiQFrontendSystem();
});