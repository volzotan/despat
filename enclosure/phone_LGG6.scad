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

phoneDepth = 71.9;
phoneWidth = 148.9;
phoneHeight = 8.6; //7.9;
lensHoleDistance = 18;
phoneName = "LG G6";

tol_depth = 0.4;

translate_cavity = [sizeBot[0]-(phoneWidth)-6, 10, 7];
lensHole = [
    translate_cavity[0]+phoneWidth-lensHoleDistance, 
    translate_cavity[1]+phoneDepth/2-3
];

module phone() {
    translate([]) difference() {
        union() {
            block(phoneWidth, phoneDepth, phoneHeight, crad=10);
            translate([phoneWidth-22-26, phoneDepth, 2]) cube([22, 1, 4]);
        }
        translate([phoneWidth-lensHoleDistance, phoneDepth/2-9, -1]) cylinder($fn=32, d=8, h=10);
        translate([phoneWidth-lensHoleDistance, phoneDepth/2+9, -1]) cylinder($fn=32, d=8, h=10);
    }
}

module cavity(height) {
    translate([translate_cavity[0], translate_cavity[1], translate_cavity[2]]) {
        difference() {
            translate([0, 0]) block(phoneWidth, phoneDepth+tol_depth, height, crad=8);

            // holding nudge
            points = [[0, 0], [1, 1], [1, 10], [0, 10]];
            translate([phoneWidth-.1, phoneDepth+tol_depth+.1, 7.1]) rotate([90, 0, -90]) linear_extrude(height=phoneWidth) polygon(points);
        }
    
        // buttons
        translate([99.25, +64, 0]) block(22+3, 10, height, crad=2);
        
        // wedge
        translate([20, -10, 0]) cube([30, 11, height]);  
        
        // model name
        translate([20.5, 10.5, -0.3]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text(phoneName);
    }
}