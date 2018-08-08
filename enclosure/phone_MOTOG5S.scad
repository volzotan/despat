/* MOTO G5S
 *
 * Max: [150, 73.5, 8.4]
 * About 1-1.5mm curvature on top and bottom sides
 * Distance middle usb port to edge: 36
 * length usb port: 8.1 (long side of port facing down)
 * corner radius: 10mm
 * curvature bottom: 12.5-8.5
 * button positions from bottom: 77 [10] 7 [20] 34
 * camera diameter: 25, camera center distance from top: 20
 *
 */

phoneDepth = 74;
phoneWidth = 150;
phoneHeight = 10;
lensHoleDistance = 25;
phoneName = "MOTO5GS";

translate_cavity = [sizeBot[0]-(phoneWidth)-6, 10, 7];
lensHole = [
    translate_cavity[0]+phoneWidth-lensHoleDistance, 
    translate_cavity[1]+phoneDepth/2
];

module phone() {
    translate([]) {
        block(phoneWidth, phoneDepth, phoneHeight, crad=10);
        translate([phoneWidth-lensHoleDistance, phoneDepth/2, -1]) cylinder($fn=32, d=10, h=3);
    }
}

module cavity(height) {
    translate([translate_cavity[0], translate_cavity[1], translate_cavity[2]]) {
        difference() {
            translate([0, 0]) block(phoneWidth, phoneDepth, height, crad=8);

            // holding nudge
            points = [[0, 0], [1, 1], [1, 10], [0, 10]];
            translate([phoneWidth-.1, phoneDepth+.1, 8.4-1]) rotate([90, 0, -90]) linear_extrude(height=phoneWidth) polygon(points);
        }
    
        // buttons
        translate([77-2, -4, 0]) block(39, 10, height, crad=2);
        
        // wedge
        translate([20, -10, 0]) cube([30, 11, height]);  
        
        // model name
        translate([20.5, 10.5, -0.3]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text(phoneName);
    }
}