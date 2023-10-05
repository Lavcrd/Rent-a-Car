function initializeMap(coordinates, zoomLevel) {
    var map = L.map('map').setView(coordinates, zoomLevel);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
    var marker = L.marker(coordinates).addTo(map);

}